package com.example.capturegrid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val ControlShape = RoundedCornerShape(14.dp)
val PanelShape = RoundedCornerShape(18.dp)

@Composable
fun ScreenBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) { content() }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ControlShape,
        color = if (enabled) colors.primary else colors.surfaceVariant,
        contentColor = if (enabled) colors.onPrimary else colors.onSurfaceVariant,
        shadowElevation = if (enabled) 3.dp else 0.dp,
        modifier = modifier.heightIn(min = 56.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = MaterialTheme.colorScheme.onSurface
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ControlShape,
        color = colors.surface,
        contentColor = if (enabled) accent else colors.onSurfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (enabled) colors.outline else colors.outlineVariant),
        modifier = modifier.heightIn(min = 52.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.semantics { heading() }
    )
}

/** Circular 48dp icon button with a hand-drawn glyph (keeps the app free of icon libraries). */
@Composable
fun GlyphButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: Glyph
) {
    val tint = MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        modifier = modifier
            .size(48.dp)
            .semantics {
                contentDescription = description
                role = Role.Button
            }
    ) {
        Canvas(Modifier.padding(14.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            when (glyph) {
                Glyph.BACK -> drawPath(
                    Path().apply {
                        moveTo(w * 0.62f, h * 0.15f)
                        lineTo(w * 0.28f, h * 0.5f)
                        lineTo(w * 0.62f, h * 0.85f)
                    },
                    tint,
                    style = stroke
                )
                Glyph.MENU -> listOf(0.2f, 0.5f, 0.8f).forEach {
                    drawCircle(tint, 2.dp.toPx(), Offset(w / 2, h * it))
                }
            }
        }
    }
}

enum class Glyph { BACK, MENU }

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphButton(description = "Back", onClick = onBack, glyph = Glyph.BACK)
        Spacer(Modifier.width(4.dp))
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        actions()
    }
}

@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Small bronze diamond used as a bullet / ornament. */
@Composable
fun DiamondBullet(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier.size(8.dp)) {
        drawPath(
            Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(size.width / 2, size.height)
                lineTo(0f, size.height / 2)
                close()
            },
            color
        )
    }
}
