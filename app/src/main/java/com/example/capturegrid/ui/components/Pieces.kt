package com.example.capturegrid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.theme.BoardPalette
import com.example.capturegrid.ui.theme.LocalGameColors
import com.example.capturegrid.ui.theme.PlayerColors

/** Raised, bevelled token with an engraved player symbol (diamond / circle). */
fun DrawScope.drawToken(
    player: Player,
    colors: PlayerColors,
    center: Offset,
    radius: Float,
    alpha: Float = 1f
) {
    if (alpha <= 0f || radius <= 0f) return
    // Soft contact shadow
    drawCircle(Color.Black.copy(alpha = 0.20f * alpha), radius * 1.05f, center + Offset(0f, radius * 0.16f))
    drawCircle(Color.Black.copy(alpha = 0.18f * alpha), radius, center + Offset(0f, radius * 0.07f))
    // Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors.highlight, colors.base, colors.shade),
            center = center + Offset(-radius * 0.35f, -radius * 0.45f),
            radius = radius * 1.75f
        ),
        radius = radius,
        center = center,
        alpha = alpha
    )
    // Bevel: light upper edge, dark lower edge
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.38f), Color.Transparent, Color.Black.copy(alpha = 0.32f)),
            startY = center.y - radius,
            endY = center.y + radius
        ),
        radius = radius * 0.95f,
        center = center,
        alpha = alpha,
        style = Stroke(width = radius * 0.08f)
    )
    // Engraved inner face
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = 0.30f), Color.White.copy(alpha = 0.16f)),
            startY = center.y - radius * 0.7f,
            endY = center.y + radius * 0.7f
        ),
        radius = radius * 0.68f,
        center = center,
        alpha = alpha,
        style = Stroke(width = radius * 0.05f)
    )
    val symbolColor = colors.symbol.copy(alpha = 0.92f * alpha)
    when (player) {
        Player.PLAYER_ONE -> {
            val s = radius * 0.36f
            val diamond = Path().apply {
                moveTo(center.x, center.y - s)
                lineTo(center.x + s * 0.82f, center.y)
                lineTo(center.x, center.y + s)
                lineTo(center.x - s * 0.82f, center.y)
                close()
            }
            drawPath(diamond, symbolColor)
        }
        Player.PLAYER_TWO -> drawCircle(
            color = symbolColor,
            radius = radius * 0.27f,
            center = center,
            style = Stroke(width = radius * 0.13f)
        )
    }
}

/** Recessed stone cell. */
fun DrawScope.drawCellTile(palette: BoardPalette, topLeft: Offset, size: Size, corner: Float) {
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(palette.cellTop, palette.cellBottom),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(corner)
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent, palette.cellRim),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(corner),
        style = Stroke(width = 1.2f.coerceAtLeast(size.minDimension * 0.025f))
    )
}

/** Decorative player token, hidden from accessibility (callers describe the player in text). */
@Composable
fun PlayerToken(player: Player, size: Dp = 28.dp, modifier: Modifier = Modifier) {
    val colors = LocalGameColors.current.of(player)
    Canvas(modifier.size(size).clearAndSetSemantics { }) {
        drawToken(player, colors, center, this.size.minDimension * 0.44f)
    }
}
