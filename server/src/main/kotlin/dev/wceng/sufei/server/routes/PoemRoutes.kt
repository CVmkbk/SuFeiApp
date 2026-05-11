package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.models.PagedResponse
import dev.wceng.sufei.server.service.PoemService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.ceil

fun Route.poemRoutes() {
    route("/api/v1/poems") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            if (page < 1 || limit < 1 || limit > 100) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(code = 400, message = "Invalid page or limit parameter")
                )
                return@get
            }

            val (items, total) = PoemService.getPoems(page, limit)
            val totalPages = ceil(total.toDouble() / limit).toInt()

            call.respond(
                ApiResponse(
                    data = PagedResponse(
                        items = items,
                        total = total,
                        page = page,
                        totalPages = totalPages
                    )
                )
            )
        }

        get("/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(code = 400, message = "Poem ID is required")
                )
                return@get
            }

            val poem = PoemService.getPoemById(id)
            if (poem == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(code = 404, message = "Poem not found")
                )
                return@get
            }

            call.respond(ApiResponse(data = poem))
        }

        get("/random") {
            val poem = PoemService.getRandomPoem()
            if (poem == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(code = 404, message = "No poem found")
                )
                return@get
            }

            call.respond(ApiResponse(data = poem))
        }

        get("/random/high-quality") {
            val poem = PoemService.getHighQualityRandomPoem()
            if (poem == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(code = 404, message = "No high-quality poem found")
                )
                return@get
            }

            call.respond(ApiResponse(data = poem))
        }
    }
}