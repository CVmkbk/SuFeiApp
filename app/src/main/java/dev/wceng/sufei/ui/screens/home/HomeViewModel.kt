package dev.wceng.sufei.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.local.room.PoemDao
import dev.wceng.sufei.data.local.room.entity.toPoem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val tokenManager: TokenManager,
    private val favoriteApiService: FavoriteApiService,
    private val poemDao: PoemDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadRandomPoem()
    }

    private fun loadRandomPoem() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val entity = poemDao.getHighQualityRandomPoem().first()
                    ?: poemDao.getRandomPoem().first()
                if (entity != null) {
                    val prefs = userPreferencesRepository.userPreferences.first()
                    val userPoem = UserPoem(poem = entity.toPoem(), userPreferences = prefs)
                    _uiState.value = HomeUiState.Success(userPoem)
                } else {
                    val userPoem = poemRepository.getRandomUserPoem().first()
                    _uiState.value = if (userPoem != null) {
                        HomeUiState.Success(userPoem)
                    } else {
                        HomeUiState.Error("未能偶遇诗句")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(poemId, isFavorite)
            val current = _uiState.value
            if (current is HomeUiState.Success) {
                _uiState.value = current.copy(
                    userPoem = current.userPoem.copy(isFavorite = isFavorite)
                )
            }
            if (tokenManager.isLoggedIn) {
                if (isFavorite) {
                    favoriteApiService.addFavorite(poemId)
                } else {
                    favoriteApiService.removeFavorite(poemId)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadRandomPoem()
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val userPoem: UserPoem) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
