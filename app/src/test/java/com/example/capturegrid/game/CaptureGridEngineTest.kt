package com.example.capturegrid.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private val P1 = Player.PLAYER_ONE
private val P2 = Player.PLAYER_TWO

/** Builds an engine from 7 rows of text: 'X' = Player 1, 'O' = Player 2, '.' = empty. */
private fun engineOf(
    vararg rows: String,
    current: Player = P1,
    p1Captures: Int = 0,
    p2Captures: Int = 0
): CaptureGridEngine {
    val padded = rows.toList() + List(BoardRules.SIZE - rows.size) { "......." }
    val board = padded.map { row ->
        row.padEnd(BoardRules.SIZE, '.').map {
            when (it) {
                'X' -> P1
                'O' -> P2
                else -> null
            }
        }
    }
    return CaptureGridEngine(
        CaptureGridState(
            board = board,
            currentPlayer = current,
            player1Captures = p1Captures,
            player2Captures = p2Captures
        )
    )
}

private fun pos(row: Int, col: Int) = GridPosition(row, col)

class CaptureGridEngineTest {

    @Test
    fun legalPlacement_placesPieceAndSwitchesPlayer() {
        val engine = CaptureGridEngine()
        val result = engine.placePiece(pos(3, 3))
        assertTrue(result is PlacementResult.Placed)
        assertEquals(P1, engine.state.board[3][3])
        assertEquals(P2, engine.state.currentPlayer)
        assertEquals(1, engine.state.moveCount)
        assertEquals(pos(3, 3), engine.state.lastMove)
    }

    @Test
    fun occupiedCell_isRejectedWithoutChangingTurn() {
        val engine = CaptureGridEngine()
        engine.placePiece(pos(0, 0))
        val result = engine.placePiece(pos(0, 0))
        assertEquals(PlacementResult.Rejected(RejectReason.OCCUPIED), result)
        assertEquals(P2, engine.state.currentPlayer)
        assertEquals(1, engine.state.moveCount)
    }

    @Test
    fun outOfBoard_isRejected() {
        val engine = CaptureGridEngine()
        assertEquals(PlacementResult.Rejected(RejectReason.OUT_OF_BOUNDS), engine.placePiece(pos(7, 0)))
        assertEquals(PlacementResult.Rejected(RejectReason.OUT_OF_BOUNDS), engine.placePiece(pos(0, -1)))
    }

    @Test
    fun neighbors_areOrthogonalAndClippedToBoard() {
        assertEquals(setOf(pos(0, 1), pos(1, 0)), BoardRules.getNeighbors(pos(0, 0)).toSet())
        assertEquals(
            setOf(pos(2, 3), pos(4, 3), pos(3, 2), pos(3, 4)),
            BoardRules.getNeighbors(pos(3, 3)).toSet()
        )
        assertEquals(2, BoardRules.getNeighbors(pos(6, 6)).size)
        assertEquals(3, BoardRules.getNeighbors(pos(0, 3)).size)
    }

    @Test
    fun singlePieceGroup() {
        val engine = engineOf(".......", "..X....")
        assertEquals(setOf(pos(1, 2)), engine.getConnectedGroup(pos(1, 2), P1))
    }

    @Test
    fun multiPieceGroup_ignoresDiagonals() {
        val engine = engineOf(
            "XX.....",
            "X......",
            ".X....."
        )
        assertEquals(setOf(pos(0, 0), pos(0, 1), pos(1, 0)), engine.getConnectedGroup(pos(0, 0), P1))
    }

    @Test
    fun libertyDetection_countsUniqueEmptyNeighbors() {
        val engine = engineOf(
            "XX.....",
            "XO....."
        )
        val group = engine.getConnectedGroup(pos(0, 0), P1)
        assertEquals(setOf(pos(0, 2), pos(2, 0)), engine.getLiberties(group))
    }

    @Test
    fun singlePieceCapture() {
        val engine = engineOf(
            ".X.....",
            "XO.....",
            ".X....."
        )
        val result = engine.placePiece(pos(1, 2)) as PlacementResult.Placed
        assertEquals(setOf(pos(1, 1)), result.captured.keys)
        assertNull(engine.state.board[1][1])
        assertEquals(1, engine.state.player1Captures)
    }

    @Test
    fun multiPieceCapture() {
        val engine = engineOf(
            "OO.....",
            "XX....."
        )
        val result = engine.placePiece(pos(0, 2)) as PlacementResult.Placed
        assertEquals(setOf(pos(0, 0), pos(0, 1)), result.captured.keys)
        assertNull(engine.state.board[0][0])
        assertNull(engine.state.board[0][1])
        assertEquals(2, engine.state.player1Captures)
    }

