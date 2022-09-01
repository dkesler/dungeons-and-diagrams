package rules

import game.Board
import game.CellType
import game.neighborsWithTypes

//For a given cell that could be hall or treasure room, if every neighbor, or every neighbor but one is a wall,
// the cell must be a wall to avoid creating a dead end
class AvoidCreatingDeadEnd : Rule {
    override fun name() = "AvoidCreatingDeadEnd"
    override fun apply(board: Board): ApplyResult {
        for (rowIdx in board.grid.rows) {
            for (colIdx in board.grid.cols) {
                if (board.grid.cells[rowIdx][colIdx].canBe(CellType.TREASURE_ROOM, CellType.HALL)) {
                    val neighbors = neighborsWithTypes(rowIdx, colIdx, board.grid.cells)
                    val wallNeighbors = neighbors.filter { it.type.eq(CellType.WALL) }
                    if (wallNeighbors.count() >= neighbors.count()-1) {
                        val update = board.update(rowIdx, colIdx, board.grid.cells[rowIdx][colIdx].types - setOf(CellType.TREASURE_ROOM, CellType.HALL))
                        return ApplyResult(true, !update.valid, name(), "${name()}.row[$rowIdx].col[$colIdx]", update.board)
                    }
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}