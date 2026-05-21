package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.AuthApiService
import dev.wceng.sufei.data.network.dto.AuthResponse
import dev.wceng.sufei.data.network.dto.LoginRequest
import dev.wceng.sufei.data.network.dto.RegisterRequest
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn

    override val currentUserId: Long
        get() = tokenManager.userId

    override val currentUserName: String?
        get() = tokenManager.username

    override val currentNickname: String?
        get() = tokenManager.nickname

    override val currentAvatarUrl: String?
        get() = tokenManager.avatarUrl

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            if (response.code == 200 && response.data != null) {
                tokenManager.token = response.data.token
                tokenManager.saveUserInfo(response.data.user)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = when (e.code()) {
                401 -> "邮箱或密码错误"
                400 -> "请求参数有误"
                else -> errorBody ?: "服务器错误 (${e.code()})"
            }
            Result.failure(Exception(message))
        } catch (e: IOException) {
            Result.failure(Exception("网络连接失败，请检查：\n1. 服务器是否已启动\n2. 模拟器是否可访问 10.0.2.2:8080\n3. 防火墙是否拦截"))
        } catch (e: Exception) {
            Result.failure(Exception("登录失败：${e.message}"))
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        return try {
            val response = authApiService.register(RegisterRequest(username, email, password))
            if (response.code == 201 && response.data != null) {
                tokenManager.token = response.data.token
                tokenManager.saveUserInfo(response.data.user)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = when (e.code()) {
                400 -> try {
                    // 尝试从错误体中提取服务端返回的具体错误信息
                    val errorResponse = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString<dev.wceng.sufei.data.network.dto.ApiResponse<Unit>>(
                            errorBody ?: "{}"
                        )
                    errorResponse.message
                } catch (_: Exception) {
                    "请求参数有误"
                }
                409 -> "用户名或邮箱已被注册"
                else -> errorBody ?: "服务器错误 (${e.code()})"
            }
            Result.failure(Exception(message))
        } catch (e: IOException) {
            Result.failure(Exception("网络连接失败，请检查服务器是否启动"))
        } catch (e: Exception) {
            Result.failure(Exception("注册失败：${e.message}"))
        }
    }

    override suspend fun logout() {
        tokenManager.clear()
    }
}