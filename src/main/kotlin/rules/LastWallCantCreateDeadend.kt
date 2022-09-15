package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point

//For each row/col, if it has one wall left to place, and placing that wall in a cell would result in a deadend
//that cell cannot be a wall
class LastWallCantCreateDeadend: Rule {
    override fun apply(board: Board): ApplyResult {
        return eachStripe(
            board,
            ::rowRule
        )
    }

    private fun rowRule(row: List<Point>, rowIdx: Int, board: Board): Rule.Check? {
        if (wallsRemaining(board.rowReqs[rowIdx], row) == 1) {
            val changeFromWall = mutableSetOf<Point>()
            for (colIdx in board.grid.cols) {
                if (!row[colIdx].type.known && row[colIdx].type.canBe(Type.WALL) && wouldCreateDeadEndAsWall(colIdx, row, board)) {
                    changeFromWall.add(Point(rowIdx, colIdx, TypeRange(row[colIdx].type.types - Type.WALL)))
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


    private fun wallsRemaining(wallsRequired: Int, points: List<Point>): Int {
        return wallsRequired - points.count{ it.type.eq(Type.WALL) }
    }

    private fun wouldCreateDeadEndAsWall(idx: Int, slice: List<Point>, board: Board): Boolean {
        val pointTurningToWall = slice[idx]
        for (point in slice) {
            if (pointTurningToWall != point && point.type.canBe(Type.HALLWAY, Type.ROOM)) {
                val neighbors = board.grid.neighbors(point.row, point.col)
                val wallNeighbors = neighbors.filter{ it == pointTurningToWall || it.type.eq(Type.WALL) }
                if (wallNeighbors.size + 1 >= neighbors.size) {
                    return true
                }
            }
        }
        return false
    }

    override fun name() = "LastWallCantCreateDeadend"
}