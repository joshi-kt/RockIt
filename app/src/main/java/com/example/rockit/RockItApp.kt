package com.example.rockit

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.rockit.Utils.Utils.logger
import com.example.rockit.data.preferences.AppPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

@HiltAndroidApp
class RockItApp : Application() {

    companion object {
        var localData : JsonObject? = null
    }

    @Inject lateinit var exoplayer : ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            localData = loadDataFromTest(this@RockItApp)
        }
        if (exoplayer.isReleased) {

            val audioAttributes = AudioAttributes
                .Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            exoplayer = ExoPlayer
                .Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .setTrackSelector(DefaultTrackSelector(this))
                .build()
        }
    }

    private fun loadDataFromTest(context: Context): JsonObject? {
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