package dev.wceng.sufei.data.network.api

import dev.wceng.sufei.data.network.dto.ApiResponse
import dev.wceng.sufei.data.network.dto.TagResponse
import dev.wceng.sufei.data.network.dto.TuneResponse
import retrofit2.http.GET

interface ReferenceApiService {

    @GET("api/v1/tags")
    suspend fun getTags(): ApiResponse<List<TagResponse>>

    @GET("api/v1/tunes")
    suspend fun getTunes(): ApiResponse<List<TuneResponse>>
}