package dev.wceng.sufei.server.routes

import dev.wceng.sufei.server.models.ApiResponse
import dev.wceng.sufei.server.models.LoginRequest
import dev.wceng.sufei.server.models.RegisterRequest
import dev.wceng.sufei.server.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/api/v1/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = UserService.register(request)

            result.fold(
                onSuccess = { authResponse ->
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(data = authResponse)
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Unit>(code = 400, message = error.message ?: "注册失败")
                    )
                }
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = UserService.login(request)

            result.fold(
                onSuccess = { authResponse ->
                    call.respond(ApiResponse(data = authResponse))
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(code = 401, message = error.message ?: "登录失败")
                    )
                }
            )
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse<Unit>(code = 401, message = "无效的认证令牌")
                    )
                    return@get
                }

                val user = UserService.getUserById(userId)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(code = 404, message = "用户不存在")
                    )
                    return@get
                }

                call.respond(ApiResponse(data = user))
            }
        }
    }
}