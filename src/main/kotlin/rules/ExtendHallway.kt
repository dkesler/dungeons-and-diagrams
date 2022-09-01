package rules

import game.Board
import game.CellType
import game.neighborsWithTypes

class ExtendHallway : Rule {
    override fun name() = "ExtendHallway"
    override fun apply(board: Board): ApplyResult {
        for (row in board.grid.rows) {
            for (col in board.grid.cols) {
                if (board.grid.cells[row][col].eq(CellType.HALL)) {
                    val neighbors = neighborsWithTypes(row, col, board.grid.cells)
                    //if a hallway ever has neighbors.count()-1 walls, it would be a dead end.  so if it has neighbors.count()-2 walls
                    //as neighbors, and it has more neighbors that might be walls but aren't known, those neighbors can't be walls
                    if (neighbors.count{it.type.eq(CellType.WALL) } == neighbors.count()-2 &&
                        neighbors.count{it.type.canBe(CellType.WALL)} > neighbors.count()-2) {
                        val unknownNeighbors = neighbors.filter{it.type.canBe(CellType.WALL) && !it.type.known }
                        var b = board
                        for (n in unknownNeighbors) {
                            val update = b.update(n.row, n.col, n.type.types - CellType.WALL)
                            if (!update.valid) {
                                return ApplyResult(true, true, name(), "${name()}.row[$row]col[${col}]", b)
                            }
                            b = update.board
                        }
                        return ApplyResult(true, false, name(), "${name()}.row[$row]col[${col}]", b)
                    }
                }
            }
        }
        return ApplyResult(false, false, name(), "", board)
    }
}