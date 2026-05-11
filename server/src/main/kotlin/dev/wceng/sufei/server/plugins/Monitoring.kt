package dev.wceng.sufei.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val code: Int, val message: String)

fun Application.configureMonitoring() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(500, cause.message ?: "Internal Server Error")
            )
        }
    }
}