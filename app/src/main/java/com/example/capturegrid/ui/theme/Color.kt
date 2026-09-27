package com.example.capturegrid.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.capturegrid.data.BoardTheme

// App surfaces
val Graphite = Color(0xFF0E1013)
val GraphiteSurface = Color(0xFF16191E)
val GraphiteRaised = Color(0xFF1D2127)
val GraphiteOutline = Color(0xFF2C3139)
val Ivory = Color(0xFFE8E4DC)
val IvoryMuted = Color(0xFF9C988F)
val Bronze = Color(0xFFB8955E)
val BronzeDeep = Color(0xFF8A6A3A)

val Parchment = Color(0xFFF1ECE3)
val ParchmentSurface = Color(0xFFF9F6F0)
val ParchmentRaised = Color(0xFFE8E1D5)
val ParchmentOutline = Color(0xFFD2C8B7)
val Ink = Color(0xFF1C1D20)
val InkMuted = Color(0xFF625D55)

val ErrorMuted = Color(0xFFC2584E)

/** Colours for one player's tokens and UI accents. */
@Immutable
data class PlayerColors(
    val highlight: Color,
    val base: Color,
    val shade: Color,
    val symbol: Color,
    val accent: Color
)

val CrimsonCopper = PlayerColors(
    highlight = Color(0xFFD9845F),
    base = Color(0xFFA64B39),
    shade = Color(0xFF5E231B),
    symbol = Color(0xFFF6DCCB),
    accent = Color(0xFFC96B52)
)

val TealSapphire = PlayerColors(
    highlight = Color(0xFF5AA7B2),
    base = Color(0xFF1F6177),
    shade = Color(0xFF0B2E3B),
    symbol = Color(0xFFD3ECEF),
    accent = Color(0xFF4A9DAE)
)

/** Board frame, recessed cells and trim. */
@Immutable
data class BoardPalette(
    val frame: Color,
    val frameEdge: Color,
    val cellTop: Color,
    val cellBottom: Color,
    val cellRim: Color,
    val trim: Color,
    val marker: Color
)

fun boardPalette(theme: BoardTheme, dark: Boolean): BoardPalette = when (theme) {
    BoardTheme.CLASSIC -> if (dark) {
        BoardPalette(
            frame = Color(0xFF25221E),
            frameEdge = Color(0xFF3A342C),
            cellTop = Color(0xFF151310),
            cellBottom = Color(0xFF221F1B),
            cellRim = Color(0xFF34302A),
            trim = Bronze,
            marker = Color(0xFFE0C08A)
        )
    } else {
        BoardPalette(
            frame = Color(0xFFCDC2AE),
            frameEdge = Color(0xFFB3A68E),
            cellTop = Color(0xFFB5A890),
            cellBottom = Color(0xFFC9BEA9),
            cellRim = Color(0xFFDDD3C1),
            trim = BronzeDeep,
            marker = Color(0xFF6E4F22)
        )
    }
    BoardTheme.MIDNIGHT -> BoardPalette(
        frame = Color(0xFF161B24),
        frameEdge = Color(0xFF28303D),
        cellTop = Color(0xFF0B0E14),
        cellBottom = Color(0xFF151A23),
        cellRim = Color(0xFF232A36),
        trim = Color(0xFF7D8BA3),
        marker = Color(0xFFC9D3E6)
    )
}
