package com.example.capturegrid.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    /** Null until the first DataStore read completes. */
    val settings: StateFlow<AppSettings?> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val stats: StateFlow<GameStats> =
        repository.stats.stateIn(viewModelScope, SharingStarted.Eagerly, GameStats())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = launch { repository.setBoardTheme(theme) }
    fun setSound(enabled: Boolean) = launch { repository.setSound(enabled) }
    fun setHaptics(enabled: Boolean) = launch { repository.setHaptics(enabled) }
    fun setShowLastMove(enabled: Boolean) = launch { repository.setShowLastMove(enabled) }
    fun setConfirmPass(enabled: Boolean) = launch { repository.setConfirmPass(enabled) }
    fun resetStats() = launch { repository.resetStats() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
