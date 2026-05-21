package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.network.dto.AuthResponse
import dev.wceng.sufei.data.network.dto.UserResponse

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: UserResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

interface AuthRepository {
    val isLoggedIn: Boolean
    val currentUserId: Long
    val currentUserName: String?
    val currentNickname: String?
    val currentAvatarUrl: String?

    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(username: String, email: String, password: String): Result<AuthResponse>
    suspend fun logout()
}