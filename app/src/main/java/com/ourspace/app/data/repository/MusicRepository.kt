package com.ourspace.app.data.repository

import com.ourspace.app.data.api.ItunesSearchApi
import com.ourspace.app.data.api.SongResult
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MusicRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(ItunesSearchApi::class.java)

    suspend fun searchSongs(term: String): List<SongResult> {
        return try {
            if (term.isBlank()) return emptyList()
            val response = api.searchSongs(term)
            response.results
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "Error searching songs: ${e.message}")
            emptyList()
        }
    }
}
