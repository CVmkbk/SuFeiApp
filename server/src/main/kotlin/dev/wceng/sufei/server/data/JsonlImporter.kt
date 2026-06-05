package dev.wceng.sufei.server.data

import com.zaxxer.hikari.HikariDataSource
import dev.wceng.sufei.server.models.*
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.sql.Connection
import java.sql.Timestamp

private val logger = KtorSimpleLogger("JsonlImporter")

object JsonlImporter {

    private val json = Json { ignoreUnknownKeys = true }

    // ================================================================
    // 批量导入参数
    // ================================================================
    private const val POEM_BATCH_SIZE = 2000      // 诗词批量大小
    private const val TAG_BATCH_SIZE = 2000       // 诗词标签批量大小
    private const val REF_BATCH_SIZE = 1000       // 标签/词牌/诗人批量大小

    /**
     * 进度回调接口，用于向外部（如 API 路由）报告导入进度
     */
    fun interface ProgressCallback {
        fun onProgress(
            phase: String,          // 当前阶段描述
            current: Int,           // 当前已处理数
            total: Int,             // 预估总数
            message: String          // 附加描述
        )
    }

    // ================================================================
    // 公共入口
    // ================================================================

    /**
     * 增量导入：检查 import_metadata 表，仅导入未完成或需要更新的文件
     *
     * @param dataSource 数据库连接池
     * @param force      是否强制重新导入所有文件（忽略已有记录）
     * @param onProgress 进度回调（可选）
     */
    fun importIfNeeded(
        dataSource: HikariDataSource,
        force: Boolean = false,
        onProgress: ProgressCallback? = null
    ) {
        val totalStartTime = System.currentTimeMillis()

        // 初始化 import_metadata 表（确保 Flyway 迁移已执行）
        ensureMetadataTable(dataSource)

        // 计算各文件待导入记录数
        val filePlan = buildFilePlan(dataSource, force)

        if (filePlan.all { it.status == "completed" }) {
            logger.info("所有文件已导入完成，无需重复导入")
            onProgress?.onProgress("完成", 1, 1, "数据已是最新")
            return
        }

        val totalRecords = filePlan.sumOf { it.recordCount }
        var totalImported = filePlan.sumOf { it.importedCount }
        logger.info("========== 开始数据导入 (增量模式) ==========")
        logger.info("待导入: ${filePlan.filter { it.status != "completed" }.size} 个文件, " +
                "共 ${totalRecords - totalImported} 条新记录")

        try {
            // 1. 标签（轻量，先导入）
            importTagsIncremental(dataSource, filePlan, onProgress, totalRecords, totalImported)

            // 2. 词牌
            importTunesIncremental(dataSource, filePlan, onProgress, totalRecords, totalImported)

            // 3. 诗人
            importPoetsIncremental(dataSource, filePlan, onProgress, totalRecords, totalImported)

            // 4. 诗词（大量数据，最后导入）
            importPoemsIncremental(dataSource, filePlan, onProgress, totalRecords, totalImported)

            val elapsed = (System.currentTimeMillis() - totalStartTime) / 1000
            logger.info("========== 数据导入完成 (耗时: ${elapsed}s) ==========")
            onProgress?.onProgress("完成", 1, 1, "导入完成，耗时 ${elapsed}s")
        } catch (e: Exception) {
            logger.error("数据导入失败: ${e.message}", e)
            throw e
        }
    }

    // ================================================================
    // 文件计划：决定哪些文件需要导入
    // ================================================================

    private data class FileImportPlan(
        val fileName: String,
        val recordCount: Int,       // 文件中总记录数
        val importedCount: Int,     // 已导入数
        val status: String          // pending / importing / completed
    )

