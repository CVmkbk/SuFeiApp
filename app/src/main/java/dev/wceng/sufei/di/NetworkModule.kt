package dev.wceng.sufei.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.wceng.sufei.data.network.TokenManager
import dev.wceng.sufei.data.network.api.AuthApiService
import dev.wceng.sufei.data.network.api.FavoriteApiService
import dev.wceng.sufei.data.network.api.PoemApiService
import dev.wceng.sufei.data.network.api.PoetApiService
import dev.wceng.sufei.data.network.api.ReferenceApiService
import dev.wceng.sufei.data.network.interceptor.AuthInterceptor
import dev.wceng.sufei.data.repository.NetworkPoemRepositoryImpl
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePoemApiService(retrofit: Retrofit): PoemApiService {
        return retrofit.create(PoemApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePoetApiService(retrofit: Retrofit): PoetApiService {
        return retrofit.create(PoetApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReferenceApiService(retrofit: Retrofit): ReferenceApiService {
        return retrofit.create(ReferenceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoriteApiService(retrofit: Retrofit): FavoriteApiService {
        return retrofit.create(FavoriteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkPoemRepository(
        poemApiService: PoemApiService,
        poetApiService: PoetApiService,
        referenceApiService: ReferenceApiService,
        favoriteApiService: FavoriteApiService,
        tokenManager: TokenManager
    ): NetworkPoemRepositoryImpl {
        return NetworkPoemRepositoryImpl(
            poemApiService = poemApiService,
            poetApiService = poetApiService,
            referenceApiService = referenceApiService,
            favoriteApiService = favoriteApiService,
            tokenManager = tokenManager
        )
    }
}