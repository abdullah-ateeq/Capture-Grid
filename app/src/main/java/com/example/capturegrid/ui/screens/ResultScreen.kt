package com.example.capturegrid.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capturegrid.game.CaptureGridState
import com.example.capturegrid.game.Decision
import com.example.capturegrid.game.EndReason
import com.example.capturegrid.game.GameUiState
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.components.MiniBoard
import com.example.capturegrid.ui.components.Panel
import com.example.capturegrid.ui.components.PlayerToken
import com.example.capturegrid.ui.components.PrimaryButton
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.SecondaryButton
import com.example.capturegrid.ui.theme.LocalGameColors

@Composable
fun ResultScreen(
    ui: GameUiState,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val game = ui.game
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }

    val title = when (game.winner) {
        null -> "DRAW"
        else -> "${game.winner.label.uppercase()} WINS"
    }
    val decision = when (game.decision) {
        Decision.CAPTURES -> "Decided by captures"
        Decision.PIECES_REMAINING -> "Captures level — decided by pieces remaining"
        Decision.DRAW, null -> "Captures and remaining pieces are level"
    }
    val ending = when (game.endReason) {
        EndReason.BOARD_FULL -> "Board full"
        EndReason.BOTH_PASSED, null -> "Both players passed"
    }

    ScreenBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth > 700.dp && maxWidth > maxHeight
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp)
                    .graphicsLayer {
                        alpha = entrance.value
                        translationY = (1f - entrance.value) * 24.dp.toPx()
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val content: @Composable (Modifier) -> Unit = { modifier ->
                    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                        if (game.winner != null) {
                            PlayerToken(game.winner, size = 64.dp)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                                PlayerToken(Player.PLAYER_ONE, size = 56.dp)
                                PlayerToken(Player.PLAYER_TWO, size = 56.dp)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.displayMedium,
                            color = game.winner?.let { LocalGameColors.current.of(it).accent }
                                ?: MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            decision,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        ScoreCard(game)
                        Spacer(Modifier.height(12.dp))
                        DetailsCard(
                            listOf(
                                "Total moves" to "${game.moveCount}",
                                "Match duration" to formatDuration(ui.durationMillis),
                                "Match ended" to ending
                            )
                        )
                    }
                }
                val finalBoard: @Composable (Modifier) -> Unit = { modifier ->
                    MiniBoard(
                        rows = game.board.map { row ->
                            row.joinToString("") {
                                when (it) {
                                    Player.PLAYER_ONE -> "X"
                                    Player.PLAYER_TWO -> "O"
                                    null -> "."
                                }
                            }
                        },
                        description = "Final board position",
                        modifier = modifier
                    )
                }
                if (wide) {
                    Row(
                        Modifier.widthIn(max = 900.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        content(Modifier.weight(1f))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            finalBoard(Modifier.widthIn(max = 320.dp).fillMaxWidth())
                            Spacer(Modifier.height(24.dp))
                            Buttons(onPlayAgain, onHome)
                        }
                    }
                } else {
                    Column(
                        Modifier.widthIn(max = 440.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        content(Modifier.fillMaxWidth())
                        Spacer(Modifier.height(20.dp))
                        finalBoard(Modifier.width(180.dp))
                        Spacer(Modifier.height(28.dp))
                        Buttons(onPlayAgain, onHome)
                    }
                }
            }
        }
    }
}

@Composable
private fun Buttons(onPlayAgain: () -> Unit, onHome: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton("Play Again", onPlayAgain, Modifier.fillMaxWidth())
        SecondaryButton("Home", onHome, Modifier.fillMaxWidth())
    }
}

@Composable
private fun ScoreCard(game: CaptureGridState) {
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PlayerScore(Player.PLAYER_ONE, game, Modifier.weight(1f))
            VerticalDivider(Modifier.height(72.dp), color = MaterialTheme.colorScheme.outlineVariant)
            PlayerScore(Player.PLAYER_TWO, game, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlayerScore(player: Player, game: CaptureGridState, modifier: Modifier = Modifier) {
    val captures = game.capturesOf(player)
    val pieces = game.piecesOf(player)
    val isWinner = game.winner == player
    Column(
        modifier.clearAndSetSemantics {
            contentDescription = "${player.label}, $captures captures, $pieces pieces remaining"
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerToken(player, size = 20.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                player.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "$captures",
            style = MaterialTheme.typography.displayMedium,
            color = if (isWinner) LocalGameColors.current.of(player).accent else MaterialTheme.colorScheme.onSurface
        )
        Text(
            if (captures == 1) "capture" else "captures",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "$pieces pieces remaining",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailsCard(rows: List<Pair<String, String>>) {
    Panel(Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
            Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) { }) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
