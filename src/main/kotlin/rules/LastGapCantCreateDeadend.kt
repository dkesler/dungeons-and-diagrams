package rules

import game.*
import utils.Point

//Given a row/col with exactly one gap to place, if placing a gap in a cell would make it a dead end, it must be a wall instead
class LastGapCantCreateDeadend: Rule {
    override fun name() = "LastGapCantCreateDeadend"

    override fun apply(board: Board): ApplyResult {
        for (rowIdx in board.grid.rows) {
            val row = board.grid.row(rowIdx)
            if (gapsRemaining(board.rowReqs[rowIdx], row) == 1) {
                val changeToWall = mutableSetOf<Pair<Int, Int>>()
                for (colIdx in board.grid.cols) {
                    if (!row[colIdx].type.known && row[colIdx].type.canBe(CellType.WALL) && wouldBeDeadEndAsGap(rowIdx, colIdx, board, true)) {
                        changeToWall.add(Pair(rowIdx, colIdx))
                    }
                }
                if (changeToWall.isNotEmpty()) {
                    var b = board
                    for (point in changeToWall) {
                        val update = b.update(point.first, point.second, setOf(CellType.WALL))
                        if (!update.valid) {
                            return ApplyResult(true, true, name(), "", b)
                        }
                        b = update.board
                    }
                    return ApplyResult(true, false, name(), "${name()}.row[$rowIdx]", b)
                }
            }
        }

        for (colIdx in board.grid.cols) {
            val col = board.grid.col(colIdx)
            if (gapsRemaining(board.colReqs[colIdx], col) == 1) {
                val changeToWall = mutableSetOf<Pair<Int, Int>>()
                for (rowIdx in board.grid.rows) {
                    if (!col[rowIdx].type.known && col[rowIdx].type.canBe(CellType.WALL) && wouldBeDeadEndAsGap(rowIdx, colIdx, board, false)) {
                        changeToWall.add(Pair(rowIdx, colIdx))
                    }
                }
                if (changeToWall.isNotEmpty()) {
                    var b = board
                    for (point in changeToWall) {
                        val update = b.update(point.first, point.second, setOf(CellType.WALL))
                        if (!update.valid) {
                            return ApplyResult(true, true, name(), "", b)
                        }
                        b = update.board
                    }
                    return ApplyResult(true, false, name(), "${name()}.col[$colIdx]", b)
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }

    private fun wouldBeDeadEndAsGap(rowIdx: Int, colIdx: Int, board: Board, checkingRow: Boolean): Boolean {
        val verticalNeighbors = board.grid.verticalNeighbors(rowIdx, colIdx)
        val horizontalNeighbors = board.grid.horizontalNeighbors(rowIdx, colIdx)
        val untouchedNeighbors = if (checkingRow) verticalNeighbors else horizontalNeighbors
        val touchedNeighbors = if (checkingRow) horizontalNeighbors else verticalNeighbors

        val neighborWallCount = untouchedNeighbors.count{ it.type.eq(CellType.WALL) } +
                touchedNeighbors.count{ it.type.canBe(CellType.WALL) }

        val neighborCount = untouchedNeighbors.count() + touchedNeighbors.count()

        return neighborCount - neighborWallCount <= 1
    }

    private fun gapsRemaining(wallsReqd: Int, line: List<Point>): Int {
        val wallsLeftToPlace = wallsReqd - line.count{ it.type.eq(CellType.WALL) }
        val unknowns = line.count{!it.type.known}
        return unknowns - wallsLeftToPlace
    }
}