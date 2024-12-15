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
import com.example.rockit.data.repository.DataRepository
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

    @Inject private lateinit var dataRepository: DataRepository

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            localData = dataRepository.loadDataFromTest(this@RockItApp)
        }
    }


}