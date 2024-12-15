package com.example.rockit.player.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import com.example.rockit.Utils.Utils.logger
import kotlin.math.log

object AudioServiceState {

    var isAudioServiceRunning = false

    fun startAudioService(context: Context) {
        logger("starting service")
        if (isAudioServiceRunning.not()) {
            val intent = Intent(context, AppAudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            isAudioServiceRunning = true
        }
    }

//    fun stopAudioService(context: Context) {
//        if (isAudioServiceRunning) {
////            logger("stopping service task : ${context.stopService(Intent(context, AppAudioService::class.java))}")
//            isAudioServiceRunning = false
//        }
//    }
}