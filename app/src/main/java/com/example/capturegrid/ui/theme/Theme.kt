package com.example.capturegrid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.capturegrid.data.BoardTheme
import com.example.capturegrid.game.Player

private val DarkColors = darkColorScheme(
    primary = Bronze,
    onPrimary = Color(0xFF1B150B),
    secondary = Color(0xFF8C7A5B),
    onSecondary = Graphite,
    background = Graphite,
    onBackground = Ivory,
    surface = GraphiteSurface,
    onSurface = Ivory,
    surfaceVariant = GraphiteRaised,
    onSurfaceVariant = IvoryMuted,
    surfaceContainerHigh = GraphiteRaised,
    outline = GraphiteOutline,
    outlineVariant = Color(0xFF23272E),
    error = ErrorMuted,
    onError = Color.White
)

private val LightColors = lightColorScheme(
    primary = BronzeDeep,
    onPrimary = Color.White,
    secondary = Color(0xFF7A6547),
    onSecondary = Color.White,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentSurface,
    onSurface = Ink,
    surfaceVariant = ParchmentRaised,
    onSurfaceVariant = InkMuted,
    surfaceContainerHigh = ParchmentSurface,
    outline = ParchmentOutline,
    outlineVariant = Color(0xFFE2DACC),
    error = Color(0xFFA8433A),
    onError = Color.White
)

data class GameColors(
    val playerOne: PlayerColors,
    val playerTwo: PlayerColors,
    val board: BoardPalette,
    val isDark: Boolean
) {
    fun of(player: Player): PlayerColors = if (player == Player.PLAYER_ONE) playerOne else playerTwo
}

val LocalGameColors = staticCompositionLocalOf {
    GameColors(CrimsonCopper, TealSapphire, boardPalette(BoardTheme.CLASSIC, true), true)
}

@Composable
fun CaptureGridTheme(
    darkTheme: Boolean,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val gameColors = GameColors(CrimsonCopper, TealSapphire, boardPalette(boardTheme, darkTheme), darkTheme)
    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}
