package rules

import game.*
import utils.Point

//Given a row/col with exactly one gap to place, if placing a gap in a cell would make it a dead end, it must be a wall instead
class LastGapCantCreateDeadend: Rule {
    override fun name() = "LastGapCantCreateDeadend"

    override fun apply(board: Board): ApplyResult {

        fun rowRule(row: List<Point>, rowIdx: Int): Rule.Check? {
            if (gapsRemaining(board.rowReqs[rowIdx], row) == 1) {
                val changeToWall = mutableSetOf<Point>()
                for (colIdx in board.grid.cols) {
                    if (!row[colIdx].type.known && row[colIdx].type.canBe(Type.WALL) && wouldBeDeadEndAsGap(rowIdx, colIdx, board, true)) {
                        changeToWall.add(Point(rowIdx, colIdx, TypeRange(setOf(Type.WALL))))
                    }
                }

                if (changeToWall.isNotEmpty()) {
                    return Rule.Check(board.update(changeToWall), "row[$rowIdx]");
                }
            }
            return null
        }

        fun colRule(col: List<Point>, colIdx: Int): Rule.Check? {
            if (gapsRemaining(board.colReqs[colIdx], col) == 1) {
                val changeToWall = mutableSetOf<Point>()
                for (rowIdx in board.grid.rows) {
                    if (!col[rowIdx].type.known && col[rowIdx].type.canBe(Type.WALL) && wouldBeDeadEndAsGap(rowIdx, colIdx, board, false)) {
                        changeToWall.add(Point(rowIdx, colIdx, TypeRange(setOf(Type.WALL))))
                    }
                }
                if (changeToWall.isNotEmpty()) {
                    return Rule.Check(board.update(changeToWall), "col[$colIdx]")
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

    private fun wouldBeDeadEndAsGap(rowIdx: Int, colIdx: Int, board: Board, checkingRow: Boolean): Boolean {
        val verticalNeighbors = board.grid.verticalNeighbors(rowIdx, colIdx)
        val horizontalNeighbors = board.grid.horizontalNeighbors(rowIdx, colIdx)
        val untouchedNeighbors = if (checkingRow) verticalNeighbors else horizontalNeighbors
        val touchedNeighbors = if (checkingRow) horizontalNeighbors else verticalNeighbors

        val neighborWallCount = untouchedNeighbors.count{ it.type.eq(Type.WALL) } +
                touchedNeighbors.count{ it.type.canBe(Type.WALL) }

        val neighborCount = untouchedNeighbors.count() + touchedNeighbors.count()

        return neighborCount - neighborWallCount <= 1
    }

    private fun gapsRemaining(wallsReqd: Int, line: List<Point>): Int {
        val wallsLeftToPlace = wallsReqd - line.count{ it.type.eq(Type.WALL) }
        val unknownsThatCouldBeWall = line.count{it.type.canBe(Type.WALL) && !it.type.known}
        return unknownsThatCouldBeWall - wallsLeftToPlace
    }
}