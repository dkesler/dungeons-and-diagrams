package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

//For each row/col, if it has one wall left to place, and placing that wall in a cell would result in a deadend
//that cell cannot be a wall
class LastWallCantCreateDeadend: Rule {
    override fun apply(board: Board): ApplyResult {
        fun rowRule(row: List<Point>, rowIdx: Int): Rule.Check? {
            if (wallsRemaining(board.rowReqs[rowIdx], row) == 1) {
                val changeFromWall = mutableSetOf<Point>()
                for (colIdx in board.grid.cols) {
                    if (!row[colIdx].type.known && row[colIdx].type.canBe(CellType.WALL) && wouldCreateDeadEndAsWall(colIdx, row, board)) {
                        changeFromWall.add(Point(rowIdx, colIdx, TypeRange(row[colIdx].type.types - CellType.WALL)))
                    }
                }
                if (changeFromWall.isNotEmpty()) {
                    return Rule.Check(
                        board.update(changeFromWall),
                        "row[${rowIdx}]"
                    )
                }
            }
            return null
        }

        fun colRule(col: List<Point>, colIdx: Int): Rule.Check? {
            if (wallsRemaining(board.colReqs[colIdx], col) == 1) {
                val changeFromWall = mutableSetOf<Point>()
                for (rowIdx in board.grid.rows) {
                    if (!col[rowIdx].type.known && col[rowIdx].type.canBe(CellType.WALL) && wouldCreateDeadEndAsWall(rowIdx, col, board)) {
                        changeFromWall.add(Point(rowIdx, colIdx, TypeRange(col[rowIdx].type.types - CellType.WALL)))
                    }
                }
                if (changeFromWall.isNotEmpty()) {
                    return Rule.Check(
                        board.update(changeFromWall),
                        "col[${colIdx}]"
                    )
                }
            }
            return null
        }
        return eachRowAndCol(
            board,
            ::rowRule,
            ::colRule
        )
    }

    private fun wallsRemaining(wallsRequired: Int, points: List<Point>): Int {
        return wallsRequired - points.count{ it.type.eq(CellType.WALL) }
    }

    private fun wouldCreateDeadEndAsWall(idx: Int, slice: List<Point>, board: Board): Boolean {
        val pointTurningToWall = slice[idx]
        for (point in slice) {
            if (pointTurningToWall != point && point.type.canBe(CellType.HALL, CellType.TREASURE_ROOM)) {
                val neighbors = board.grid.neighbors(point.row, point.col)
                val wallNeighbors = neighbors.filter{ it == pointTurningToWall || it.type.eq(CellType.WALL) }
                if (wallNeighbors.size + 1 >= neighbors.size) {
                    return true
                }
            }
        }
        return false
    }

    override fun name() = "LastWallCantCreateDeadend"
}