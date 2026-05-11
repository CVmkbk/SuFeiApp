package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.models.PagedResponse
import dev.wceng.sufei.server.service.PoetService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.ceil

fun Route.poetRoutes() {
    route("/api/v1/poets") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            if (page < 1 || limit < 1 || limit > 100) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(code = 400, message = "Invalid parameters"))
                return@get
            }

            val (items, total) = PoetService.getPoets(page, limit)
            val totalPages = ceil(total.toDouble() / limit).toInt()

            call.respond(ApiResponse(data = PagedResponse(items = items, total = total, page = page, totalPages = totalPages)))
        }

        get("/top") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val poets = PoetService.getTopPoets(limit.coerceIn(1, 50))
            call.respond(ApiResponse(data = poets))
        }

        get("/search") {
            val query = call.request.queryParameters["q"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(code = 400, message = "Parameter 'q' is required"))
                return@get
            }
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val poets = PoetService.searchPoets(query, limit.coerceIn(1, 100))
            call.respond(ApiResponse(data = poets))
        }

        get("/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(code = 400, message = "Poet ID required"))
                return@get
            }

            val poet = PoetService.getPoetById(id)
            if (poet == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(code = 404, message = "Poet not found"))
                return@get
            }

            call.respond(ApiResponse(data = poet))
        }

        get("/{id}/poems") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(code = 400, message = "Poet ID required"))
                return@get
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            val poet = PoetService.getPoetById(id)
            if (poet == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(code = 404, message = "Poet not found"))
                return@get
            }

            val (items, total) = PoetService.getPoemsByAuthor(poet.name, page, limit)
            val totalPages = ceil(total.toDouble() / limit).toInt()

            call.respond(ApiResponse(data = PagedResponse(items = items, total = total, page = page, totalPages = totalPages)))
        }
    }
}