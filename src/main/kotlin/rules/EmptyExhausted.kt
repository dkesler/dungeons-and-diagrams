package rules

import game.Board
import game.CellType

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun name() = "EmptyExhausted"
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.rows) {
            val row = board.row(rIdx);
            val rowWalls = row.filter{it.eq(CellType.WALL)}.count()
            val rowPotentialWalls = row.filter{it.canBe(CellType.WALL) && !it.known}.count()
            if (rowPotentialWalls == board.rowReqs[rIdx] - rowWalls && rowPotentialWalls > 0) {
                val update = unknownToWallRow(rIdx, board)
                return ApplyResult(true, update.second,name(),"${name()}.row[${rIdx}]" , update.first)
            }
        }

        for (cIdx in board.grid.cols) {
            val col = board.col(cIdx);
            val colWalls = col.filter{it.eq(CellType.WALL)}.count()
            val colPotentialWalls = col.filter{it.canBe(CellType.WALL) && !it.known}.count()
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