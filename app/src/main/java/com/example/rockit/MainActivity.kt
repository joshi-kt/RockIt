package com.example.rockit

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rockit.Utils.Utils
import com.example.rockit.models.AllSongsScreen
import com.example.rockit.models.SongScreen
import com.example.rockit.navigation.SetupNavigation
import com.example.rockit.player.service.AudioServiceState
import com.example.rockit.ui.screens.AllSongsScreen
import com.example.rockit.ui.screens.SongScreen
import com.example.rockit.ui.theme.RockItTheme
import com.example.rockit.ui.viewmodels.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val baseViewModel by viewModels<BaseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {

                NotificationPermission()

            }

            SetupNavigation(baseViewModel)
        }
    }


    @SuppressLint("InlinedApi")
    @Composable
    private fun NotificationPermission() {
        var hasNotificationPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { result ->
            hasNotificationPermission = result
        }

        LaunchedEffect(true) {
            if (!hasNotificationPermission) {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioServiceState.stopAudioService(this)
    }
}