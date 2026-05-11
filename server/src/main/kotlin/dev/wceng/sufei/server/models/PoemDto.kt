package dev.wceng.sufei.server.models

import kotlinx.serialization.Serializable

@Serializable
data class PoemDto(
    val id: String,
    val sourceUrl: String,
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