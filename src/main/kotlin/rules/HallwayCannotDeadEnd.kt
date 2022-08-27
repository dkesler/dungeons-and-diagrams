package rules

import Board
import neighborsWithTypes

class HallwayCannotDeadEnd : Rule {
    override fun apply(board: Board): ApplyResult {
        for (row in board.grid.indices) {
            for (col in board.grid[0].indices) {
                if (board.grid[row][col] == Space.HALL) {
                    val neighbors = neighborsWithTypes(row, col, board.grid)
                    //if a hallway ever has neighbors.count()-1 walls, it would be a dead end.  so if it has neighbors.count()-2 walls
                    //as neighbors, all other neighbors must be some form of empty
                    if (neighbors.count{it.type == Space.WALL } == neighbors.count()-2 && neighbors.count{it.type == Space.UNKNOWN} > 0) {
                        val unknownNeighbors = neighbors.filter{it.type == Space.UNKNOWN }
                        var b = board
                        for (n in unknownNeighbors) {
                            val update = b.update(n.row, n.col, Space.EMPTY)
                            if (!update.valid) {
                                return ApplyResult(true, true, "HallwayCannotDeadEnd", "HallwayCannotDeadEnd.row[$row]col[${col}]", b)
                            }
                            b = update.board
                        }
                        return ApplyResult(true, false, "HallwayCannotDeadEnd", "HallwayCannotDeadEnd.row[$row]col[${col}]", b)
                    }
                }
            }
        }
        return ApplyResult(false, false, "HallwayCannotDeadEnd", "", board)
    }
}