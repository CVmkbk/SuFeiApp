package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.service.TagService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tagRoutes() {
    route("/api/v1/tags") {
        get {
            val tags = TagService.getAllTags()
            call.respond(ApiResponse(data = tags))
        }

        get("/recommended") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 30
            val tags = TagService.getRecommendedTags(limit.coerceIn(1, 100))
            call.respond(ApiResponse(data = tags))
        }
    }
}