package com.example.rockit

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RockItApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}