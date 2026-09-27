package com.example.capturegrid.game

enum class Player(val number: Int) {
    PLAYER_ONE(1),
    PLAYER_TWO(2);

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    val label: String
        get() = "Player $number"
}

data class GridPosition(
    val row: Int,
    val col: Int
)

typealias Board = List<List<Player?>>

enum class EndReason {
    BOARD_FULL,
    BOTH_PASSED
}

/** How the winner (or draw) was determined. */
enum class Decision {
    CAPTURES,
    PIECES_REMAINING,
    DRAW
}

data class CaptureGridState(
    val board: Board = BoardRules.emptyBoard(),
    val currentPlayer: Player = Player.PLAYER_ONE,
    val player1Captures: Int = 0,
    val player2Captures: Int = 0,
    val lastMove: GridPosition? = null,
    val consecutivePasses: Int = 0,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val moveCount: Int = 0,
    val isAnimating: Boolean = false,
    val isGameOver: Boolean = false,
    val endReason: EndReason? = null,
    val decision: Decision? = null
) {
    fun capturesOf(player: Player): Int =
        if (player == Player.PLAYER_ONE) player1Captures else player2Captures

    fun piecesOf(player: Player): Int = BoardRules.countPieces(board, player)
}

enum class RejectReason {
    GAME_OVER,
    BUSY,
    OUT_OF_BOUNDS,
    OCCUPIED,
    SELF_CAPTURE
}

sealed interface PlacementResult {
    /** [captured] maps every removed cell to the player whose piece was removed. */
    data class Placed(
        val position: GridPosition,
        val player: Player,
        val captured: Map<GridPosition, Player>,
        val gameOver: Boolean
    ) : PlacementResult

    data class Rejected(val reason: RejectReason) : PlacementResult
}
