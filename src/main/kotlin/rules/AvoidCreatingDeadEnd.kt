package rules

import Board
import neighborsWithTypes

//For a given unknown cell, if every neighbor, or every neighbor but one is a wall, the cell must be a wall to avoid
//creating a dead end
class AvoidCreatingDeadEnd : Rule {
    override fun name() = "AvoidCreatingDeadEnd"
    override fun apply(board: Board): ApplyResult {
        for (rowIdx in board.grid.indices) {
            for (colIdx in board.grid[0].indices) {
                if (board.grid[rowIdx][colIdx] == Space.UNKNOWN) {
                    val neighbors = neighborsWithTypes(rowIdx, colIdx, board.grid)
                    val wallNeighbors = neighbors.filter { it.type == Space.WALL }
                    if (wallNeighbors.count() >= neighbors.count()-1) {
                        val update = board.update(rowIdx, colIdx, Space.WALL)
                        return ApplyResult(true, !update.valid, name(), "${name()}.row[$rowIdx].col[$colIdx]", update.board)
                    }
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}