package dev.wceng.sufei.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val favoriteApiService: FavoriteApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    private var isFirstLoad = true

    init {
        loadRandomPoem()
    }

    private fun loadRandomPoem() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val userPoem = poemRepository.getRandomUserPoem().first()
                _uiState.value = if (userPoem != null) {
                    HomeUiState.Success(userPoem)
                } else {
                    HomeUiState.Error("未能偶遇诗句")
                }
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Failed to connect") == true ||
                    e.message?.contains("Network is unreachable") == true ->
                        "网络连接失败，请检查网络后重试"
                    e is java.net.ConnectException ->
                        "无法连接到服务器，请稍后重试"
                    e is java.net.SocketTimeoutException ->
                        "请求超时，请检查网络后重试"
                    else -> e.message ?: "加载失败"
                }
                _uiState.value = HomeUiState.Error(message)
            }
            isFirstLoad = false
        }
    }

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        if (!tokenManager.isLoggedIn) {
            _showLoginDialog.value = true
            return
        }
        val current = _uiState.value
        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(
                userPoem = current.userPoem.copy(isFavorite = isFavorite)
            )
        }
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    favoriteApiService.addFavorite(poemId)
                } else {
                    favoriteApiService.removeFavorite(poemId)
                }
            } catch (_: Exception) {
                if (current is HomeUiState.Success) {
                    _uiState.value = current
                }
            }
        }
    }

    fun dismissLoginDialog() {
        _showLoginDialog.value = false
    }

    fun refresh() {
        loadRandomPoem()
    }

    fun refreshCurrentPoem() {
        val current = _uiState.value
        if (current !is HomeUiState.Success) return
        viewModelScope.launch {
            try {
                val userPoem = poemRepository.getUserPoemById(current.userPoem.poem.id).first()
                if (userPoem != null) {
                    _uiState.value = HomeUiState.Success(userPoem)
                }
            } catch (_: Exception) { }
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val userPoem: UserPoem) : HomeUiState
    data class Error(val message: String) : HomeUiState
}