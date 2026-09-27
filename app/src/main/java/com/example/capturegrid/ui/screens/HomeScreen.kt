package com.example.capturegrid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capturegrid.data.GameStats
import com.example.capturegrid.game.GridPosition
import com.example.capturegrid.ui.components.MiniBoard
import com.example.capturegrid.ui.components.MiniMarker
import com.example.capturegrid.ui.components.Panel
import com.example.capturegrid.ui.components.PrimaryButton
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.SecondaryButton
import com.example.capturegrid.ui.components.StatItem

@Composable
fun HomeScreen(
    stats: GameStats,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    ScreenBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight && maxHeight < 600.dp
            val scroll = rememberScrollState()
            if (landscape) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) { Brand(emblemSize = 110) }
                    Column(
                        Modifier
                            .weight(1f)
                            .widthIn(max = 420.dp)
                            .verticalScroll(scroll),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Actions(onPlay, onHowToPlay, onSettings, onAbout)
                        StatsStrip(stats)
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .heightIn(min = maxHeight)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(Modifier.height(8.dp))
                    Brand(emblemSize = 132)
                    Column(
                        Modifier
                            .widthIn(max = 420.dp)
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Actions(onPlay, onHowToPlay, onSettings, onAbout)
                        Spacer(Modifier.height(12.dp))
                        StatsStrip(stats)
                    }
                }
            }
        }
    }
}

@Composable
private fun Brand(emblemSize: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MiniBoard(
            rows = listOf(".X.", "XOX", ".X."),
            description = "Emblem: a Player 2 piece surrounded by Player 1 pieces",
            markers = mapOf(GridPosition(1, 1) to MiniMarker.CAPTURED),
            modifier = Modifier.size(emblemSize.dp)
        )
        Spacer(Modifier.height(28.dp))
        Text(
            "CAPTURE GRID",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Surround. Capture. Control the grid.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Actions(
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    PrimaryButton("Play", onPlay, Modifier.fillMaxWidth())
    SecondaryButton("How to Play", onHowToPlay, Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("Settings", onSettings, Modifier.weight(1f))
        SecondaryButton("About", onAbout, Modifier.weight(1f))
    }
}

@Composable
private fun StatsStrip(stats: GameStats) {
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            StatItem("${stats.gamesPlayed}", "Games", Modifier.weight(1f))
            StatItem("${stats.player1Wins}", "P1 Wins", Modifier.weight(1f))
            StatItem("${stats.player2Wins}", "P2 Wins", Modifier.weight(1f))
            StatItem("${stats.draws}", "Draws", Modifier.weight(1f))
        }
    }
}
