package rules

import Board
import java.lang.RuntimeException

//If the total number of walls in a row or column matches the number of walls required, every other space
//in that row or column must be free
class WallsExhausted : Rule {
    override fun name() = "WallsExhausted"
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.indices) {
            val row = board.row(rIdx);
            val rowWalls = row.filter{it == Space.WALL}.count()
            val rowUnknowns = row.filter{it == Space.UNKNOWN}.count()
            if (rowWalls == board.rowReqs[rIdx] && rowUnknowns > 0) {
                val result = unknownToFreeRow(rIdx, board)
                return ApplyResult(true, result.first,name(), "${name()}.row[${rIdx}]", result.second)
            }
        }

        for (cIdx in board.grid[0].indices) {
            val col = board.col(cIdx);
            val colWalls = col.filter{it == Space.WALL}.count()
            val colUnknowns = col.filter{it == Space.UNKNOWN}.count()
            if (colWalls == board.colReqs[cIdx] && colUnknowns > 0) {
                val result = unknownToFreeCol(cIdx, board)
                return ApplyResult(true, result.first,name(), "${name()}.col[${cIdx}]", result.second)
            }
        }

        return ApplyResult(false, false,name(),"", board);
    }

    private fun unknownToFreeRow(rIdx: Int, board: Board): Pair<Boolean, Board> {
        var b = board
        for (cIdx in board.grid[rIdx].indices) {
            if (b.grid[rIdx][cIdx] == Space.UNKNOWN) {
                val update = b.update(rIdx, cIdx, Space.EMPTY)
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
        for (rIdx in board.grid.indices) {
            if (b.grid[rIdx][cIdx] == Space.UNKNOWN) {
                val update = b.update(rIdx, cIdx, Space.EMPTY)
                if (!update.valid) {
                    return Pair(true, b)
                }
                b = update.board
            }
        }
        return Pair(false, b)
    }
}