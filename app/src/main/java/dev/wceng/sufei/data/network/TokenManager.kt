package dev.wceng.sufei.data.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sufei_auth", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(!prefs.getString(KEY_TOKEN, null).isNullOrBlank())
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
            _isLoggedIn.value = !value.isNullOrBlank()
        }

    var userId: Long
        get() = prefs.getLong(KEY_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_USER_ID, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var nickname: String?
        get() = prefs.getString(KEY_NICKNAME, null)
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    var avatarUrl: String?
        get() = prefs.getString(KEY_AVATAR_URL, null)
        set(value) = prefs.edit().putString(KEY_AVATAR_URL, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    fun saveUserInfo(user: dev.wceng.sufei.data.network.dto.UserResponse) {
        userId = user.id
        username = user.username
        nickname = user.nickname ?: user.username
        avatarUrl = user.avatarUrl
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_AVATAR_URL = "avatar_url"
    }
}