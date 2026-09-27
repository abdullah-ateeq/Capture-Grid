package com.example.capturegrid.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.capturegrid.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "capture_grid")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class BoardTheme { CLASSIC, MIDNIGHT }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLastMove: Boolean = true,
    val confirmPass: Boolean = true
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val draws: Int = 0,
    val mostCaptures: Int = 0
)

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val BOARD = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val LAST_MOVE = booleanPreferencesKey("show_last_move")
        val CONFIRM_PASS = booleanPreferencesKey("confirm_pass")
        val GAMES = intPreferencesKey("games_played")
        val P1_WINS = intPreferencesKey("p1_wins")
        val P2_WINS = intPreferencesKey("p2_wins")
        val DRAWS = intPreferencesKey("draws")
        val MOST_CAPTURES = intPreferencesKey("most_captures")
    }

    val settings: Flow<AppSettings> = store.data.map { p ->
        val defaults = AppSettings()
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.themeMode,
            boardTheme = p[Keys.BOARD]?.let { runCatching { BoardTheme.valueOf(it) }.getOrNull() } ?: defaults.boardTheme,
            soundEnabled = p[Keys.SOUND] ?: defaults.soundEnabled,
            hapticsEnabled = p[Keys.HAPTICS] ?: defaults.hapticsEnabled,
            showLastMove = p[Keys.LAST_MOVE] ?: defaults.showLastMove,
            confirmPass = p[Keys.CONFIRM_PASS] ?: defaults.confirmPass
        )
    }

    val stats: Flow<GameStats> = store.data.map { p ->
        GameStats(
            gamesPlayed = p[Keys.GAMES] ?: 0,
            player1Wins = p[Keys.P1_WINS] ?: 0,
            player2Wins = p[Keys.P2_WINS] ?: 0,
            draws = p[Keys.DRAWS] ?: 0,
            mostCaptures = p[Keys.MOST_CAPTURES] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.THEME] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.BOARD] = theme.name }
    suspend fun setSound(enabled: Boolean) = store.edit { it[Keys.SOUND] = enabled }
    suspend fun setHaptics(enabled: Boolean) = store.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLastMove(enabled: Boolean) = store.edit { it[Keys.LAST_MOVE] = enabled }
    suspend fun setConfirmPass(enabled: Boolean) = store.edit { it[Keys.CONFIRM_PASS] = enabled }

    suspend fun recordGame(winner: Player?, bestCaptureCount: Int) {
        store.edit { p ->
            p[Keys.GAMES] = (p[Keys.GAMES] ?: 0) + 1
            when (winner) {
                Player.PLAYER_ONE -> p[Keys.P1_WINS] = (p[Keys.P1_WINS] ?: 0) + 1
                Player.PLAYER_TWO -> p[Keys.P2_WINS] = (p[Keys.P2_WINS] ?: 0) + 1
                null -> p[Keys.DRAWS] = (p[Keys.DRAWS] ?: 0) + 1
            }
            p[Keys.MOST_CAPTURES] = maxOf(p[Keys.MOST_CAPTURES] ?: 0, bestCaptureCount)
        }
    }

    suspend fun resetStats() {
        store.edit { p ->
            p.remove(Keys.GAMES)
            p.remove(Keys.P1_WINS)
            p.remove(Keys.P2_WINS)
            p.remove(Keys.DRAWS)
            p.remove(Keys.MOST_CAPTURES)
        }
    }
}
