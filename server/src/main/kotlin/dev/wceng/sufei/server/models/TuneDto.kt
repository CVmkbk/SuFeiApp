package dev.wceng.sufei.server.models

import kotlinx.serialization.Serializable

@Serializable
data class TuneDto(
    val name: String,
    val description: String? = null
)