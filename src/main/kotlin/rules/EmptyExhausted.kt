package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun name() = "EmptyExhausted"
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.rows) {
            val row = board.grid.row(rIdx);
            val rowWalls = row.count { it.type.eq(CellType.WALL) }
            val rowPotentialWalls = row.filter { it.type.canBe(CellType.WALL) && !it.type.known }
            if (rowPotentialWalls.size == board.rowReqs[rIdx] - rowWalls && rowPotentialWalls.isNotEmpty()) {
                val toUpdate = rowPotentialWalls.map{ Point(it.row, it.col, TypeRange(setOf(CellType.WALL))) }
                return update(board, toUpdate, ".row[${rIdx}]")
            }
        }

        for (cIdx in board.grid.cols) {
            val col = board.grid.col(cIdx);
            val colWalls = col.count { it.type.eq(CellType.WALL) }
            val colPotentialWalls = col.filter { it.type.canBe(CellType.WALL) && !it.type.known }
            if (colPotentialWalls.size == board.colReqs[cIdx] - colWalls && colPotentialWalls.isNotEmpty()) {
                val toUpdate = colPotentialWalls.map{ Point(it.row, it.col, TypeRange(setOf(CellType.WALL))) }
                return update(board, toUpdate, ".col[${cIdx}]")
            }
        }

        return ApplyResult(false, false, name(), "", board);
    }
}