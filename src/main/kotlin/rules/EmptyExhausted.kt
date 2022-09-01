package rules

import game.Board
import game.CellType

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun name() = "EmptyExhausted"
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.rows) {
            val row = board.grid.row(rIdx);
            val rowWalls = row.count { it.eq(CellType.WALL) }
            val rowPotentialWalls = row.count { it.canBe(CellType.WALL) && !it.known }
            if (rowPotentialWalls == board.rowReqs[rIdx] - rowWalls && rowPotentialWalls > 0) {
                val update = unknownToWallRow(rIdx, board)
                return ApplyResult(true, update.second,name(),"${name()}.row[${rIdx}]" , update.first)
            }
        }

        for (cIdx in board.grid.cols) {
            val col = board.grid.col(cIdx);
            val colWalls = col.count { it.eq(CellType.WALL) }
            val colPotentialWalls = col.count { it.canBe(CellType.WALL) && !it.known }
            if (colPotentialWalls == board.colReqs[cIdx] - colWalls && colPotentialWalls > 0) {
                val update = unknownToWallCol(cIdx, board)
                return ApplyResult(true, update.second,name(),"${name()}.col[${cIdx}]", update.first)
            }
        }

        return ApplyResult(false, false, name(), "", board);
    }

    private fun unknownToWallRow(rIdx: Int, board: Board): Pair<Board, Boolean> {
        var b = board
        for (cIdx in board.grid.cells[rIdx].indices) {
            val cell = b.grid.cells[rIdx][cIdx]
            if (cell.canBe(CellType.WALL) && !cell.known) {
                val update = b.update(rIdx, cIdx, setOf(CellType.WALL))
                if (!update.valid) {
                    return Pair(b, true)
                }
                b = update.board
            }
        }
        return Pair(b, false)
    }

    private fun unknownToWallCol(cIdx: Int, board: Board): Pair<Board, Boolean> {
        var b = board
        for (rIdx in board.grid.rows) {
            val cell = b.grid.cells[rIdx][cIdx]
            if (cell.canBe(CellType.WALL) && !cell.known) {
                val update = b.update(rIdx, cIdx, setOf(CellType.WALL))
                if (!update.valid) {
                    return Pair(b, true)
                }
                b = update.board

            }
        }
        return Pair(b, false)
    }
}