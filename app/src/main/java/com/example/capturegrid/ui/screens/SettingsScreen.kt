package com.example.capturegrid.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.capturegrid.data.AppSettings
import com.example.capturegrid.data.AppViewModel
import com.example.capturegrid.data.BoardTheme
import com.example.capturegrid.data.GameStats
import com.example.capturegrid.data.ThemeMode
import com.example.capturegrid.ui.components.Panel
import com.example.capturegrid.ui.components.ScreenBackground
import com.example.capturegrid.ui.components.ScreenHeader
import com.example.capturegrid.ui.components.SecondaryButton
import com.example.capturegrid.ui.components.SectionLabel
import com.example.capturegrid.ui.components.StatItem
import com.example.capturegrid.ui.theme.LocalGameColors
import com.example.capturegrid.ui.theme.boardPalette

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: GameStats,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Settings", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Panel(Modifier.fillMaxWidth()) {
                        SectionLabel("Appearance")
                        Spacer(Modifier.height(14.dp))
                        FieldLabel("Theme")
                        SegmentedControl(
                            options = listOf(ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark", ThemeMode.SYSTEM to "System"),
                            selected = settings.themeMode,
                            onSelect = viewModel::setThemeMode
                        )
                        Spacer(Modifier.height(16.dp))
                        FieldLabel("Board theme")
                        SegmentedControl(
                            options = listOf(BoardTheme.CLASSIC to "Classic", BoardTheme.MIDNIGHT to "Midnight"),
                            selected = settings.boardTheme,
                            onSelect = viewModel::setBoardTheme,
                            swatch = { theme ->
                                val palette = boardPalette(theme, dark = LocalGameColors.current.isDark)
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(palette.frame)
                                ) {
                                    Box(
                                        Modifier
                                            .padding(4.dp)
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(palette.trim)
                                    )
                                }
                            }
                        )
                    }
                    Panel(Modifier.fillMaxWidth()) {
                        SectionLabel("Gameplay")
                        Spacer(Modifier.height(6.dp))
                        SettingSwitch("Sound effects", null, settings.soundEnabled, viewModel::setSound)
                        RowDivider()
                        SettingSwitch("Haptic feedback", null, settings.hapticsEnabled, viewModel::setHaptics)
                        RowDivider()
                        SettingSwitch("Show last move", "Thin ring around the latest piece", settings.showLastMove, viewModel::setShowLastMove)
                        RowDivider()
                        SettingSwitch("Confirm pass", "Ask before passing a turn", settings.confirmPass, viewModel::setConfirmPass)
                    }
                    Panel(Modifier.fillMaxWidth()) {
                        SectionLabel("Statistics")
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth()) {
                            StatItem("${stats.gamesPlayed}", "Games", Modifier.weight(1f))
                            StatItem("${stats.player1Wins}", "P1 Wins", Modifier.weight(1f))
                            StatItem("${stats.player2Wins}", "P2 Wins", Modifier.weight(1f))
                            StatItem("${stats.draws}", "Draws", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        StatItem("${stats.mostCaptures}", "Most captures in one game", Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        SecondaryButton(
                            "Reset Statistics",
                            onClick = { confirmReset = true },
                            accent = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    if (confirmReset) {
        ConfirmDialog(
            title = "Reset statistics?",
            message = "Games played, wins, draws and records will be cleared. This cannot be undone.",
            confirmLabel = "Reset",
            onConfirm = {
                confirmReset = false
                viewModel.resetStats()
            },
            onDismiss = { confirmReset = false }
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    swatch: (@Composable (T) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .padding(4.dp)
                .selectableGroup()
        ) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                Row(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.18f) else Color.Transparent)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(value) }),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (swatch != null) {
                        swatch(value)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary,
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.surfaceVariant,
                uncheckedBorderColor = colors.outline
            )
        )
    }
}