    private fun buildFilePlan(dataSource: HikariDataSource, force: Boolean): List<FileImportPlan> {
        // 文件定义: 文件名 -> 快速计数方式
        val fileDefs = listOf(
            "tags.jsonl" to { countLines("tags.jsonl") },
            "tunes.jsonl" to { countLines("tunes.jsonl") },
            "poets.jsonl" to { countLines("poets.jsonl") },
            "poems_0.jsonl" to { countLines("poems_0.jsonl") },
            "poems_1.jsonl" to { countLines("poems_1.jsonl") },
            "poems_2.jsonl" to { countLines("poems_2.jsonl") },
            "poems_3.jsonl" to { countLines("poems_3.jsonl") },
            "poems_4.jsonl" to { countLines("poems_4.jsonl") },
        )

        return dataSource.connection.use { conn ->
            fileDefs.map { (fileName, countFn) ->
                val recordCount = if (force) countFn() else getOrComputeRecordCount(conn, fileName, countFn)
                val meta = getMetadata(conn, fileName)
                val status = if (force) "pending" else (meta?.status ?: "pending")
                val importedCount = if (force) 0 else (meta?.importedCount ?: 0)
                FileImportPlan(fileName, recordCount, importedCount, status)
            }
        }
    }

    private fun getMetadata(conn: Connection, fileName: String): FileImportPlan? {
        conn.prepareStatement(
            "SELECT record_count, imported_count, status FROM import_metadata WHERE file_name = ?"
        ).use { stmt ->
            stmt.setString(1, fileName)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return FileImportPlan(
                        fileName = fileName,
                        recordCount = rs.getInt("record_count"),
                        importedCount = rs.getInt("imported_count"),
                        status = rs.getString("status")
                    )
                }
            }
        }
        return null
    }

    private fun getOrComputeRecordCount(conn: Connection, fileName: String, countFn: () -> Int): Int {
        conn.prepareStatement(
            "SELECT record_count FROM import_metadata WHERE file_name = ?"
        ).use { stmt ->
            stmt.setString(1, fileName)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val cached = rs.getInt("record_count")
                    if (cached > 0) return cached
                }
            }
        }
        return countFn()
    }

    private fun upsertMetadata(
        conn: Connection,
        fileName: String,
        recordCount: Int,
        importedCount: Int,
        status: String,
        errorMessage: String? = null
    ) {
        conn.prepareStatement(
            """INSERT INTO import_metadata (file_name, record_count, imported_count, status, started_at, completed_at, error_message)
               VALUES (?, ?, ?, ?, ?, ?, ?)
               ON DUPLICATE KEY UPDATE
                   record_count = VALUES(record_count),
                   imported_count = VALUES(imported_count),
                   status = VALUES(status),
                   completed_at = VALUES(completed_at),
                   error_message = VALUES(error_message)"""
        ).use { stmt ->
            stmt.setString(1, fileName)
            stmt.setInt(2, recordCount)
            stmt.setInt(3, importedCount)
            stmt.setString(4, status)
            stmt.setTimestamp(5, if (status == "importing") Timestamp(System.currentTimeMillis()) else null)
            stmt.setTimestamp(6, if (status == "completed") Timestamp(System.currentTimeMillis()) else null)
            stmt.setString(7, errorMessage)
            stmt.executeUpdate()
        }
    }

    private fun ensureMetadataTable(dataSource: HikariDataSource) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """CREATE TABLE IF NOT EXISTS import_metadata (
                    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                    file_name       VARCHAR(100)  NOT NULL,
                    record_count    INT           NOT NULL DEFAULT 0,
                    imported_count  INT           NOT NULL DEFAULT 0,
                    status          VARCHAR(20)   NOT NULL DEFAULT 'pending',
                    started_at      TIMESTAMP     NULL DEFAULT NULL,
                    completed_at    TIMESTAMP     NULL DEFAULT NULL,
                    error_message   TEXT          NULL DEFAULT NULL,
                    UNIQUE KEY uk_file_name (file_name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"""
            ).use { it.execute() }
        }
    }

    /**
     * 快速统计 JSONL 文件行数（跳过空行）
     */
    private fun countLines(fileName: String): Int {
        val stream = javaClass.getResourceAsStream("/$fileName") ?: return 0
        return BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
            reader.lineSequence().count { it.isNotBlank() }
        }
    }

    // ================================================================
    // 增量导入实现
    // ================================================================

    private fun importTagsIncremental(
        dataSource: HikariDataSource,
        filePlan: List<FileImportPlan>,
        onProgress: ProgressCallback?,
        totalRecords: Int,
        vararg currentImported: Int
    ) {
        val plan = filePlan.find { it.fileName == "tags.jsonl" } ?: return
        if (plan.status == "completed") {
            logger.info("[1/4] 标签已导入完成，跳过")
            return
        }

        logger.info("[1/4] 导入标签...")
        val stream = javaClass.getResourceAsStream("/tags.jsonl") ?: error("找不到 tags.jsonl")
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var imported = 0

        dataSource.connection.use { conn ->
            conn.autoCommit = false
            upsertMetadata(conn, "tags.jsonl", plan.recordCount, 0, "importing")

            val ps = conn.prepareStatement("INSERT IGNORE INTO tags (name) VALUES (?)")
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        runCatching {
                            val tag = json.decodeFromString<TagDto>(line)
                            ps.setString(1, tag.name)
                            ps.addBatch()
                            imported++
                            if (imported % REF_BATCH_SIZE == 0) {
                                ps.executeBatch()
                                upsertMetadata(conn, "tags.jsonl", plan.recordCount, imported, "importing")
                                onProgress?.onProgress("标签", imported, totalRecords, "已导入标签 $imported 条")
                            }
                        }
                    }
                }
            }
            ps.executeBatch()
            upsertMetadata(conn, "tags.jsonl", plan.recordCount, imported, "completed")
            conn.commit()
        }

        logger.info("[1/4] 标签导入完成: $imported 条")
        onProgress?.onProgress("标签", imported, totalRecords, "标签导入完成: $imported 条")
    }

    private fun importTunesIncremental(
        dataSource: HikariDataSource,
        filePlan: List<FileImportPlan>,
        onProgress: ProgressCallback?,
        totalRecords: Int,
        vararg currentImported: Int
    ) {
        val plan = filePlan.find { it.fileName == "tunes.jsonl" } ?: return
        if (plan.status == "completed") {
            logger.info("[2/4] 词牌已导入完成，跳过")
            return
        }

        logger.info("[2/4] 导入词牌...")
        val stream = javaClass.getResourceAsStream("/tunes.jsonl") ?: error("找不到 tunes.jsonl")
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var imported = 0

        dataSource.connection.use { conn ->
            conn.autoCommit = false
            upsertMetadata(conn, "tunes.jsonl", plan.recordCount, 0, "importing")

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
                            if (imported % REF_BATCH_SIZE == 0) {
                                ps.executeBatch()
                                upsertMetadata(conn, "tunes.jsonl", plan.recordCount, imported, "importing")
                                onProgress?.onProgress("词牌", imported, totalRecords, "已导入词牌 $imported 条")
                            }
                        }
                    }
                }
            }
            ps.executeBatch()
            upsertMetadata(conn, "tunes.jsonl", plan.recordCount, imported, "completed")
            conn.commit()
        }

        logger.info("[2/4] 词牌导入完成: $imported 条")
        onProgress?.onProgress("词牌", imported, totalRecords, "词牌导入完成: $imported 条")
    }

    private fun importPoetsIncremental(
        dataSource: HikariDataSource,
        filePlan: List<FileImportPlan>,
        onProgress: ProgressCallback?,
        totalRecords: Int,
        vararg currentImported: Int
    ) {
        val plan = filePlan.find { it.fileName == "poets.jsonl" } ?: return
        if (plan.status == "completed") {
            logger.info("[3/4] 诗人已导入完成，跳过")
            return
        }

        logger.info("[3/4] 导入诗人...")
        val stream = javaClass.getResourceAsStream("/poets.jsonl") ?: error("找不到 poets.jsonl")
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var imported = 0

        dataSource.connection.use { conn ->
            conn.autoCommit = false
            upsertMetadata(conn, "poets.jsonl", plan.recordCount, 0, "importing")

            val ps = conn.prepareStatement(
                "INSERT IGNORE INTO poets (id, name, dynasty, avatar_url, lifetime, descriptions, poem_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0)"
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
                            if (imported % REF_BATCH_SIZE == 0) {
                                ps.executeBatch()
                                upsertMetadata(conn, "poets.jsonl", plan.recordCount, imported, "importing")
                                onProgress?.onProgress("诗人", imported, totalRecords, "已导入诗人 $imported 条")
                            }
                        }
                    }
                }
            }
            ps.executeBatch()
            upsertMetadata(conn, "poets.jsonl", plan.recordCount, imported, "completed")
            conn.commit()
        }

        logger.info("[3/4] 诗人导入完成: $imported 条")
        onProgress?.onProgress("诗人", imported, totalRecords, "诗人导入完成: $imported 条")
    }

    private fun importPoemsIncremental(
        dataSource: HikariDataSource,
        filePlan: List<FileImportPlan>,
        onProgress: ProgressCallback?,
        totalRecords: Int,
        vararg currentImported: Int
    ) {
        val poemFiles = listOf("poems_0.jsonl", "poems_1.jsonl", "poems_2.jsonl", "poems_3.jsonl", "poems_4.jsonl")
        var globalImported = 0

        // 累计前面阶段已导入数，用于全局进度计算
        val priorImported = filePlan
            .filter { it.fileName in listOf("tags.jsonl", "tunes.jsonl", "poets.jsonl") }
            .sumOf { it.importedCount }

        for ((index, file) in poemFiles.withIndex()) {
            val plan = filePlan.find { it.fileName == file } ?: continue
            if (plan.status == "completed") {
                logger.info("[4/4] $file 已导入完成，跳过 (${plan.importedCount}/${plan.recordCount})")
                globalImported += plan.importedCount
                continue
            }

            logger.info("[4/4] 正在处理 $file (${index + 1}/${poemFiles.size})...")
            val stream = javaClass.getResourceAsStream("/$file") ?: error("找不到 $file")
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var fileImported = plan.importedCount // 从上次中断处继续统计

            dataSource.connection.use { conn ->
                conn.autoCommit = false

                // 禁用外键检查以加速批量导入
                conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0").use { it.execute() }

                try {
                    upsertMetadata(conn, file, plan.recordCount, fileImported, "importing")

                    val poemPs = conn.prepareStatement(
                        """INSERT IGNORE INTO poems (id, source_url, title, author, dynasty, content, notes, translation, intro, background) 
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
                    )
                    val tagPs = conn.prepareStatement(
                        "INSERT IGNORE INTO poem_tags (poem_id, tag_name) VALUES (?, ?)"
                    )

                    var poemBatchCount = 0
                    var tagBatchCount = 0

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
                                    poemBatchCount++

                                    for (tag in poem.tags) {
                                        tagPs.setString(1, poem.id)
                                        tagPs.setString(2, tag)
                                        tagPs.addBatch()
                                        tagBatchCount++
                                    }

                                    fileImported++
                                    globalImported++

                                    if (poemBatchCount >= POEM_BATCH_SIZE) {
                                        poemPs.executeBatch()
                                        tagPs.executeBatch()
                                        val progress = priorImported + globalImported
                                        upsertMetadata(conn, file, plan.recordCount, fileImported, "importing")
                                        onProgress?.onProgress("诗词", progress, totalRecords,
                                            "已导入诗词 $globalImported 条")
                                        poemBatchCount = 0
                                        tagBatchCount = 0
                                    }
                                }
                            }
                        }
                    }

                    // 处理剩余批次
                    if (poemBatchCount > 0) {
                        poemPs.executeBatch()
                        tagPs.executeBatch()
                    }

                    upsertMetadata(conn, file, plan.recordCount, fileImported, "completed")
                    conn.commit()
                } finally {
                    // 恢复外键检查
                    conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1").use { it.execute() }
                }
            }

            logger.info("[4/4] $file 完成: $fileImported 条")
        }

        logger.info("[4/4] 诗词导入完成: 共 $globalImported 条")
        onProgress?.onProgress("诗词", totalRecords, totalRecords, "诗词导入完成: $globalImported 条")
    }
}