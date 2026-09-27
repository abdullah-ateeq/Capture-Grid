package com.example.capturegrid.game

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capturegrid.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val game: CaptureGridState = CaptureGridState(),
    /** Pieces currently playing their capture animation (already removed from the board). */
    val capturing: Map<GridPosition, Player> = emptyMap(),
    val invalidCell: GridPosition? = null,
    val hasLegalMove: Boolean = true,
    val durationMillis: Long = 0L
)

sealed interface GameEvent {
    data object Placed : GameEvent
    data class Captured(val count: Int) : GameEvent
    data object Invalid : GameEvent
    data object Passed : GameEvent
    data class GameOver(val winner: Player?) : GameEvent
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private var engine = CaptureGridEngine()
    private var startedAt = SystemClock.elapsedRealtime()
    private var invalidJob: Job? = null
    private var captureJob: Job? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = Channel<GameEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun newGame() {
        invalidJob?.cancel()
        captureJob?.cancel()
        engine = CaptureGridEngine()
        startedAt = SystemClock.elapsedRealtime()
        _uiState.value = GameUiState(game = engine.state)
    }

    fun onCellTapped(position: GridPosition) {
        when (val result = engine.placePiece(position)) {
            is PlacementResult.Placed -> {
                if (result.captured.isNotEmpty()) {
                    _events.trySend(GameEvent.Captured(result.captured.size))
                    animateCaptures(result.captured)
                } else {
                    _events.trySend(GameEvent.Placed)
                    publish()
                }
                if (result.gameOver) onGameOver()
            }
            is PlacementResult.Rejected -> if (result.reason == RejectReason.SELF_CAPTURE) {
                showInvalid(position)
            }
        }
    }

    fun onPass() {
        if (!engine.passTurn()) return
        _events.trySend(GameEvent.Passed)
        publish()
        if (engine.state.isGameOver) onGameOver()
    }

    /** Locks input while captured pieces pulse and fade, then releases it. */
    private fun animateCaptures(captured: Map<GridPosition, Player>) {
        engine.setAnimating(true)
        _uiState.update { it.copy(capturing = captured) }
        publish()
        captureJob = viewModelScope.launch {
            delay(CAPTURE_ANIMATION_MS)
            engine.setAnimating(false)
            _uiState.update { it.copy(capturing = emptyMap()) }
            publish()
        }
    }

    private fun showInvalid(position: GridPosition) {
        _events.trySend(GameEvent.Invalid)
        invalidJob?.cancel()
        _uiState.update { it.copy(invalidCell = position) }
        invalidJob = viewModelScope.launch {
            delay(INVALID_DISPLAY_MS)
            _uiState.update { it.copy(invalidCell = null) }
        }
    }

    private fun onGameOver() {
        val state = engine.state
        _events.trySend(GameEvent.GameOver(state.winner))
        viewModelScope.launch {
            repository.recordGame(state.winner, maxOf(state.player1Captures, state.player2Captures))
        }
    }

    private fun publish() {
        val state = engine.state
        _uiState.update {
            it.copy(
                game = state,
                hasLegalMove = state.isGameOver || engine.hasLegalMove(),
                durationMillis = SystemClock.elapsedRealtime() - startedAt
            )
        }
    }

    companion object {
        const val CAPTURE_ANIMATION_MS = 240L
        const val INVALID_DISPLAY_MS = 1100L
    }
}
