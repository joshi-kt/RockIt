package com.example.rockit

import android.app.Application
import android.content.Context
import android.os.Build
import com.example.rockit.Utils.Utils.logger
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

@HiltAndroidApp
class RockItApp : Application() {

    companion object {
        var localData : JsonObject? = null
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            GlobalScope.launch(Dispatchers.IO) {
                localData = loadDataFromTest(this@RockItApp)
                logger("local data $localData")
            }
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