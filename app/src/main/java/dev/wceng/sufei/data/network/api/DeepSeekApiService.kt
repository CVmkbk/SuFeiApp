package dev.wceng.sufei.data.network.api

import dev.wceng.sufei.data.network.dto.DeepSeekChatRequest
import dev.wceng.sufei.data.network.dto.DeepSeekChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApiService {

    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: DeepSeekChatRequest): DeepSeekChatResponse
}