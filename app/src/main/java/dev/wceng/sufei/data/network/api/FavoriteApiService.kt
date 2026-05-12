package dev.wceng.sufei.data.network.api

import dev.wceng.sufei.data.network.dto.ApiResponse
import dev.wceng.sufei.data.network.dto.FavoriteCheckResponse
import dev.wceng.sufei.data.network.dto.PagedResponse
import dev.wceng.sufei.data.network.dto.PoemListItem
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoriteApiService {

    @GET("api/v1/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PagedResponse<PoemListItem>>

    @POST("api/v1/favorites/{poemId}")
    suspend fun addFavorite(@Path("poemId") poemId: String): ApiResponse<Unit>

    @DELETE("api/v1/favorites/{poemId}")
    suspend fun removeFavorite(@Path("poemId") poemId: String): ApiResponse<Unit>

    @GET("api/v1/favorites/{poemId}/check")
    suspend fun checkFavorite(@Path("poemId") poemId: String): ApiResponse<FavoriteCheckResponse>
}