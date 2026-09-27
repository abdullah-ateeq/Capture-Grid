package com.example.capturegrid.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.drawCellTile
import com.example.capturegrid.ui.components.drawToken
import com.example.capturegrid.ui.theme.LocalGameColors

private const val SPLASH_MS = 1600f

private data class SplashPiece(val row: Int, val col: Int, val player: Player, val at: Float)

private val SplashPieces = listOf(
    SplashPiece(2, 2, Player.PLAYER_TWO, 220f),
    SplashPiece(1, 2, Player.PLAYER_ONE, 360f),
    SplashPiece(2, 1, Player.PLAYER_ONE, 470f),
    SplashPiece(3, 2, Player.PLAYER_ONE, 580f),
    SplashPiece(2, 3, Player.PLAYER_ONE, 690f)
)
private const val CAPTURE_AT = 760f

private fun phase(t: Float, start: Float, duration: Float) = ((t - start) / duration).coerceIn(0f, 1f)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val done by rememberUpdatedState(onFinished)
    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        time.animateTo(SPLASH_MS, tween(SPLASH_MS.toInt(), easing = LinearEasing))
        done()
    }
    val gameColors = LocalGameColors.current
    val palette = gameColors.board
    val measurer = rememberTextMeasurer()
    val scoreStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)

    ScreenBackground {
        Box(
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Capture Grid" },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Canvas(Modifier.size(180.dp)) {
                    val t = time.value
                    val n = 5
                    val gridAlpha = phase(t, 0f, 280f)
                    val pad = size.minDimension * 0.05f
                    val cell = (size.minDimension - pad * 2) / n
                    val gap = cell * 0.08f
                    drawRoundRect(palette.frame, alpha = gridAlpha, cornerRadius = CornerRadius(pad * 1.5f))
                    drawRoundRect(
                        palette.frameEdge,
                        alpha = gridAlpha,
                        cornerRadius = CornerRadius(pad * 1.5f),
                        style = Stroke(1.5f)
                    )
                    if (gridAlpha > 0.05f) {
                        for (r in 0 until n) for (c in 0 until n) {
                            val delay = (r + c) * 18f
                            if (phase(t, delay, 200f) > 0f) {
                                drawCellTile(
                                    palette,
                                    Offset(pad + c * cell + gap / 2, pad + r * cell + gap / 2),
                                    Size(cell - gap, cell - gap),
                                    cell * 0.16f
                                )
                            }
                        }
                    }
                    for (piece in SplashPieces) {
                        val appear = phase(t, piece.at, 150f)
                        if (appear <= 0f) continue
                        var scale = 0.55f + 0.45f * appear
                        var alpha = appear
                        if (piece.player == Player.PLAYER_TWO) {
                            val pulse = phase(t, CAPTURE_AT, 70f)
                            val fade = phase(t, CAPTURE_AT + 70f, 170f)
                            scale *= 1f + 0.14f * pulse - 0.75f * fade
                            alpha *= 1f - fade
                        }
                        val center = Offset(
                            pad + piece.col * cell + cell / 2,
                            pad + piece.row * cell + cell / 2
                        )
                        drawToken(piece.player, gameColors.of(piece.player), center, cell * 0.36f * scale, alpha)
                    }
                    // Score pulse rising from the captured cell.
                    val score = phase(t, CAPTURE_AT + 90f, 420f)
                    if (score > 0f && score < 1f) {
                        val layout = measurer.measure("+1", scoreStyle)
                        val alpha = if (score < 0.3f) score / 0.3f else 1f - (score - 0.3f) / 0.7f
                        drawText(
                            layout,
                            topLeft = Offset(
                                size.width / 2 - layout.size.width / 2,
                                pad + 2 * cell - cell * 0.2f - score * cell * 0.7f - layout.size.height / 2
                            ),
                            alpha = alpha.coerceIn(0f, 1f)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        val a = phase(time.value, 900f, 380f)
                        alpha = a
                        translationY = (1f - a) * 12.dp.toPx()
                    }
                ) {
                    Text(
                        "CAPTURE GRID",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
