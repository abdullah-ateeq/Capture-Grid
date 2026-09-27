package com.example.capturegrid.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.capturegrid.game.Board
import com.example.capturegrid.game.BoardRules
import com.example.capturegrid.game.GridPosition
import com.example.capturegrid.game.GroupInfo
import com.example.capturegrid.game.Player
import com.example.capturegrid.ui.components.drawCellTile
import com.example.capturegrid.ui.components.drawToken
import com.example.capturegrid.ui.theme.LocalGameColors
import kotlinx.coroutines.launch

private val BoardShape = RoundedCornerShape(20.dp)

@Composable
fun GameBoard(
    board: Board,
    capturing: Map<GridPosition, Player>,
    lastMove: GridPosition?,
    showLastMove: Boolean,
    invalidCell: GridPosition?,
    currentPlayer: Player,
    enabled: Boolean,
    onCellTap: (GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGameColors.current.board
    val groupInfo = remember(board) { BoardRules.groupInfo(board) }
    Box(
        modifier
            .aspectRatio(1f)
            .shadow(14.dp, BoardShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(BoardShape)
            .background(palette.frame)
            .border(1.dp, palette.frameEdge, BoardShape)
            .padding(8.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            for (r in 0 until BoardRules.SIZE) {
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (c in 0 until BoardRules.SIZE) {
                        val position = GridPosition(r, c)
                        BoardCell(
                            position = position,
                            owner = board[r][c],
                            capturedOwner = capturing[position],
                            isLastMove = showLastMove && lastMove == position,
                            isInvalid = invalidCell == position,
                            currentPlayer = currentPlayer,
                            info = groupInfo[position],
                            enabled = enabled,
                            onTap = onCellTap,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    position: GridPosition,
    owner: Player?,
    capturedOwner: Player?,
    isLastMove: Boolean,
    isInvalid: Boolean,
    currentPlayer: Player,
    info: GroupInfo?,
    enabled: Boolean,
    onTap: (GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    val palette = gameColors.board
    val errorColor = MaterialTheme.colorScheme.error
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(if (owner != null) 1f else 0f) }
    val shake = remember { Animatable(0f) }
    val previousOwner = remember { arrayOf(owner) }

    LaunchedEffect(owner, capturedOwner) {
        val before = previousOwner[0]
        previousOwner[0] = owner
        when {
            owner != null && owner != before -> {
                // Placement: scale + fade in.
                scale.snapTo(0.55f)
                alpha.snapTo(0f)
                launch { scale.animateTo(1f, tween(170, easing = FastOutSlowInEasing)) }
                alpha.animateTo(1f, tween(140))
            }
            owner == null && capturedOwner != null -> {
                // Capture: brief pulse, then shrink and fade.
                alpha.snapTo(1f)
                scale.animateTo(1.14f, tween(70))
                launch { alpha.animateTo(0f, tween(160)) }
                scale.animateTo(0.35f, tween(160, easing = FastOutSlowInEasing))
            }
            owner == null -> alpha.snapTo(0f)
        }
    }
    LaunchedEffect(isInvalid) {
        if (isInvalid) {
            for (target in listOf(6f, -5f, 4f, -2f, 0f)) shake.animateTo(target, tween(45))
        }
    }
    val invalidAlpha by animateFloatAsState(if (isInvalid) 1f else 0f, tween(160), label = "invalid")
    val lastMoveAlpha by animateFloatAsState(if (isLastMove) 1f else 0f, tween(200), label = "lastMove")

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val canTap = enabled && owner == null && capturedOwner == null
    val displayOwner = owner ?: capturedOwner

    val description = describeCell(position, owner, info, isLastMove)

    Box(
        modifier
            .semantics {
                contentDescription = description
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = canTap,
                role = Role.Button,
                onClickLabel = "Place piece"
            ) { onTap(position) }
            .drawBehind {
                val inset = size.minDimension * 0.04f
                val tile = Size(size.width - inset * 2, size.height - inset * 2)
                val corner = size.minDimension * 0.16f
                translate(left = shake.value.dp.toPx()) {
                    drawCellTile(palette, Offset(inset, inset), tile, corner)
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension * 0.36f

                    if (pressed && canTap) {
                        drawToken(currentPlayer, gameColors.of(currentPlayer), center, radius, alpha = 0.28f)
                    }
                    if (invalidAlpha > 0f) {
                        drawToken(currentPlayer, gameColors.of(currentPlayer), center, radius, alpha = 0.35f * invalidAlpha)
                        drawRoundRect(
                            color = errorColor.copy(alpha = invalidAlpha),
                            topLeft = Offset(inset, inset),
                            size = tile,
                            cornerRadius = CornerRadius(corner),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    if (displayOwner != null) {
                        drawToken(displayOwner, gameColors.of(displayOwner), center, radius * scale.value, alpha.value)
                        if (lastMoveAlpha > 0f && owner != null) {
                            drawCircle(
                                color = palette.marker.copy(alpha = 0.85f * lastMoveAlpha * alpha.value),
                                radius = radius * 1.2f,
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }
            }
    )
}

private fun describeCell(position: GridPosition, owner: Player?, info: GroupInfo?, isLastMove: Boolean): String {
    val where = "row ${position.row + 1} column ${position.col + 1}"
    if (owner == null) return "Empty cell, $where"
    return buildString {
        append("${owner.label} piece at $where")
        if (info != null) {
            val libs = if (info.liberties == 1) "one liberty" else "${info.liberties} liberties"
            if (info.size > 1) append(", ${owner.label} group of ${info.size} with $libs")
            else append(", $libs")
        }
        if (isLastMove) append(", last move")
    }
}
