package com.example.rockit.data.repository

import android.content.Context
import com.example.rockit.RockItApp
import com.example.rockit.Utils.Utils.logger
import com.example.rockit.data.network.ApiService
import com.example.rockit.models.Song
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.nio.charset.Charset

class DataRepository(private val apiService: ApiService) {

    suspend fun getTopSongs(): List<Song> {
        val result = apiService.getTopSongPlayList()
        var body = result.body()
//        var body : JsonObject? = null
        if (body == null) {
            logger("loading data from local")
            body = RockItApp.localData
        }
        val playListObject = body?.getAsJsonObject("data")
        logger("top songs : $playListObject")
        val songs = mutableListOf<Song>()
        playListObject?.let {
            val songsJSONArray = it.getAsJsonArray("songs")
            addSongsFromSongsObject(songs, songsJSONArray)
        }
        return songs
    }

    suspend fun getSearchedSongs(searchText: String): List<Song> {
        val result = apiService.searchSongs(searchText)
        val body = result.body()
        val resultDataObject = body?.getAsJsonObject("data")
        logger("searched songs : $resultDataObject")
        val songs = mutableListOf<Song>()
        resultDataObject?.let {
            val songsJSONArray = it.getAsJsonArray("results")
            addSongsFromSongsObject(songs, songsJSONArray)
        }
        return songs
    }

    private fun addSongsFromSongsObject(
        songs: MutableList<Song>,
        songsJSONArray: JsonArray
    ) {
        for (index in 0..<songsJSONArray.size()) {
            val songObject = songsJSONArray.get(index)
            try {
                val song = Gson().fromJson(songObject.toString(), Song::class.java)
                songs.add(song)
            } catch (e: Exception) {
                logger("JSON conversion failed for : $songObject")
            }
        }
    }

    fun loadDataFromTest(context: Context): JsonObject? {
        val json: String
        try {
            val inputStream = context.assets.open("data.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
            return Gson().fromJson(json, JsonObject::class.java)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

}