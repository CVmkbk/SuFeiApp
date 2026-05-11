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
                val dataSource = DatabaseFactory.getDataSource()
                JsonlImporter.importIfNeeded(dataSource)
                call.respond(HttpStatusCode.OK, mapOf("message" to "数据导入完成"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
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