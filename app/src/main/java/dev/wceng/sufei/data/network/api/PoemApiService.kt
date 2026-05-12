package dev.wceng.sufei.data.network.api

import dev.wceng.sufei.data.network.dto.ApiResponse
import dev.wceng.sufei.data.network.dto.PagedResponse
import dev.wceng.sufei.data.network.dto.PoemListItem
import dev.wceng.sufei.data.network.dto.PoemResponse
import dev.wceng.sufei.data.network.dto.SearchResultResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PoemApiService {

    @GET("api/v1/poems")
    suspend fun getPoems(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PagedResponse<PoemListItem>>

    @GET("api/v1/poems/{id}")
    suspend fun getPoemById(@Path("id") id: String): ApiResponse<PoemResponse>

    @GET("api/v1/poems/random")
    suspend fun getRandomPoem(): ApiResponse<PoemResponse>

    @GET("api/v1/poems/random/high-quality")
    suspend fun getHighQualityRandomPoem(): ApiResponse<PoemResponse>

    @GET("api/v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("dynasty") dynasty: String? = null,
        @Query("tag") tag: String? = null,
        @Query("tune") tune: String? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<SearchResultResponse>
}