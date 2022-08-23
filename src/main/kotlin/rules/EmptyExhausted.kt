package rules

import Board

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.indices) {
            val row = board.row(rIdx);
            val rowWalls = row.filter{it == Space.WALL}.count()
            val rowUnknowns = row.filter{it == Space.UNKNOWN}.count()
            if (rowUnknowns == board.rowReqs[rIdx] - rowWalls && rowUnknowns > 0) {
                return ApplyResult(true, "EmptyExhausted","EmptyExhausted.row[${rIdx}]" , unknownToWallRow(rIdx, board))
            }
        }

        for (cIdx in board.grid[0].indices) {
            val col = board.col(cIdx);
            val colWalls = col.filter{it == Space.WALL}.count()
            val colUnknowns = col.filter{it == Space.UNKNOWN}.count()
            if (colUnknowns == board.colReqs[cIdx] - colWalls && colUnknowns > 0) {
                return ApplyResult(true, "EmptyExhausted","EmptyExhausted.col[${cIdx}]", unknownToWallCol(cIdx, board))
            }
        }

        return ApplyResult(false, "EmptyExhausted", "", board);
    }

    private fun unknownToWallRow(rIdx: Int, board: Board): Board {
        var b = board
        for (cIdx in board.grid[rIdx].indices) {
            if (b.grid[rIdx][cIdx] == Space.UNKNOWN) {
                val update = b.update(rIdx, cIdx, Space.WALL)
                if (!update.valid) {
                    throw RuntimeException("Invalid update in EmptyExhausted: ${update.invalidReason}")
                }
                b = update.board
            }
        }
        return b
    }

    private fun unknownToWallCol(cIdx: Int, board: Board): Board {
        var b = board
        for (rIdx in board.grid.indices) {
            if (b.grid[rIdx][cIdx] == Space.UNKNOWN) {
                val update = b.update(rIdx, cIdx, Space.WALL)
                if (!update.valid) {
                    throw RuntimeException("Invalid update in EmptyExhausted: ${update.invalidReason}")
                }
                b = update.board

            }
        }
        return b
    }
}