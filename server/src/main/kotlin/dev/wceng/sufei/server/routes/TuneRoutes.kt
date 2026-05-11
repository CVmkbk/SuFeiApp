package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.service.TuneService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tuneRoutes() {
    route("/api/v1/tunes") {
        get {
            val tunes = TuneService.getAllTunes()
            call.respond(ApiResponse(data = tunes))
        }

        get("/recommended") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val tunes = TuneService.getRecommendedTunes(limit.coerceIn(1, 100))
            call.respond(ApiResponse(data = tunes))
        }
    }
}