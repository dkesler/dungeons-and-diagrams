package rules

import game.*

//Given a row/col with exactly one gap to place, if placing a gap in a cell would make it a dead end, it must be a wall instead
class LastGapCantCreateDeadend: Rule {
    override fun name() = "LastGapCantCreateDeadend"

    override fun apply(board: Board): ApplyResult {
        for (rowIdx in board.grid.rows) {
            val row = board.row(rowIdx)
            if (gapsRemaining(board.rowReqs[rowIdx], row) == 1) {
                val changeToWall = mutableSetOf<Pair<Int, Int>>()
                for (colIdx in board.grid.cols) {
                    if (!row[colIdx].known && row[colIdx].canBe(CellType.WALL) && wouldBeDeadEndAsGap(rowIdx, colIdx, board, true)) {
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
            val col = board.col(colIdx)
            if (gapsRemaining(board.colReqs[colIdx], col) == 1) {
                val changeToWall = mutableSetOf<Pair<Int, Int>>()
                for (rowIdx in board.grid.rows) {
                    if (!col[rowIdx].known && col[rowIdx].canBe(CellType.WALL) && wouldBeDeadEndAsGap(rowIdx, colIdx, board, false)) {
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
        val verticalNeighbors = verticalNeighbors(rowIdx, colIdx, board.grid.numRows)
        val horizontalNeighbors = horizontalNeighbors(rowIdx, colIdx, board.grid.numCols)
        val untouchedNeighbors = if (checkingRow) verticalNeighbors else horizontalNeighbors
        val touchedNeighbors = if (checkingRow) horizontalNeighbors else verticalNeighbors

        val neighborWallCount = untouchedNeighbors.count{ board.grid.cells[it.first][it.second].eq(CellType.WALL) } +
                touchedNeighbors.count{ board.grid.cells[it.first][it.second].canBe(CellType.WALL) }

        val neighborCount = untouchedNeighbors.count() + touchedNeighbors.count()

        return neighborCount - neighborWallCount <= 1
    }

    private fun gapsRemaining(wallsReqd: Int, line: List<TypeRange>): Int {
        val wallsLeftToPlace = wallsReqd - line.count{ it.eq(CellType.WALL) }
        val unknowns = line.count{!it.known}
        return unknowns - wallsLeftToPlace
    }
}