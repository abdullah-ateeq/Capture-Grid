package com.example.capturegrid.game

/**
 * Authoritative rules engine for a single Capture Grid match.
 *
 * All mutations happen on a private working board; [state] is republished as an
 * immutable snapshot after every accepted action, so a rejected move never leaks
 * partial changes (board or score).
 */
class CaptureGridEngine(initialState: CaptureGridState = CaptureGridState()) {

    var state: CaptureGridState = initialState
        private set

    private val cells: MutableList<MutableList<Player?>> =
        initialState.board.map { it.toMutableList() }.toMutableList()

    fun getNeighbors(position: GridPosition): List<GridPosition> = BoardRules.getNeighbors(position)

    fun getConnectedGroup(start: GridPosition, player: Player): Set<GridPosition> =
        BoardRules.getConnectedGroup(cells, start, player)

    fun getLiberties(group: Set<GridPosition>): Set<GridPosition> =
        BoardRules.getLiberties(cells, group)

    /** Clears every cell of [group] and returns the number of pieces removed. */
    fun removeCapturedGroup(group: Set<GridPosition>): Int {
        var removed = 0
        for (cell in group) {
            if (cells[cell.row][cell.col] != null) {
                cells[cell.row][cell.col] = null
                removed++
            }
        }
        return removed
    }

    fun setAnimating(animating: Boolean) {
        state = state.copy(isAnimating = animating)
    }

    fun placePiece(position: GridPosition): PlacementResult {
        if (state.isGameOver) return PlacementResult.Rejected(RejectReason.GAME_OVER)
        if (state.isAnimating) return PlacementResult.Rejected(RejectReason.BUSY)
        if (!BoardRules.isInside(position)) return PlacementResult.Rejected(RejectReason.OUT_OF_BOUNDS)
        if (cells[position.row][position.col] != null) return PlacementResult.Rejected(RejectReason.OCCUPIED)

        val snapshot = cells.map { it.toList() }
        val player = state.currentPlayer
        val opponent = player.opponent
        cells[position.row][position.col] = player

        // Resolve every distinct adjacent opponent group before removing anything.
        val inspected = mutableSetOf<GridPosition>()
        val capturedGroups = mutableListOf<Set<GridPosition>>()
        for (neighbor in getNeighbors(position)) {
            if (neighbor in inspected || cells[neighbor.row][neighbor.col] != opponent) continue
            val group = getConnectedGroup(neighbor, opponent)
            inspected += group
            if (getLiberties(group).isEmpty()) capturedGroups += group
        }
        val captured = capturedGroups.flatten().associateWith { opponent }
        val points = capturedGroups.sumOf { removeCapturedGroup(it) }

        if (getLiberties(getConnectedGroup(position, player)).isEmpty()) {
            snapshot.forEachIndexed { r, row -> row.forEachIndexed { c, value -> cells[r][c] = value } }
            return PlacementResult.Rejected(RejectReason.SELF_CAPTURE)
        }

        state = state.copy(
            board = boardSnapshot(),
            player1Captures = state.player1Captures + if (player == Player.PLAYER_ONE) points else 0,
            player2Captures = state.player2Captures + if (player == Player.PLAYER_TWO) points else 0,
            moveCount = state.moveCount + 1,
            consecutivePasses = 0,
            lastMove = position
        )

        if (BoardRules.isFull(cells)) {
            finishGame(EndReason.BOARD_FULL)
        } else {
            state = state.copy(currentPlayer = opponent)
        }
        return PlacementResult.Placed(position, player, captured, state.isGameOver)
    }

    /** Returns false if passing is not currently allowed. */
    fun passTurn(): Boolean {
        if (state.isGameOver || state.isAnimating) return false
        val passes = state.consecutivePasses + 1
        state = state.copy(consecutivePasses = passes)
        if (passes >= 2) {
            finishGame(EndReason.BOTH_PASSED)
        } else {
            state = state.copy(currentPlayer = state.currentPlayer.opponent)
        }
        return true
    }

    fun finishGame(reason: EndReason = EndReason.BOTH_PASSED) {
        if (state.isGameOver) return
        val p1Score = state.player1Captures
        val p2Score = state.player2Captures
        val p1Pieces = BoardRules.countPieces(cells, Player.PLAYER_ONE)
        val p2Pieces = BoardRules.countPieces(cells, Player.PLAYER_TWO)

        val (winner, decision) = when {
            p1Score > p2Score -> Player.PLAYER_ONE to Decision.CAPTURES
            p2Score > p1Score -> Player.PLAYER_TWO to Decision.CAPTURES
            p1Pieces > p2Pieces -> Player.PLAYER_ONE to Decision.PIECES_REMAINING
            p2Pieces > p1Pieces -> Player.PLAYER_TWO to Decision.PIECES_REMAINING
            else -> null to Decision.DRAW
        }
        state = state.copy(
            winner = winner,
            isDraw = winner == null,
            isGameOver = true,
            endReason = reason,
            decision = decision,
            isAnimating = false
        )
    }

    /** True if the current player has at least one placement that is not self-capture. */
    fun hasLegalMove(): Boolean {
        if (state.isGameOver) return false
        val player = state.currentPlayer
        return BoardRules.allPositions().any { position ->
            cells[position.row][position.col] == null && isLegalFor(position, player)
        }
    }

    private fun isLegalFor(position: GridPosition, player: Player): Boolean {
        val trial = cells.map { it.toMutableList() }
        trial[position.row][position.col] = player
        // A placement is legal if it has a liberty itself, joins a group that keeps one,
        // or captures an adjacent opponent group.
        val capturesSomething = BoardRules.getNeighbors(position).any { neighbor ->
            trial[neighbor.row][neighbor.col] == player.opponent &&
                BoardRules.getLiberties(trial, BoardRules.getConnectedGroup(trial, neighbor, player.opponent)).isEmpty()
        }
        return capturesSomething ||
            BoardRules.getLiberties(trial, BoardRules.getConnectedGroup(trial, position, player)).isNotEmpty()
    }

    private fun boardSnapshot(): Board = cells.map { it.toList() }
}
