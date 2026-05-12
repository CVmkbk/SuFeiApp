package dev.wceng.sufei.data.network.api

import dev.wceng.sufei.data.network.dto.ApiResponse
import dev.wceng.sufei.data.network.dto.AuthResponse
import dev.wceng.sufei.data.network.dto.LoginRequest
import dev.wceng.sufei.data.network.dto.RegisterRequest
import dev.wceng.sufei.data.network.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(): ApiResponse<UserResponse>
}