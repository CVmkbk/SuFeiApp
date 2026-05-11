package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.service.SearchService
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.searchRoutes() {
    route("/api/v1/search") {
        get {
            val query = call.request.queryParameters["q"]
            val dynasty = call.request.queryParameters["dynasty"]
            val tag = call.request.queryParameters["tag"]
            val tune = call.request.queryParameters["tune"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

            val result = SearchService.searchAll(query, dynasty, tag, tune, limit.coerceIn(1, 100))

            call.respond(ApiResponse(data = result))
        }
    }
}