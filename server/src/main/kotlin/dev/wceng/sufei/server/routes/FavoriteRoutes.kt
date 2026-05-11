package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.models.FavoriteCheckResponse
import dev.wceng.sufei.server.models.PagedResponse
import dev.wceng.sufei.server.service.FavoriteService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.ceil

fun Route.favoriteRoutes() {
    authenticate("auth-jwt") {
        route("/api/v1/favorites") {

            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(code = 401, message = "无效的认证令牌")
                    )

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

                if (page < 1 || limit < 1 || limit > 100) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(code = 400, message = "参数无效")
                    )
                    return@get
                }

                val (items, total) = FavoriteService.getFavorites(userId, page, limit)
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

            post("/{poemId}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(code = 401, message = "无效的认证令牌")
                    )

                val poemId = call.parameters["poemId"]
                if (poemId.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(code = 400, message = "诗词ID不能为空")
                    )
                    return@post
                }

                val result = FavoriteService.addFavorite(userId, poemId)
                result.fold(
                    onSuccess = {
                        call.respond(ApiResponse<Unit>(message = "收藏成功"))
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse<Unit>(code = 500, message = error.message ?: "收藏失败")
                        )
                    }
                )
            }

            delete("/{poemId}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(code = 401, message = "无效的认证令牌")
                    )

                val poemId = call.parameters["poemId"]
                if (poemId.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(code = 400, message = "诗词ID不能为空")
                    )
                    return@delete
                }

                val result = FavoriteService.removeFavorite(userId, poemId)
                result.fold(
                    onSuccess = {
                        call.respond(ApiResponse<Unit>(message = "已取消收藏"))
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse<Unit>(code = 500, message = error.message ?: "取消收藏失败")
                        )
                    }
                )
            }

            get("/{poemId}/check") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(code = 401, message = "无效的认证令牌")
                    )

                val poemId = call.parameters["poemId"]
                if (poemId.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(code = 400, message = "诗词ID不能为空")
                    )
                    return@get
                }

                val favorited = FavoriteService.isFavorited(userId, poemId)
                call.respond(ApiResponse(data = FavoriteCheckResponse(favorited = favorited)))
            }
        }
    }
}