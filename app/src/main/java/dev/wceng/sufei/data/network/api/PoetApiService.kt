package dev.wceng.sufei.data.network.api

import dev.wceng.sufei.data.network.dto.ApiResponse
import dev.wceng.sufei.data.network.dto.PagedResponse
import dev.wceng.sufei.data.network.dto.PoemListItem
import dev.wceng.sufei.data.network.dto.PoetListItem
import dev.wceng.sufei.data.network.dto.PoetResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PoetApiService {

    @GET("api/v1/poets")
    suspend fun getPoets(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PagedResponse<PoetListItem>>

    @GET("api/v1/poets/{id}")
    suspend fun getPoetById(@Path("id") id: String): ApiResponse<PoetResponse>

    @GET("api/v1/poets/top")
    suspend fun getTopPoets(
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<PoetListItem>>

    @GET("api/v1/poets/search")
    suspend fun searchPoets(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<PoetListItem>>

    @GET("api/v1/poets/{id}/poems")
    suspend fun getPoemsByPoet(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PagedResponse<PoemListItem>>
}