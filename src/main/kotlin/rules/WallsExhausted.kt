package rules

import game.Board
import game.CellType

//If the total number of walls in a row or column matches the number of walls required, every other CellType
//in that row or column must be free
class WallsExhausted : Rule {
    override fun name() = "WallsExhausted"
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.rows) {
            val row = board.row(rIdx);
            val rowWalls = row.filter{it.eq(CellType.WALL)}.count()
            val rowPotentialWalls = row.filter{it.canBe(CellType.WALL) && !it.known}.count()
            if (rowWalls == board.rowReqs[rIdx] && rowPotentialWalls > 0) {
                val result = unknownToFreeRow(rIdx, board)
                return ApplyResult(true, result.first,name(), "${name()}.row[${rIdx}]", result.second)
            }
        }

        for (cIdx in board.grid.cols) {
            val col = board.col(cIdx);
            val colWalls = col.filter{it.eq(CellType.WALL)}.count()
            val colPotentialWalls = col.filter{it.canBe(CellType.WALL) && !it.known}.count()
            if (colWalls == board.colReqs[cIdx] && colPotentialWalls > 0) {
                val result = unknownToFreeCol(cIdx, board)
                return ApplyResult(true, result.first,name(), "${name()}.col[${cIdx}]", result.second)
            }
        }

        return ApplyResult(false, false,name(),"", board);
    }

    private fun unknownToFreeRow(rIdx: Int, board: Board): Pair<Boolean, Board> {
        var b = board
        for (cIdx in board.grid.cells[rIdx].indices) {
            val cell = b.grid.cells[rIdx][cIdx]
            if (cell.canBe(CellType.WALL) && !cell.known) {
                val update = b.update(rIdx, cIdx, cell.types - CellType.WALL)
                if (!update.valid) {
                    return Pair(true, b)
                }
                b = update.board
            }
        }
        return Pair(false, b)
    }

    private fun unknownToFreeCol(cIdx: Int, board: Board): Pair<Boolean, Board> {
        var b = board
        for (rIdx in board.grid.rows) {
            val cell = b.grid.cells[rIdx][cIdx]
            if (cell.canBe(CellType.WALL) && !cell.known) {
                val update = b.update(rIdx, cIdx, cell.types - CellType.WALL)
                if (!update.valid) {
                    return Pair(true, b)
                }
                b = update.board
            }
        }
        return Pair(false, b)
    }
}