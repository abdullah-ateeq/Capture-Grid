package com.example.capturegrid

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capturegrid.data.AppSettings
import com.example.capturegrid.data.AppViewModel
import com.example.capturegrid.data.ThemeMode
import com.example.capturegrid.feedback.FeedbackController
import com.example.capturegrid.feedback.LocalFeedback
import com.example.capturegrid.feedback.SoundEffects
import com.example.capturegrid.game.GameViewModel
import com.example.capturegrid.navigation.AppNavHost
import com.example.capturegrid.ui.theme.CaptureGridTheme
import com.example.capturegrid.ui.theme.Graphite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val gameViewModel: GameViewModel = viewModel()
            val loaded by appViewModel.settings.collectAsStateWithLifecycle()
            val settings = loaded

            if (settings == null) {
                // Settings load in a few milliseconds; avoid flashing the wrong theme.
                Box(Modifier.fillMaxSize().background(Graphite))
            } else {
                AppContent(appViewModel, gameViewModel, settings)
            }
        }
    }

    @Composable
    private fun AppContent(appViewModel: AppViewModel, gameViewModel: GameViewModel, settings: AppSettings) {
        val dark = when (settings.themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
        LaunchedEffect(dark) {
            val style = if (dark) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }

        val view = LocalView.current
        val sounds = remember { SoundEffects(applicationContext) }
        DisposableEffect(sounds) { onDispose { sounds.release() } }
        val currentSettings by rememberUpdatedState(settings)
        val feedback = remember(view, sounds) {
            FeedbackController(
                sounds = sounds,
                view = view,
                soundEnabled = { currentSettings.soundEnabled },
                hapticsEnabled = { currentSettings.hapticsEnabled }
            )
        }

        CaptureGridTheme(darkTheme = dark, boardTheme = settings.boardTheme) {
            CompositionLocalProvider(LocalFeedback provides feedback) {
                AppNavHost(appViewModel, gameViewModel, settings)
            }
        }
    }
}
