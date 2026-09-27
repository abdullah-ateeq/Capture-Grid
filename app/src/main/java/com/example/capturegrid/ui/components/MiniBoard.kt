package com.example.capturegrid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.capturegrid.game.GridPosition
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.theme.LocalGameColors

enum class MiniMarker { LIBERTY, CAPTURED, BLOCKED }

/**
 * Static, non-interactive board used for diagrams and the final position.
 * [rows] uses 'X' for Player 1, 'O' for Player 2 and '.' for empty.
 */
@Composable
fun MiniBoard(
    rows: List<String>,
    description: String,
    modifier: Modifier = Modifier,
    markers: Map<GridPosition, MiniMarker> = emptyMap()
) {
    val gameColors = LocalGameColors.current
    val palette = gameColors.board
    val n = rows.size
    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
    ) {
        val pad = size.minDimension * 0.045f
        drawRoundRect(palette.frame, cornerRadius = CornerRadius(pad * 1.4f))
        drawRoundRect(palette.frameEdge, cornerRadius = CornerRadius(pad * 1.4f), style = Stroke(1.5f))
        val cell = (size.minDimension - pad * 2) / n
        val gap = cell * 0.07f
        for (r in 0 until n) for (c in 0 until n) {
            val topLeft = Offset(pad + c * cell + gap / 2, pad + r * cell + gap / 2)
            val tile = Size(cell - gap, cell - gap)
            drawCellTile(palette, topLeft, tile, cell * 0.14f)
            val center = topLeft + Offset(tile.width / 2, tile.height / 2)
            val owner = when (rows[r].getOrNull(c)) {
                'X' -> Player.PLAYER_ONE
                'O' -> Player.PLAYER_TWO
                else -> null
            }
            if (owner != null) {
                drawToken(owner, gameColors.of(owner), center, cell * 0.36f)
            }
            when (markers[GridPosition(r, c)]) {
                MiniMarker.LIBERTY -> drawCircle(palette.marker, cell * 0.09f, center)
                MiniMarker.CAPTURED -> drawCircle(
                    palette.marker,
                    cell * 0.43f,
                    center,
                    style = Stroke(
                        width = cell * 0.05f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(cell * 0.12f, cell * 0.08f))
                    )
                )
                MiniMarker.BLOCKED -> {
                    val s = cell * 0.16f
                    val stroke = cell * 0.06f
                    drawLine(palette.marker, center - Offset(s, s), center + Offset(s, s), stroke, StrokeCap.Round)
                    drawLine(palette.marker, center - Offset(s, -s), center + Offset(s, -s), stroke, StrokeCap.Round)
                }
                null -> Unit
            }
        }
    }
}
