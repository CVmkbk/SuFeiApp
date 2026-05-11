package dev.wceng.sufei.server.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = 200,
    val message: String = "success",
    val data: T? = null
)

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val totalPages: Int
)

@Serializable
data class PoemResponse(
    val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val translation: String? = null,
    val intro: String? = null,
    val background: String? = null
)

@Serializable
data class PoemListItem(
    val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val content: String,
    val tags: List<String> = emptyList()
)

@Serializable
data class PoetResponse(
    val id: String,
    val name: String,
    val dynasty: String,
    val avatarUrl: String? = null,
    val lifetime: String? = null,
    val descriptions: List<PoetDescriptionDto> = emptyList(),
    val poemCount: Int = 0
)

@Serializable
data class PoetListItem(
    val id: String,
    val name: String,
    val dynasty: String,
    val lifetime: String? = null,
    val poemCount: Int = 0
)

@Serializable
data class TagResponse(
    val name: String
)

@Serializable
data class TuneResponse(
    val name: String,
    val description: String? = null
)

@Serializable
data class SearchResultResponse(
    val poems: List<PoemListItem> = emptyList(),
    val poets: List<PoetListItem> = emptyList()
)

