package com.example.rockit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rockit.models.AllSongsScreen
import com.example.rockit.models.SongScreen
import com.example.rockit.navigation.SetupNavigation
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
            SetupNavigation(baseViewModel)
        }
    }
}