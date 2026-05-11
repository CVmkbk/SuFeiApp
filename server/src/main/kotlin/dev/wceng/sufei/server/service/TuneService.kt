package dev.wceng.sufei.server.service

import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.TuneResponse

object TuneService {

    fun getAllTunes(): List<TuneResponse> {
        val items = mutableListOf<TuneResponse>()

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement("SELECT name, description FROM tunes ORDER BY name").use { stmt ->
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(TuneResponse(
                            name = rs.getString("name"),
                            description = rs.getString("description")
                        ))
                    }
                }
            }
        }

        return items
    }

    fun getRecommendedTunes(limit: Int = 20): List<TuneResponse> {
        val items = mutableListOf<TuneResponse>()

        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                """SELECT name, description FROM tunes ORDER BY name LIMIT ?"""
            ).use { stmt ->
                stmt.setInt(1, limit)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        items.add(TuneResponse(
                            name = rs.getString("name"),
                            description = rs.getString("description")
                        ))
                    }
                }
            }
        }

        return items
    }
}