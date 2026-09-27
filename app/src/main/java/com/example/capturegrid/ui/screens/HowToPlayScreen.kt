package com.example.capturegrid.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capturegrid.game.GridPosition
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.components.MiniBoard
import com.example.capturegrid.ui.components.MiniMarker
import com.example.capturegrid.ui.components.Panel
import com.example.capturegrid.ui.components.PlayerToken
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.ScreenHeader
import com.example.capturegrid.ui.components.SectionLabel

private val Rules = listOf(
    "Players take turns placing one piece in any empty cell. Player 1 moves first.",
    "Pieces touching up, down, left or right form a group. Diagonals do not connect.",
    "Empty cells directly beside a group are its liberties.",
    "A group with no liberties is captured and removed from the board.",
    "Each captured piece adds one point to your score. One move can capture several groups.",
    "You cannot place a piece that immediately traps your own group, unless that move captures first.",
    "You may pass instead of placing. Two consecutive passes end the match.",
    "Most captures wins. If tied, more pieces on the board wins. Otherwise it is a draw."
)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("How to Play", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) { }) {
                        PlayerToken(Player.PLAYER_ONE, 26.dp)
                        Text(" Player 1 · Diamond", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        PlayerToken(Player.PLAYER_TWO, 26.dp)
                        Text(" Player 2 · Circle", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Diagram(
                            rows = listOf(".....", ".XX..", ".X...", ".....", "....."),
                            markers = listOf(
                                GridPosition(0, 1), GridPosition(0, 2), GridPosition(1, 0), GridPosition(1, 3),
                                GridPosition(2, 0), GridPosition(2, 2), GridPosition(3, 1)
                            ).associateWith { MiniMarker.LIBERTY },
                            caption = "Group of 3\n7 liberties",
                            description = "Diagram: a Player 1 group of three pieces with seven liberties marked",
                            modifier = Modifier.weight(1f)
                        )
                        Diagram(
                            rows = listOf(".....", "..X..", ".XOX.", "..X..", "....."),
                            markers = mapOf(GridPosition(2, 2) to MiniMarker.CAPTURED),
                            caption = "No liberties\nCaptured",
                            description = "Diagram: a Player 2 piece surrounded on four sides is captured",
                            modifier = Modifier.weight(1f)
                        )
                        Diagram(
                            rows = listOf(".O...", "O....", ".....", ".....", "....."),
                            markers = mapOf(GridPosition(0, 0) to MiniMarker.BLOCKED),
                            caption = "Self-capture\nNot allowed",
                            description = "Diagram: Player 1 may not play in the corner between two Player 2 pieces",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Panel(Modifier.fillMaxWidth()) {
                        SectionLabel("Rules")
                        Spacer(Modifier.height(12.dp))
                        Rules.forEachIndexed { index, rule ->
                            Row(
                                Modifier
                                    .padding(vertical = 6.dp)
                                    .semantics(mergeDescendants = true) { }
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Diagram(
    rows: List<String>,
    markers: Map<GridPosition, MiniMarker>,
    caption: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        MiniBoard(rows, description, Modifier.fillMaxWidth(), markers)
        Spacer(Modifier.height(8.dp))
        Text(
            caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
