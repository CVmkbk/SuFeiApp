package dev.wceng.sufei.server.models

import kotlinx.serialization.Serializable

@Serializable
data class PoetDescriptionDto(
    val type: String,
    val content: String
)

@Serializable
data class PoetDto(
    val id: String,
    val name: String,
    val dynasty: String,
    val avatarUrl: String? = null,
    val lifetime: String? = null,
    val descriptions: List<PoetDescriptionDto> = emptyList()
)