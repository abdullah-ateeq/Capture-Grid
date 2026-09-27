package com.example.capturegrid.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.capturegrid.data.AppSettings
import com.example.capturegrid.feedback.Haptic
import com.example.capturegrid.feedback.LocalFeedback
import com.example.capturegrid.feedback.Sfx
import com.example.capturegrid.game.GameEvent
import com.example.capturegrid.game.GameUiState
import com.example.capturegrid.game.GameViewModel
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.components.Glyph
import com.example.capturegrid.ui.components.GlyphButton
import com.example.capturegrid.ui.components.PlayerToken
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.SecondaryButton
import com.example.capturegrid.ui.theme.LocalGameColors
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: AppSettings,
    onExit: () -> Unit,
    onGameFinished: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback = LocalFeedback.current
    val finish by rememberUpdatedState(onGameFinished)

    var showPassDialog by rememberSaveable { mutableStateOf(false) }
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                GameEvent.Placed -> {
                    feedback.sound(Sfx.PLACE)
                    feedback.haptic(Haptic.PLACE)
                }
                is GameEvent.Captured -> {
                    feedback.sound(Sfx.CAPTURE)
                    feedback.haptic(Haptic.CAPTURE)
                }
                GameEvent.Invalid -> {
                    feedback.sound(Sfx.INVALID)
                    feedback.haptic(Haptic.INVALID)
                }
                GameEvent.Passed -> {
                    feedback.sound(Sfx.PASS)
                    feedback.haptic(Haptic.PLACE)
                }
                is GameEvent.GameOver -> {
                    feedback.sound(Sfx.GAME_END)
                    feedback.haptic(if (event.winner != null) Haptic.SUCCESS else Haptic.CAPTURE)
                }
            }
        }
    }

    LaunchedEffect(ui.game.isGameOver) {
        if (ui.game.isGameOver) {
            delay(900)
            finish()
        }
    }

    val matchInProgress = !ui.game.isGameOver && (ui.game.moveCount > 0 || ui.game.consecutivePasses > 0)
    val requestExit = { if (matchInProgress) showLeaveDialog = true else onExit() }
    BackHandler(enabled = matchInProgress) { showLeaveDialog = true }

    val requestPass = {
        if (!ui.game.isGameOver && !ui.game.isAnimating) {
            if (settings.confirmPass) showPassDialog = true else viewModel.onPass()
        }
    }

    ScreenBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            val sideWidth = if (maxWidth > 840.dp) 380.dp else 320.dp
            val topBar: @Composable () -> Unit = {
                GameTopBar(
                    onBack = requestExit,
                    onRestart = { showRestartDialog = true },
                    onHowToPlay = onHowToPlay,
                    onSettings = onSettings,
                    onExit = requestExit
                )
            }
            val board: @Composable (Modifier) -> Unit = { modifier ->
                GameBoard(
                    board = ui.game.board,
                    capturing = ui.capturing,
                    lastMove = ui.game.lastMove,
                    showLastMove = settings.showLastMove,
                    invalidCell = ui.invalidCell,
                    currentPlayer = ui.game.currentPlayer,
                    enabled = !ui.game.isGameOver && !ui.game.isAnimating,
                    onCellTap = viewModel::onCellTapped,
                    modifier = modifier
                )
            }
            if (landscape) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) { board(Modifier) }
                    Spacer(Modifier.width(20.dp))
                    Box(
                        Modifier
                            .width(sideWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            topBar()
                            PlayerPanel(Player.PLAYER_TWO, ui)
                            StatusLine(ui, Modifier.fillMaxWidth())
                            PlayerPanel(Player.PLAYER_ONE, ui)
                            PassButton(ui, requestPass)
                        }
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                ) {
                    topBar()
                    PlayerPanel(Player.PLAYER_TWO, ui)
                    StatusLine(
                        ui,
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { board(Modifier) }
                    Spacer(Modifier.height(14.dp))
                    PlayerPanel(Player.PLAYER_ONE, ui)
                    Spacer(Modifier.height(10.dp))
                    PassButton(ui, requestPass)
                }
            }
        }
    }

    if (showPassDialog) {
        ConfirmDialog(
            title = "Pass this turn?",
            message = if (ui.game.consecutivePasses == 1) {
                "Your opponent just passed. Passing now ends the match."
            } else {
                "${ui.game.currentPlayer.label} will skip placing a piece."
            },
            confirmLabel = "Pass",
            onConfirm = {
                showPassDialog = false
                viewModel.onPass()
            },
            onDismiss = { showPassDialog = false }
        )
    }
    if (showLeaveDialog) {
        ConfirmDialog(
            title = "Leave match?",
            message = "The current match will be abandoned and not counted in statistics.",
            confirmLabel = "Leave",
            onConfirm = {
                showLeaveDialog = false
                onExit()
            },
            onDismiss = { showLeaveDialog = false }
        )
    }
    if (showRestartDialog) {
        ConfirmDialog(
            title = "Restart match?",
            message = "The board and capture scores will be cleared.",
            confirmLabel = "Restart",
            onConfirm = {
                showRestartDialog = false
                viewModel.newGame()
            },
            onDismiss = { showRestartDialog = false }
        )
    }
}

