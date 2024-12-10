package com.example.rockit.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rockit.models.AllSongsScreen
import com.example.rockit.models.SongScreen
import com.example.rockit.ui.screens.AllSongsScreen
import com.example.rockit.ui.screens.SongScreen
import com.example.rockit.ui.viewmodels.BaseViewModel

@Composable
fun SetupNavigation(baseViewModel : BaseViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = AllSongsScreen) {
        composable<AllSongsScreen>{
            AllSongsScreen(
                viewModel = baseViewModel,
                navController = navController
            )
        }
        composable<SongScreen>(
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(1000)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(1000)
                )
            }
        ) {
            SongScreen(
                viewModel = baseViewModel,
                navController = navController
            )
        }
    }
}