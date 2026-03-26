package com.ourspace.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesSearchApi {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 10
    ): ItunesSearchResponse
}

data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<SongResult>
)

data class SongResult(
    val trackName: String,
    val artistName: String,
    val previewUrl: String?,
    val artworkUrl100: String?
)
