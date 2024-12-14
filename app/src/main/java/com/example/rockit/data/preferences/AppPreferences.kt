package com.example.rockit.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.rockit.Utils.Utils.APP_PREFERENCE_NAME
import com.example.rockit.Utils.Utils.AUDIO_QUALITY
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppPreferences {

    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val AUDIO_QUALITY_PREF = Pair(AUDIO_QUALITY,4)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(APP_PREFERENCE_NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    var audioQuality: Int
        get() = preferences.getInt(AUDIO_QUALITY_PREF.first, AUDIO_QUALITY_PREF.second)
        set(value) = preferences.edit {
            it.putInt(AUDIO_QUALITY_PREF.first, value)
        }
}