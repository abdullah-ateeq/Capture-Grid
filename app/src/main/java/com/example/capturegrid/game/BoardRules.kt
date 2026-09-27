package com.example.capturegrid.game

/**
 * Stateless board helpers. Only orthogonal (up/down/left/right) adjacency is used
 * for neighbours, groups, liberties and captures.
 */
object BoardRules {
    const val SIZE = 7

    private val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

    fun emptyBoard(): Board = List(SIZE) { List(SIZE) { null } }

    fun isInside(position: GridPosition): Boolean =
        position.row in 0 until SIZE && position.col in 0 until SIZE

    fun getNeighbors(position: GridPosition): List<GridPosition> =
        directions
            .map { (dr, dc) -> GridPosition(position.row + dr, position.col + dc) }
            .filter(::isInside)

    fun cellAt(board: Board, position: GridPosition): Player? = board[position.row][position.col]

    /** Breadth-first search over same-player, orthogonally connected pieces. */
    fun getConnectedGroup(board: Board, start: GridPosition, player: Player): Set<GridPosition> {
        if (!isInside(start) || cellAt(board, start) != player) return emptySet()
        val group = linkedSetOf(start)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (next in getNeighbors(current)) {
                if (next !in group && cellAt(board, next) == player) {
                    group += next
                    queue += next
                }
            }
        }
        return group
    }

    fun getLiberties(board: Board, group: Set<GridPosition>): Set<GridPosition> {
        val liberties = mutableSetOf<GridPosition>()
        for (cell in group) {
            for (next in getNeighbors(cell)) {
                if (cellAt(board, next) == null) liberties += next
            }
        }
        return liberties
    }

    fun countPieces(board: Board, player: Player): Int = board.sumOf { row -> row.count { it == player } }

    fun isFull(board: Board): Boolean = board.all { row -> row.all { it != null } }

    fun allPositions(): List<GridPosition> =
        (0 until SIZE).flatMap { r -> (0 until SIZE).map { c -> GridPosition(r, c) } }

    /** Group size and liberty count for every occupied cell; used for accessibility descriptions. */
    fun groupInfo(board: Board): Map<GridPosition, GroupInfo> {
        val result = mutableMapOf<GridPosition, GroupInfo>()
        for (position in allPositions()) {
            if (position in result) continue
            val owner = cellAt(board, position) ?: continue
            val group = getConnectedGroup(board, position, owner)
            val info = GroupInfo(group.size, getLiberties(board, group).size)
            group.forEach { result[it] = info }
        }
        return result
    }
}

data class GroupInfo(val size: Int, val liberties: Int)