@Composable
private fun GameTopBar(
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphButton(description = "Leave match", onClick = onBack, glyph = Glyph.BACK)
        Text(
            "CAPTURE GRID",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Box {
            GlyphButton(description = "Game menu", onClick = { menuOpen = true }, glyph = Glyph.MENU)
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                listOf(
                    "Restart match" to onRestart,
                    "How to play" to onHowToPlay,
                    "Settings" to onSettings,
                    "Exit to home" to onExit
                ).forEach { (label, action) ->
                    DropdownMenuItem(
                        text = { Text(label, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            menuOpen = false
                            action()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerPanel(player: Player, ui: GameUiState, modifier: Modifier = Modifier) {
    val game = ui.game
    val colors = LocalGameColors.current.of(player)
    val active = !game.isGameOver && game.currentPlayer == player
    val captures = game.capturesOf(player)
    val pieces = game.piecesOf(player)
    val pulse = remember { Animatable(1f) }
    val lastCaptures = remember { intArrayOf(captures) }
    LaunchedEffect(captures) {
        if (captures > lastCaptures[0]) {
            pulse.animateTo(1.28f, tween(110))
            pulse.animateTo(1f, tween(220))
        }
        lastCaptures[0] = captures
    }
    val captureWord = if (captures == 1) "capture" else "captures"
    val description = "${player.label} capture score $captures, $pieces pieces on board" +
        if (active) ", to move" else ""

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (active) 1.5.dp else 1.dp,
            if (active) colors.accent else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerToken(player, size = 34.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        player.label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (active) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = colors.accent.copy(alpha = 0.16f),
                            contentColor = colors.accent
                        ) {
                            Text(
                                "TO MOVE",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    "$pieces on board",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$captures",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (active) colors.accent else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulse.value
                        scaleY = pulse.value
                    }
                )
                Text(
                    captureWord.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusLine(ui: GameUiState, modifier: Modifier = Modifier) {
    val game = ui.game
    val invalid = ui.invalidCell != null
    val (headline, detail) = when {
        invalid -> "INVALID MOVE" to "That placement would leave your group with no liberties."
        game.isGameOver -> "MATCH COMPLETE" to ""
        !ui.hasLegalMove -> "${game.currentPlayer.label.uppercase()}'S TURN" to "No legal placements. Pass to continue."
        game.consecutivePasses == 1 -> "${game.currentPlayer.label.uppercase()}'S TURN" to
            "${game.currentPlayer.opponent.label} passed. Another pass ends the match."
        else -> "${game.currentPlayer.label.uppercase()}'S TURN" to ""
    }
    Column(
        modifier
            .heightIn(min = 44.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!invalid && !game.isGameOver) {
                PlayerToken(game.currentPlayer, size = 18.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                headline,
                style = MaterialTheme.typography.labelLarge,
                color = if (invalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
        }
        if (detail.isNotEmpty()) {
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PassButton(ui: GameUiState, onPass: () -> Unit) {
    val highlight = !ui.hasLegalMove && !ui.game.isGameOver
    SecondaryButton(
        text = "Pass",
        onClick = onPass,
        enabled = !ui.game.isGameOver && !ui.game.isAnimating,
        accent = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel.uppercase(), style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "CANCEL",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
