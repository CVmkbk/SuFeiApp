package dev.wceng.sufei.server.service

import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.PoemListItem
import dev.wceng.sufei.server.models.PoemResponse
import io.ktor.util.logging.KtorSimpleLogger
import java.sql.ResultSet

private val logger = KtorSimpleLogger("PoemService")

object PoemService {

    fun getPoems(page: Int, limit: Int): Pair<List<PoemListItem>, Long> {
        val offset = (page - 1) * limit
        val items = mutableListOf<PoemListItem>()
        var total = 0L

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement("SELECT COUNT(*) FROM poems").use { stmt ->
                stmt.executeQuery().use { rs ->
                    if (rs.next()) total = rs.getLong(1)
                }
            }

            conn.prepareStatement(
                """
                SELECT p.id, p.title, p.author, p.dynasty, p.content, 
                       GROUP_CONCAT(pt.tag_name) as tags
                FROM poems p
                LEFT JOIN poem_tags pt ON p.id = pt.poem_id
                GROUP BY p.id
                ORDER BY p.created_at DESC
                LIMIT ? OFFSET ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.setInt(2, offset)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(toPoemListItem(rs))
                    }
                }
            }
        }

        return items to total
    }

    fun getPoemById(id: String): PoemResponse? {
        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                """
                SELECT p.*, GROUP_CONCAT(pt.tag_name) as tags
                FROM poems p
                LEFT JOIN poem_tags pt ON p.id = pt.poem_id
                WHERE p.id = ?
                GROUP BY p.id
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, id)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) return toPoemResponse(rs)
                }
            }
        }
        return null
    }

    fun getRandomPoem(): PoemResponse? {
        DatabaseFactory.getDataSource().connection.use { conn ->
            val count = conn.prepareStatement("SELECT COUNT(*) FROM poems").use { stmt ->
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0
                }
            }
            if (count == 0L) return null

            val offset = (Math.random() * count).toLong()

            conn.prepareStatement(
                """
                SELECT p.*, GROUP_CONCAT(pt.tag_name) as tags
                FROM (SELECT * FROM poems LIMIT 1 OFFSET ?) p
                LEFT JOIN poem_tags pt ON p.id = pt.poem_id
                GROUP BY p.id
                """.trimIndent()
            ).use { stmt ->
                stmt.setLong(1, offset)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) return toPoemResponse(rs)
                }
            }
        }
        return null
    }

    fun getHighQualityRandomPoem(): PoemResponse? {
        DatabaseFactory.getDataSource().connection.use { conn ->
            val count = conn.prepareStatement(
                """
                SELECT COUNT(*) FROM poems
                WHERE notes IS NOT NULL AND notes != ''
                  AND translation IS NOT NULL AND translation != ''
                  AND intro IS NOT NULL AND intro != ''
                """.trimIndent()
            ).use { stmt ->
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0
                }
            }
            if (count == 0L) return null

            val offset = (Math.random() * count).toLong()

            conn.prepareStatement(
                """
                SELECT p.*, GROUP_CONCAT(pt.tag_name) as tags
                FROM (
                    SELECT * FROM poems 
                    WHERE notes IS NOT NULL AND notes != ''
                      AND translation IS NOT NULL AND translation != ''
                      AND intro IS NOT NULL AND intro != ''
                    LIMIT 1 OFFSET ?
                ) p
                LEFT JOIN poem_tags pt ON p.id = pt.poem_id
                GROUP BY p.id
                """.trimIndent()
            ).use { stmt ->
                stmt.setLong(1, offset)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) return toPoemResponse(rs)
                }
            }
        }
        return null
    }

    private fun toPoemListItem(rs: ResultSet): PoemListItem {
        val tagsStr = rs.getString("tags")
        val tags = if (tagsStr != null) tagsStr.split(",") else emptyList()
        return PoemListItem(
            id = rs.getString("id"),
            title = rs.getString("title"),
            author = rs.getString("author"),
            dynasty = rs.getString("dynasty"),
            content = rs.getString("content"),
            tags = tags
        )
    }

    private fun toPoemResponse(rs: ResultSet): PoemResponse {
        val tagsStr = rs.getString("tags")
        val tags = if (tagsStr != null) tagsStr.split(",") else emptyList()
        return PoemResponse(
            id = rs.getString("id"),
            title = rs.getString("title"),
            author = rs.getString("author"),
            dynasty = rs.getString("dynasty"),
            content = rs.getString("content"),
            tags = tags,
            notes = rs.getString("notes"),
            translation = rs.getString("translation"),
            intro = rs.getString("intro"),
            background = rs.getString("background")
        )
    }
}