    @Test
    fun simultaneousCaptureOfMultipleGroups() {
        // Four separate O stones share (1,1) as their last liberty.
        val engine = engineOf(
            "XOX....",
            "O.OX...",
            "XOX....",
            ".X....."
        )
        val result = engine.placePiece(pos(1, 1)) as PlacementResult.Placed
        assertEquals(setOf(pos(0, 1), pos(1, 0), pos(1, 2), pos(2, 1)), result.captured.keys)
        assertEquals(4, engine.state.player1Captures)
    }

    @Test
    fun selfCapture_isRejectedAndBoardRestored() {
        val engine = engineOf(
            ".O.....",
            "O......",
            current = P1
        )
        val before = engine.state
        val result = engine.placePiece(pos(0, 0))
        assertEquals(PlacementResult.Rejected(RejectReason.SELF_CAPTURE), result)
        assertEquals(before, engine.state)
        assertNull(engine.state.board[0][0])
    }

    @Test
    fun captureAllowsOtherwiseTrappedPlacement() {
        // X at (0,0) touches only O stones, but it removes the last liberty of (0,1).
        val engine = engineOf(
            ".OX....",
            "OX....."
        )
        val result = engine.placePiece(pos(0, 0)) as PlacementResult.Placed
        assertEquals(setOf(pos(0, 1)), result.captured.keys)
        assertEquals(P1, engine.state.board[0][0])
        assertEquals(P2, engine.state.board[1][0])
        assertEquals(1, engine.state.player1Captures)
    }

    @Test
    fun captureScore_accumulatesForCorrectPlayer() {
        val engine = engineOf(
            ".O.....",
            "OX.....",
            ".O.....",
            current = P2,
            p2Captures = 3
        )
        engine.placePiece(pos(1, 2))
        assertEquals(4, engine.state.player2Captures)
        assertEquals(0, engine.state.player1Captures)
    }

    @Test
    fun pass_switchesPlayer() {
        val engine = CaptureGridEngine()
        assertTrue(engine.passTurn())
        assertEquals(P2, engine.state.currentPlayer)
        assertEquals(1, engine.state.consecutivePasses)
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun placementResetsConsecutivePasses() {
        val engine = CaptureGridEngine()
        engine.passTurn()
        engine.placePiece(pos(2, 2))
        assertEquals(0, engine.state.consecutivePasses)
        engine.passTurn()
        assertFalse(engine.state.isGameOver)
    }

    @Test
    fun twoPasses_endGame_andBlockFurtherActions() {
        val engine = CaptureGridEngine()
        engine.passTurn()
        engine.passTurn()
        assertTrue(engine.state.isGameOver)
        assertEquals(EndReason.BOTH_PASSED, engine.state.endReason)
        assertEquals(PlacementResult.Rejected(RejectReason.GAME_OVER), engine.placePiece(pos(0, 0)))
        assertFalse(engine.passTurn())
    }

    @Test
    fun winnerByCaptures() {
        val engine = engineOf("OOOO...", p1Captures = 2, p2Captures = 5)
        engine.finishGame()
        assertEquals(P2, engine.state.winner)
        assertEquals(Decision.CAPTURES, engine.state.decision)
    }

    @Test
    fun tieBreakByRemainingPieces() {
        val engine = engineOf("XXX.OO.", p1Captures = 3, p2Captures = 3)
        engine.finishGame()
        assertEquals(P1, engine.state.winner)
        assertEquals(Decision.PIECES_REMAINING, engine.state.decision)
        assertFalse(engine.state.isDraw)
    }

    @Test
    fun finalDraw() {
        val engine = engineOf("XX..OO.", p1Captures = 1, p2Captures = 1)
        engine.finishGame()
        assertNull(engine.state.winner)
        assertTrue(engine.state.isDraw)
        assertEquals(Decision.DRAW, engine.state.decision)
    }

    @Test
    fun inputBlockedWhileAnimating() {
        val engine = CaptureGridEngine()
        engine.setAnimating(true)
        assertEquals(PlacementResult.Rejected(RejectReason.BUSY), engine.placePiece(pos(0, 0)))
        assertFalse(engine.passTurn())
        engine.setAnimating(false)
        assertTrue(engine.placePiece(pos(0, 0)) is PlacementResult.Placed)
    }
}
