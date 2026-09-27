package com.example.capturegrid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.capturegrid.game.GridPosition
import com.example.capturegrid.ui.components.DiamondBullet
import com.example.capturegrid.ui.components.MiniBoard
import com.example.capturegrid.ui.components.MiniMarker
import com.example.capturegrid.ui.components.Panel
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.ScreenHeader
import com.example.capturegrid.ui.components.SectionLabel

private val Features = listOf(
    "Local 2 Player",
    "7×7 Strategy Grid",
    "Group Capture",
    "Offline Play",
    "Premium Board Themes"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("About", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.widthIn(max = 520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))
                    MiniBoard(
                        rows = listOf(".X.", "XOX", ".X."),
                        description = "Capture Grid emblem",
                        markers = mapOf(GridPosition(1, 1) to MiniMarker.CAPTURED),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Capture Grid",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Version 1.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "A polished local two-player strategy game where players surround rival groups, " +
                            "capture pieces, and compete for control.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(28.dp))
                    Panel(Modifier.fillMaxWidth()) {
                        SectionLabel("Features")
                        Spacer(Modifier.height(8.dp))
                        Features.forEach { feature ->
                            Row(
                                Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DiamondBullet()
                                Spacer(Modifier.width(14.dp))
                                Text(feature, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(
                            "No accounts. No ads. Fully offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
