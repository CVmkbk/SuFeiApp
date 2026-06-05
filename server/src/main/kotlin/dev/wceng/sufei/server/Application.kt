package dev.wceng.sufei.server

import dev.wceng.sufei.server.data.JsonlImporter
import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.plugins.configureMonitoring
import dev.wceng.sufei.server.plugins.configureSecurity
import dev.wceng.sufei.server.plugins.configureSerialization
import dev.wceng.sufei.server.routes.*
import dev.wceng.sufei.server.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    UserService.init(environment.config)

    configureSerialization()
    configureMonitoring()
    configureSecurity()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "SuFei API"))
        }

        post("/api/v1/import") {
            try {
                val force = call.request.queryParameters["force"]?.toBoolean() ?: false
                val dataSource = DatabaseFactory.getDataSource()
                var lastProgress = ""
                JsonlImporter.importIfNeeded(dataSource, force = force) { phase, current, total, message ->
                    if (message.isNotEmpty() && message != lastProgress) {
                        lastProgress = message
                        log.info("[导入进度] $message")
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "数据导入完成",
                    "force" to force
                ))
            } catch (e: Exception) {
                log.error("导入失败: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        get("/api/v1/import/status") {
            try {
                val dataSource = DatabaseFactory.getDataSource()
                val statusList = dataSource.connection.use { conn ->
                    conn.prepareStatement(
                        "SELECT file_name, record_count, imported_count, status, completed_at FROM import_metadata ORDER BY file_name"
                    ).use { stmt ->
                        stmt.executeQuery().use { rs ->
                            val list = mutableListOf<Map<String, Any?>>()
                            while (rs.next()) {
                                list.add(mapOf(
                                    "file_name" to rs.getString("file_name"),
                                    "record_count" to rs.getInt("record_count"),
                                    "imported_count" to rs.getInt("imported_count"),
                                    "status" to rs.getString("status"),
                                    "completed_at" to rs.getTimestamp("completed_at")?.toString()
                                ))
                            }
                            list
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, statusList)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.OK, emptyList<Map<String, Any?>>())
            }
        }

        poemRoutes()
        poetRoutes()
        tagRoutes()
        tuneRoutes()
        searchRoutes()
        userRoutes()
        favoriteRoutes()
    }
}