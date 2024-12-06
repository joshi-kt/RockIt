package com.example.rockit.data.network

import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/playlists")
    suspend fun getTopSongPlayList(
        @Query("id") id: String = "1134543272",
        @Query("limit") limit : Int = 50,
    ): Response<JsonObject>

    @GET("api/search/songs")
    suspend fun searchSongs(
        @Query("query") searchText: String,
        @Query("limit") limit : Int = 50,
    ): Response<JsonObject>

}