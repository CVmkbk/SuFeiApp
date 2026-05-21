package dev.wceng.sufei.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.tts.TtsManager
import dev.wceng.sufei.ui.navigation.Detail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val poemRepository: PoemRepository,
    private val ttsManager: TtsManager,
    private val favoriteApiService: FavoriteApiService,
    private val tokenManager: TokenManager,
    @Assisted val detail: Detail
) : ViewModel() {

    val isTtsPlaying = ttsManager.isPlaying
    val currentSentenceIndex = ttsManager.currentSentenceIndex

    private val refreshTrigger = MutableStateFlow(0L)

    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DetailUiState> = refreshTrigger
        .flatMapLatest {
            poemRepository.getUserPoemById(detail.id)
        }
        .map { userPoem ->
            if (userPoem != null) {
                DetailUiState.Success(userPoem)
            } else {
                DetailUiState.Error("未找到该诗词")
            }
        }
        .catch { e ->
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
            emit(DetailUiState.Error(message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

    fun toggleFavorite(isFavorite: Boolean) {
        if (!tokenManager.isLoggedIn) {
            _showLoginDialog.value = true
            return
        }
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    favoriteApiService.addFavorite(detail.id)
                } else {
                    favoriteApiService.removeFavorite(detail.id)
                }
                refreshTrigger.value = refreshTrigger.value + 1
            } catch (_: Exception) {
            }
        }
    }

    fun dismissLoginDialog() {
        _showLoginDialog.value = false
    }

    fun refresh() {
        refreshTrigger.value = refreshTrigger.value + 1
    }

    fun toggleTts(sentences: List<String>) {
        if (isTtsPlaying.value) {
            ttsManager.stop()
        } else {
            ttsManager.speak(sentences)
        }
    }

    fun stopTts() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(detail: Detail): DetailViewModel
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val userPoem: UserPoem) : DetailUiState
    data class Error(val message: String) : DetailUiState
}