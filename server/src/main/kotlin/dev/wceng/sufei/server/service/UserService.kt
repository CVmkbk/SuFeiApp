package dev.wceng.sufei.server.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.wceng.sufei.server.database.DatabaseFactory
import dev.wceng.sufei.server.models.AuthResponse
import dev.wceng.sufei.server.models.LoginRequest
import dev.wceng.sufei.server.models.RegisterRequest
import dev.wceng.sufei.server.models.UserResponse
import io.ktor.server.config.*
import io.ktor.util.logging.KtorSimpleLogger
import java.util.*

private val logger = KtorSimpleLogger("UserService")

object UserService {

    private const val MIN_USERNAME_LENGTH = 2
    private const val MAX_USERNAME_LENGTH = 50
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MAX_PASSWORD_LENGTH = 100

    private var jwtConfig: ApplicationConfig? = null

    fun init(config: ApplicationConfig) {
        jwtConfig = config
    }

    fun register(request: RegisterRequest): Result<AuthResponse> {
        val username = request.username.trim()
        val email = request.email.trim().lowercase()
        val password = request.password

        if (username.length < MIN_USERNAME_LENGTH || username.length > MAX_USERNAME_LENGTH) {
            return Result.failure(IllegalArgumentException("用户名长度需在 $MIN_USERNAME_LENGTH-$MAX_USERNAME_LENGTH 之间"))
        }

        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            return Result.failure(IllegalArgumentException("邮箱格式不正确"))
        }

        if (password.length < MIN_PASSWORD_LENGTH || password.length > MAX_PASSWORD_LENGTH) {
            return Result.failure(IllegalArgumentException("密码长度需在 $MIN_PASSWORD_LENGTH-$MAX_PASSWORD_LENGTH 之间"))
        }

        val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        return try {
            DatabaseFactory.getDataSource().connection.use { conn ->
                conn.prepareStatement(
                    "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                ).use { stmt ->
                    stmt.setString(1, username)
                    stmt.setString(2, email)
                    stmt.setString(3, passwordHash)
                    stmt.executeUpdate()

                    val generatedKeys = stmt.generatedKeys
                    if (generatedKeys.next()) {
                        val userId = generatedKeys.getLong(1)
                        val user = UserResponse(id = userId, username = username, email = email)
                        val token = generateToken(userId, email)
                        Result.success(AuthResponse(token = token, user = user))
                    } else {
                        Result.failure(RuntimeException("用户创建失败"))
                    }
                }
            }
        } catch (e: java.sql.SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("uk_username") == true) {
                Result.failure(IllegalArgumentException("用户名已存在"))
            } else if (e.message?.contains("uk_email") == true) {
                Result.failure(IllegalArgumentException("邮箱已被注册"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            logger.error("注册失败: ${e.message}", e)
            Result.failure(RuntimeException("注册失败，请稍后重试"))
        }
    }

    fun login(request: LoginRequest): Result<AuthResponse> {
        val email = request.email.trim().lowercase()
        val password = request.password

        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("邮箱和密码不能为空"))
        }

        return try {
            DatabaseFactory.getDataSource().connection.use { conn ->
                conn.prepareStatement(
                    "SELECT id, username, email, password_hash FROM users WHERE email = ?"
                ).use { stmt ->
                    stmt.setString(1, email)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            val userId = rs.getLong("id")
                            val username = rs.getString("username")
                            val passwordHash = rs.getString("password_hash")

                            val result = BCrypt.verifyer().verify(password.toCharArray(), passwordHash)
                            if (result.verified) {
                                val user = UserResponse(id = userId, username = username, email = email)
                                val token = generateToken(userId, email)
                                Result.success(AuthResponse(token = token, user = user))
                            } else {
                                Result.failure(IllegalArgumentException("邮箱或密码错误"))
                            }
                        } else {
                            Result.failure(IllegalArgumentException("邮箱或密码错误"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("登录失败: ${e.message}", e)
            Result.failure(RuntimeException("登录失败，请稍后重试"))
        }
    }

    fun getUserById(userId: Long): UserResponse? {
        DatabaseFactory.getDataSource().connection.use { conn ->
            conn.prepareStatement(
                "SELECT id, username, email FROM users WHERE id = ?"
            ).use { stmt ->
                stmt.setLong(1, userId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return UserResponse(
                            id = rs.getLong("id"),
                            username = rs.getString("username"),
                            email = rs.getString("email")
                        )
                    }
                }
            }
        }
        return null
    }

    private fun generateToken(userId: Long, email: String): String {
        val config = jwtConfig ?: error("JWT config not initialized")
        val secret = config.property("jwt.secret").getString()
        val issuer = config.property("jwt.issuer").getString()
        val audience = config.property("jwt.audience").getString()
        val expirationMs = config.property("jwt.tokenExpirationMs").getString().toLong()

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
            .sign(Algorithm.HMAC256(secret))
    }
}