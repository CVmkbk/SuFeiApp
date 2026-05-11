package dev.wceng.sufei.server.service

import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.TagResponse

object TagService {

    fun getAllTags(): List<TagResponse> {
        val items = mutableListOf<TagResponse>()

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement("SELECT name FROM tags ORDER BY name").use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(TagResponse(name = rs.getString("name")))
                    }
                }
            }
        }

        return items
    }

    fun getRecommendedTags(limit: Int = 30): List<TagResponse> {
        val items = mutableListOf<TagResponse>()

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                """SELECT t.name FROM tags t
                   INNER JOIN poem_tags pt ON t.name = pt.tag_name
                   GROUP BY t.name
                   ORDER BY COUNT(pt.poem_id) DESC
                   LIMIT ?"""
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(TagResponse(name = rs.getString("name")))
                    }
                }
            }
        }

        return items
    }
}