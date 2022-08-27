package rules

import Board

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun name() = "EmptyExhausted"
    override fun apply(board: Board): ApplyResult {
        for (rIdx in board.grid.indices) {
            val row = board.row(rIdx);
            val rowWalls = row.filter{it == Space.WALL}.count()
            val rowUnknowns = row.filter{it == Space.UNKNOWN}.count()
            if (rowUnknowns == board.rowReqs[rIdx] - rowWalls && rowUnknowns > 0) {
                val update = unknownToWallRow(rIdx, board)
                return ApplyResult(true, update.second,name(),"${name()}.row[${rIdx}]" , update.first)
            }
        }

        for (cIdx in board.grid[0].indices) {
            val col = board.col(cIdx);
            val colWalls = col.filter{it == Space.WALL}.count()
            val colUnknowns = col.filter{it == Space.UNKNOWN}.count()
            if (colUnknowns == board.colReqs[cIdx] - colWalls && colUnknowns > 0) {
                val update = unknownToWallCol(cIdx, board)
                return ApplyResult(true, update.second,name(),"${name()}.col[${cIdx}]", update.first)
            }
        }

        return ApplyResult(false, false, name(), "", board);
    }

    private fun unknownToWallRow(rIdx: Int, board: Board): Pair<Board, Boolean> {
        var b = board
        for (cIdx in board.grid[rIdx].indices) {
            if (b.grid[rIdx][cIdx] == Space.UNKNOWN) {
                val update = b.update(rIdx, cIdx, Space.WALL)
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
        for (rIdx in board.grid.indices) {
            if (b.grid[rIdx][cIdx] == Space.UNKNOWN) {
                val update = b.update(rIdx, cIdx, Space.WALL)
                if (!update.valid) {
                    return Pair(b, true)
                }
                b = update.board

            }
        }
        return Pair(b, false)
    }
}