package dev.wceng.sufei.server.service

import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.PoemListItem
import io.ktor.util.logging.KtorSimpleLogger

private val logger = KtorSimpleLogger("FavoriteService")

object FavoriteService {

    fun addFavorite(userId: Long, poemId: String): Result<Unit> {
        return try {
            DatabaseFactory.getDataSource().connection.use { conn ->
                conn.prepareStatement(
                    "INSERT IGNORE INTO favorites (user_id, poem_id) VALUES (?, ?)"
                ).use { stmt ->
                    stmt.setLong(1, userId)
                    stmt.setString(2, poemId)
                    stmt.executeUpdate()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("添加收藏失败: ${e.message}", e)
            Result.failure(RuntimeException("添加收藏失败"))
        }
    }

    fun removeFavorite(userId: Long, poemId: String): Result<Unit> {
        return try {
            DatabaseFactory.getDataSource().connection.use { conn ->
                conn.prepareStatement(
                    "DELETE FROM favorites WHERE user_id = ? AND poem_id = ?"
                ).use { stmt ->
                    stmt.setLong(1, userId)
                    stmt.setString(2, poemId)
                    stmt.executeUpdate()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("取消收藏失败: ${e.message}", e)
            Result.failure(RuntimeException("取消收藏失败"))
        }
    }

    fun isFavorited(userId: Long, poemId: String): Boolean {
        return DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                "SELECT 1 FROM favorites WHERE user_id = ? AND poem_id = ?"
            ).use { stmt ->
                stmt.setLong(1, userId)
                stmt.setString(2, poemId)
                stmt.executeQuery().use { rs ->
                    rs.next()
                }
            }
        }
    }

    fun getFavorites(userId: Long, page: Int, limit: Int): Pair<List<PoemListItem>, Long> {
        val offset = (page - 1) * limit
        val items = mutableListOf<PoemListItem>()
        var total = 0L

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                "SELECT COUNT(*) FROM favorites WHERE user_id = ?"
            ).use { stmt ->
                stmt.setLong(1, userId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) total = rs.getLong(1)
                }
            }

            conn.prepareStatement(
                """
                SELECT p.id, p.title, p.author, p.dynasty, p.content,
                       GROUP_CONCAT(pt.tag_name) as tags
                FROM favorites f
                JOIN poems p ON f.poem_id = p.id
                LEFT JOIN poem_tags pt ON p.id = pt.poem_id
                WHERE f.user_id = ?
                GROUP BY p.id
                ORDER BY f.created_at DESC
                LIMIT ? OFFSET ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setLong(1, userId)
                stmt.setInt(2, limit)
                stmt.setInt(3, offset)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val tagsStr = rs.getString("tags")
                        val tags = if (tagsStr != null) tagsStr.split(",") else emptyList()
                        items.add(
                            PoemListItem(
                                id = rs.getString("id"),
                                title = rs.getString("title"),
                                author = rs.getString("author"),
                                dynasty = rs.getString("dynasty"),
                                content = rs.getString("content"),
                                tags = tags
                            )
                        )
                    }
                }
            }
        }

        return items to total
    }
}