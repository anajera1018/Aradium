package com.aradium.aradium

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class SongSearchResponse(
    val resultCount: Int = 0,
    val results: List<SongInfo> = emptyList()
)

data class SongInfo(
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val primaryGenreName: String? = null,
    val releaseDate: String? = null,
    val artworkUrl100: String? = null
)

interface SongApi {
    @GET("search")
    suspend fun searchSong(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 1
    ): SongSearchResponse
}

object RetrofitInstance {
    val api: SongApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SongApi::class.java)
    }
}
