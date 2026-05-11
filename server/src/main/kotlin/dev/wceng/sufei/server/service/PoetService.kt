package dev.wceng.sufei.server.service

import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.PoetDescriptionDto
import dev.wceng.sufei.server.models.PoemListItem
import dev.wceng.sufei.server.models.PoetListItem
import dev.wceng.sufei.server.models.PoetResponse
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import java.sql.ResultSet

private val logger = KtorSimpleLogger("PoetService")
private val json = Json { ignoreUnknownKeys = true }

object PoetService {

    fun getPoets(page: Int, limit: Int): Pair<List<PoetListItem>, Long> {
        val offset = (page - 1) * limit
        val items = mutableListOf<PoetListItem>()
        var total = 0L

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement("SELECT COUNT(*) FROM poets").use { stmt ->
                stmt.executeQuery().use { rs ->
                    if (rs.next()) total = rs.getLong(1)
                }
            }

            conn.prepareStatement(
                "SELECT id, name, dynasty, lifetime, poem_count FROM poets ORDER BY name LIMIT ? OFFSET ?"
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.setInt(2, offset)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(PoetListItem(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            dynasty = rs.getString("dynasty"),
                            lifetime = rs.getString("lifetime"),
                            poemCount = rs.getInt("poem_count")
                        ))
                    }
                }
            }
        }

        return items to total
    }

    fun getPoetById(id: String): PoetResponse? {
        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement("SELECT * FROM poets WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) return toPoetResponse(rs)
                }
            }
        }
        return null
    }

    fun searchPoets(query: String, limit: Int): List<PoetListItem> {
        val items = mutableListOf<PoetListItem>()

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                """SELECT id, name, dynasty, lifetime, poem_count 
                   FROM poets 
                   WHERE name LIKE ? OR dynasty LIKE ?
                   ORDER BY poem_count DESC
                   LIMIT ?""".trimIndent()
            ).use { stmt ->
                val likeQuery = "%$query%"
                stmt.setString(1, likeQuery)
                stmt.setString(2, likeQuery)
                stmt.setInt(3, limit)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(PoetListItem(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            dynasty = rs.getString("dynasty"),
                            lifetime = rs.getString("lifetime"),
                            poemCount = rs.getInt("poem_count")
                        ))
                    }
                }
            }
        }

        return items
    }

    fun getTopPoets(limit: Int): List<PoetListItem> {
        val items = mutableListOf<PoetListItem>()

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                "SELECT id, name, dynasty, lifetime, poem_count FROM poets ORDER BY poem_count DESC LIMIT ?"
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(PoetListItem(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            dynasty = rs.getString("dynasty"),
                            lifetime = rs.getString("lifetime"),
                            poemCount = rs.getInt("poem_count")
                        ))
                    }
                }
            }
        }

        return items
    }

    fun getPoemsByAuthor(authorName: String, page: Int, limit: Int): Pair<List<PoemListItem>, Long> {
        val offset = (page - 1) * limit
        val items = mutableListOf<PoemListItem>()
        var total = 0L

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                "SELECT COUNT(*) FROM poems WHERE author = ?"
            ).use { stmt ->
                stmt.setString(1, authorName)
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
                WHERE p.author = ?
                GROUP BY p.id
                LIMIT ? OFFSET ?
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, authorName)
                stmt.setInt(2, limit)
                stmt.setInt(3, offset)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(toPoemListItem(rs))
                    }
                }
            }
        }

        return items to total
    }

    private fun toPoetResponse(rs: ResultSet): PoetResponse {
        val descriptionsJson = rs.getString("descriptions")
        val descriptions: List<PoetDescriptionDto> = if (!descriptionsJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<PoetDescriptionDto>>(descriptionsJson)
            } catch (e: Exception) {
                emptyList<PoetDescriptionDto>()
            }
        } else {
            emptyList<PoetDescriptionDto>()
        }

        return PoetResponse(
            id = rs.getString("id"),
            name = rs.getString("name"),
            dynasty = rs.getString("dynasty"),
            avatarUrl = rs.getString("avatar_url"),
            lifetime = rs.getString("lifetime"),
            descriptions = descriptions,
            poemCount = rs.getInt("poem_count")
        )
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
}