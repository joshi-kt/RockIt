package com.example.rockit.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import com.example.rockit.Utils.Utils.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectivityRepository(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(isNetworkConnected())
    val isConnected = _isConnected.asStateFlow()

    private val callback : NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            _isConnected.value = true
        }

        override fun onLost(network: android.net.Network) {
            _isConnected.value = false
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    fun unregisterDefaultNetworkCallback() {
        logger("unregistering")
        connectivityManager.unregisterNetworkCallback(callback)
    }

    fun isNetworkConnected() : Boolean {
        return connectivityManager.activeNetwork != null
    }
}