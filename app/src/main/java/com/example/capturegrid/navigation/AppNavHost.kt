package com.example.capturegrid.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capturegrid.data.AppSettings
import com.example.capturegrid.data.AppViewModel
import com.example.capturegrid.game.GameViewModel
import com.example.capturegrid.ui.screens.AboutScreen
import com.example.capturegrid.ui.screens.GameScreen
import com.example.capturegrid.ui.screens.HomeScreen
import com.example.capturegrid.ui.screens.HowToPlayScreen
import com.example.capturegrid.ui.screens.ResultScreen
import com.example.capturegrid.ui.screens.SettingsScreen
import com.example.capturegrid.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun AppNavHost(
    appViewModel: AppViewModel,
    gameViewModel: GameViewModel,
    settings: AppSettings,
    navController: NavHostController = rememberNavController()
) {
    val stats by appViewModel.stats.collectAsStateWithLifecycle()

    fun goHome() {
        if (!navController.popBackStack(Routes.HOME, inclusive = false)) {
            navController.navigate(Routes.HOME) { popUpTo(0) }
        }
    }

    fun back() {
        if (!navController.popBackStack()) goHome()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                stats = stats,
                onPlay = {
                    gameViewModel.newGame()
                    navController.navigate(Routes.GAME) { launchSingleTop = true }
                },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                onExit = ::goHome,
                onGameFinished = {
                    if (navController.currentDestination?.route == Routes.GAME) {
                        navController.navigate(Routes.RESULT) {
                            popUpTo(Routes.GAME) { inclusive = true }
                        }
                    }
                },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.RESULT) {
            val ui by gameViewModel.uiState.collectAsStateWithLifecycle()
            // After process death the match is gone; there is nothing to show.
            LaunchedEffect(ui.game.isGameOver) {
                if (!ui.game.isGameOver) goHome()
            }
            ResultScreen(
                ui = ui,
                onPlayAgain = {
                    gameViewModel.newGame()
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.RESULT) { inclusive = true }
                    }
                },
                onHome = ::goHome
            )
        }
        composable(Routes.HOW_TO_PLAY) { HowToPlayScreen(onBack = ::back) }
        composable(Routes.SETTINGS) {
            SettingsScreen(settings = settings, stats = stats, viewModel = appViewModel, onBack = ::back)
        }
        composable(Routes.ABOUT) { AboutScreen(onBack = ::back) }
    }
}
