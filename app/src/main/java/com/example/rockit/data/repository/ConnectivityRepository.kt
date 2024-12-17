package com.example.rockit.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import com.example.rockit.Utils.Utils.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ConnectivityRepository(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observeConnectivity() = callbackFlow {
        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose {
            logger("unregistering")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    fun isNetworkConnected() : Boolean {
        return connectivityManager.activeNetwork != null
    }
}