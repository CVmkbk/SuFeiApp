package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.PoetDescription
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.network.api.PoemApiService
import dev.wceng.sufei.data.network.api.PoetApiService
import dev.wceng.sufei.data.network.api.ReferenceApiService
import dev.wceng.sufei.data.network.dto.PoemListItem
import dev.wceng.sufei.data.network.dto.PoemResponse
import dev.wceng.sufei.data.network.dto.PoetListItem
import dev.wceng.sufei.data.network.dto.PoetResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkPoemRepositoryImpl @Inject constructor(
    private val poemApiService: PoemApiService,
    private val poetApiService: PoetApiService,
    private val referenceApiService: ReferenceApiService,
    private val favoriteApiService: FavoriteApiService,
    private val tokenManager: TokenManager
) : PoemRepository {

    override fun getAllUserPoems(limit: Int): Flow<List<UserPoem>> = flow {
        val response = poemApiService.getPoems(limit = limit)
        val items = response.data?.items?.map { it.toUserPoem() } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun getUserPoemById(id: String): Flow<UserPoem?> = flow {
        val response = poemApiService.getPoemById(id)
        val poemResponse = response.data ?: return@flow emit(null)
        val isFav = if (tokenManager.isLoggedIn) {
            runCatching { favoriteApiService.checkFavorite(id) }
                .getOrNull()?.data?.favorited ?: false
        } else {
            false
        }
        emit(poemResponse.toUserPoem(isFavorite = isFav))
    }.flowOn(Dispatchers.IO)

    override fun getPoemByIdFlow(id: String): Flow<Poem?> = flow {
        val response = poemApiService.getPoemById(id)
        emit(response.data?.toPoem())
    }.flowOn(Dispatchers.IO)

    override fun searchUserPoems(
        query: String,
        dynasty: String?,
        tag: String?,
        tune: String?,
        limit: Int
    ): Flow<List<UserPoem>> = flow {
        val response = poemApiService.search(query, dynasty, tag, tune, limit)
        val items = response.data?.poems?.map { it.toUserPoem() } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun searchAll(
        query: String,
        dynasty: String?,
        tag: String?,
        tune: String?,
        limit: Int
    ): Flow<SearchResult> = flow {
        val response = poemApiService.search(query, dynasty, tag, tune, limit)
        val data = response.data
        emit(
            SearchResult(
                poems = data?.poems?.map { it.toUserPoem() } ?: emptyList(),
                poets = data?.poets?.map { it.toPoet() } ?: emptyList()
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getRandomUserPoem(): Flow<UserPoem?> = flow {
        val response = poemApiService.getRandomPoem()
        emit(response.data?.toUserPoem())
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavoriteUserPoems(): Flow<List<UserPoem>> =
        tokenManager.isLoggedInFlow.flatMapLatest { loggedIn ->
            if (!loggedIn) {
                flowOf(emptyList())
            } else {
                flow {
                    val response = favoriteApiService.getFavorites()
                    val items = response.data?.items?.map { it.toUserPoem(isFavorite = true) } ?: emptyList()
                    emit(items)
                }.flowOn(Dispatchers.IO)
            }
        }

    override fun getAllTags(): Flow<List<Tag>> = flow {
        val response = referenceApiService.getTags()
        val items = response.data?.map { Tag(name = it.name) } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun getAllTunes(): Flow<List<Tune>> = flow {
        val response = referenceApiService.getTunes()
        val items = response.data?.map { Tune(name = it.name, description = it.description) } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun searchPoets(query: String): Flow<List<Poet>> = flow {
        val response = poetApiService.searchPoets(query)
        val items = response.data?.map { it.toPoet() } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun getTopPoets(limit: Int): Flow<List<Poet>> = flow {
        val response = poetApiService.getTopPoets(limit)
        val items = response.data?.map { it.toPoet() } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun getAllPoets(): Flow<List<Poet>> = flow {
        val response = poetApiService.getPoets()
        val items = response.data?.items?.map { it.toPoet() } ?: emptyList()
        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun getPoetById(id: String): Flow<Poet?> = flow {
        val response = poetApiService.getPoetById(id)
        emit(response.data?.toPoet())
    }.flowOn(Dispatchers.IO)

    override fun getPoemsByPoet(authorName: String): Flow<List<UserPoem>> = flow {
        val allPoets = poetApiService.getPoets(limit = 100)
        val poet = allPoets.data?.items?.find { it.name == authorName }
        if (poet != null) {
            val response = poetApiService.getPoemsByPoet(poet.id)
            val items = response.data?.items?.map { it.toUserPoem() } ?: emptyList()
            emit(items)
        } else {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // ========== DTO → Domain 映射 ==========

    private fun PoemResponse.toPoem() = Poem(
        id = id,
        sourceUrl = "",
        title = title,
        author = author,
        dynasty = dynasty,
        content = content,
        tags = tags,
        notes = notes,
        translation = translation,
        intro = intro,
        background = background
    )

    private fun PoemResponse.toUserPoem(isFavorite: Boolean = false) = UserPoem(
        poem = toPoem(),
        isFavorite = isFavorite
    )

    private fun PoemListItem.toPoem() = Poem(
        id = id,
        sourceUrl = "",
        title = title,
        author = author,
        dynasty = dynasty,
        content = content,
        tags = tags
    )

    private fun PoemListItem.toUserPoem(isFavorite: Boolean = false) = UserPoem(
        poem = toPoem(),
        isFavorite = isFavorite
    )

    private fun PoetResponse.toPoet() = Poet(
        id = id,
        name = name,
        dynasty = dynasty,
        avatarUrl = avatarUrl,
        lifetime = lifetime,
        descriptions = descriptions.map { PoetDescription(type = it.type, content = it.content) },
        poemCount = poemCount
    )

    private fun PoetListItem.toPoet() = Poet(
        id = id,
        name = name,
        dynasty = dynasty,
        lifetime = lifetime,
        poemCount = poemCount
    )
}