package dev.wceng.sufei.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val favoriteApiService: FavoriteApiService
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0L)

    val uiState: StateFlow<CollectionUiState> = refreshTrigger
        .flatMapLatest {
            poemRepository.getFavoriteUserPoems()
        }
        .map { poems ->
            if (poems.isEmpty()) {
                CollectionUiState.Empty
            } else {
                CollectionUiState.Success(poems)
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
            emit(CollectionUiState.Error(message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CollectionUiState.Loading
        )

    fun toggleFavorite(poemId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    favoriteApiService.addFavorite(poemId)
                } else {
                    favoriteApiService.removeFavorite(poemId)
                }
                refreshTrigger.value = refreshTrigger.value + 1
            } catch (_: Exception) {
            }
        }
    }

    fun refresh() {
        refreshTrigger.value = refreshTrigger.value + 1
    }
}

sealed interface CollectionUiState {
    data object Loading : CollectionUiState
    data object Empty : CollectionUiState
    data class Success(val poems: List<UserPoem>) : CollectionUiState
    data class Error(val message: String) : CollectionUiState
}