package dev.wceng.sufei.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val tokenManager: TokenManager,
    private val favoriteApiService: FavoriteApiService
) : ViewModel() {

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
}
