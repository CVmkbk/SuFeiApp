package dev.wceng.sufei.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.repository.AuthRepository
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val favoriteApiService: FavoriteApiService
) : ViewModel() {

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn

    val displayName: String
        get() = if (isLoggedIn) {
            authRepository.currentNickname ?: authRepository.currentUserName ?: ""
        } else ""

    val avatarUrl: String?
        get() = if (isLoggedIn) authRepository.currentAvatarUrl else null

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val favoritePoems: StateFlow<List<UserPoem>> = combine(
        poemRepository.getFavoriteUserPoems(),
        userPreferencesRepository.userPreferences
    ) { serverFavorites, userPrefs ->
        serverFavorites to userPrefs
    }.flatMapLatest { (serverFavorites, userPrefs) ->
        if (serverFavorites.isNotEmpty()) {
            flowOf(serverFavorites)
        } else if (userPrefs.favoritePoemIds.isNotEmpty()) {
            val flows = userPrefs.favoritePoemIds.map { id ->
                poemRepository.getUserPoemById(id)
            }
            combine(flows) { poems -> poems.filterNotNull() }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(poemId, isFavorite)
            if (tokenManager.isLoggedIn) {
                if (isFavorite) {
                    favoriteApiService.addFavorite(poemId)
                } else {
                    favoriteApiService.removeFavorite(poemId)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setFontSizeMultiplier(multiplier)
        }
    }

    fun setLineHeightMultiplier(multiplier: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setLineHeightMultiplier(multiplier)
        }
    }

    fun setUseDarkTheme(use: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setUseDarkTheme(use)
        }
    }

    fun setFontFamilyName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setFontFamilyName(name)
        }
    }
}