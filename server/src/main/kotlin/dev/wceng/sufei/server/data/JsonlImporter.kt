package dev.wceng.sufei.server.data

import com.zaxxer.hikari.HikariDataSource
import dev.wceng.sufei.server.models.*
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

private val logger = KtorSimpleLogger("JsonlImporter")

object JsonlImporter {

    private val json = Json { ignoreUnknownKeys = true }

    fun importIfNeeded(dataSource: HikariDataSource) {
        dataSource.connection.use { conn ->
            val count = conn.prepareStatement("SELECT COUNT(*) FROM poems").use { stmt ->
                stmt.executeQuery().let { rs -> if (rs.next()) rs.getLong(1) else 0 }
            }
            if (count > 0) {
                logger.info("数据库已有 $count 条诗词记录，跳过数据导入")
                return
            }
        }

        logger.info("========== 开始数据导入 ==========")

        try {
            importTags(dataSource)
            importTunes(dataSource)
            importPoets(dataSource)
            importPoems(dataSource)

            logger.info("========== 数据导入完成 ==========")
        } catch (e: Exception) {
            logger.error("数据导入失败: ${e.message}", e)
            throw e
        }
    }

    private fun importTags(dataSource: HikariDataSource) {
        logger.info("[1/4] 导入标签...")
        val stream = javaClass.getResourceAsStream("/tags.jsonl") ?: error("找不到 tags.jsonl")
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var imported = 0

        dataSource.connection.use { conn ->
            conn.autoCommit = false
            val ps = conn.prepareStatement("INSERT IGNORE INTO tags (name) VALUES (?)")

            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        runCatching {
                            val tag = json.decodeFromString<TagDto>(line)
                            ps.setString(1, tag.name)
                            ps.addBatch()
                            imported++
                            if (imported % 500 == 0) ps.executeBatch()
                        }
                    }
                }
            }
            ps.executeBatch()
            conn.commit()
        }

        logger.info("[1/4] 标签导入完成: $imported 条")
    }

    private fun importTunes(dataSource: HikariDataSource) {
        logger.info("[2/4] 导入词牌...")
        val stream = javaClass.getResourceAsStream("/tunes.jsonl") ?: error("找不到 tunes.jsonl")
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var imported = 0

        dataSource.connection.use { conn ->
            conn.autoCommit = false
            val ps = conn.prepareStatement("INSERT IGNORE INTO tunes (name, description) VALUES (?, ?)")

            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        runCatching {
                            val tune = json.decodeFromString<TuneDto>(line)
                            ps.setString(1, tune.name)
                            ps.setString(2, tune.description)
                            ps.addBatch()
                            imported++
                            if (imported % 200 == 0) ps.executeBatch()
                        }
                    }
                }
            }
            ps.executeBatch()
            conn.commit()
        }

        logger.info("[2/4] 词牌导入完成: $imported 条")
    }

    private fun importPoets(dataSource: HikariDataSource) {
        logger.info("[3/4] 导入诗人...")
        val stream = javaClass.getResourceAsStream("/poets.jsonl") ?: error("找不到 poets.jsonl")
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var imported = 0

        dataSource.connection.use { conn ->
            conn.autoCommit = false
            val ps = conn.prepareStatement(
                "INSERT IGNORE INTO poets (id, name, dynasty, avatar_url, lifetime, descriptions, poem_count) VALUES (?, ?, ?, ?, ?, ?, 0)"
            )
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        runCatching {
                            val poet = json.decodeFromString<PoetDto>(line)
                            ps.setString(1, poet.id)
                            ps.setString(2, poet.name)
                            ps.setString(3, poet.dynasty)
                            ps.setString(4, poet.avatarUrl)
                            ps.setString(5, poet.lifetime)
                            ps.setString(6, json.encodeToString(poet.descriptions))
                            ps.addBatch()
                            imported++
                            if (imported % 200 == 0) {
                                ps.executeBatch()
                                logger.info("[3/4] 已导入诗人: $imported 条...")
                            }
                        }
                    }
                }
            }
            ps.executeBatch()
            conn.commit()
        }

        logger.info("[3/4] 诗人导入完成: $imported 条")
    }

    private fun importPoems(dataSource: HikariDataSource) {
        logger.info("[4/4] 导入诗词...")

        val poemFiles = listOf("poems_0.jsonl", "poems_1.jsonl", "poems_2.jsonl", "poems_3.jsonl", "poems_4.jsonl")
        var totalImported = 0

        for ((index, file) in poemFiles.withIndex()) {
            logger.info("[4/4] 正在处理 ${file} (${index + 1}/${poemFiles.size})...")
            val stream = javaClass.getResourceAsStream("/$file") ?: error("找不到 $file")
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var fileImported = 0

            dataSource.connection.use { conn ->
                conn.autoCommit = false
                val poemPs = conn.prepareStatement(
                    """INSERT INTO poems (id, source_url, title, author, dynasty, content, notes, translation, intro, background) 
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
                )
                val tagPs = conn.prepareStatement(
                    "INSERT IGNORE INTO poem_tags (poem_id, tag_name) VALUES (?, ?)"
                )

                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            runCatching {
                                val poem = json.decodeFromString<PoemDto>(line)
                                poemPs.setString(1, poem.id)
                                poemPs.setString(2, poem.sourceUrl)
                                poemPs.setString(3, poem.title)
                                poemPs.setString(4, poem.author)
                                poemPs.setString(5, poem.dynasty)
                                poemPs.setString(6, poem.content)
                                poemPs.setString(7, poem.notes)
                                poemPs.setString(8, poem.translation)
                                poemPs.setString(9, poem.intro)
                                poemPs.setString(10, poem.background)
                                poemPs.addBatch()

                                for (tag in poem.tags) {
                                    tagPs.setString(1, poem.id)
                                    tagPs.setString(2, tag)
                                    tagPs.addBatch()
                                }

                                totalImported++
                                fileImported++
                                if (totalImported % 1000 == 0) {
                                    poemPs.executeBatch()
                                    tagPs.executeBatch()
                                    logger.info("[4/4] 已导入诗词: $totalImported 条...")
                                }
                            }
                        }
                    }
                }
                poemPs.executeBatch()
                tagPs.executeBatch()
                conn.commit()
            }

            logger.info("[4/4] ${file} 完成: $fileImported 条")
        }

        logger.info("[4/4] 诗词导入完成: 共 $totalImported 条")
    }
}