package utils

import game.Board
import game.CellType
import kotlin.math.max
import kotlin.math.min

data class WallBoundBox(val box: Box, val rowReqs: List<IntRange>, val colReqs: List<IntRange>) {
    val minWalls = run {
        val minByRow = rowReqs.sumOf { it.first }
        val minByCol = colReqs.sumOf { it.first }
        max(minByRow, minByCol)
    }

    val maxWalls = run {
        val maxByRow = rowReqs.sumOf { it.last }
        val maxByCol = colReqs.sumOf { it.last }
        min(maxByRow, maxByCol)
    }

    companion object {
        fun fromBox(box: Box, board: Board): WallBoundBox {
            val rowReqs = mutableListOf<IntRange>()
            for (rowIdx in (box.minRow..box.maxRow)) {
                val row = board.grid.row(rowIdx)
                //if we cram as many walls elsewhere in the row, how many walls do we still need to put in the box
                val wbbRowMin = board.rowReqs[rowIdx] - row.filterIndexed{ colIdx, space -> !box.contains(rowIdx, colIdx) && space.type.canBe(CellType.WALL)}.count()
                //how many walls can we cram in the box.  it will be limited by the number of available slots in the row within the box
                //and the number of required walls for the row minus the number of placed walls
                val wbbRowMax = min(
                    row.filterIndexed{ colIdx, space -> box.contains(rowIdx, colIdx) && space.type.canBe(CellType.WALL)}.count(),
                    board.rowReqs[rowIdx] - row.filterIndexed{ colIdx, space -> !box.contains(rowIdx, colIdx) && space.type.eq(CellType.WALL)}.count()
                )
                rowReqs.add((wbbRowMin..wbbRowMax))
            }

            val colReqs = mutableListOf<IntRange>()
            for (colIdx in (box.minCol..box.maxCol)) {
                val col = board.grid.col(colIdx)
                //if we cram as many walls elsewhere in the col, how many walls do we still need to put in the box
                val wbbColMin = board.colReqs[colIdx] - col.filterIndexed{ rowIdx, space -> !box.contains(rowIdx, colIdx) && space.type.canBe(CellType.WALL)}.count()
                //how many walls can we cram in the box.  it will be limited by the number of available slots in the col and the number of required walls for the col minus the number of placed walls
                val wbbColMax = min(
                    col.filterIndexed{ rowIdx, space -> box.contains(rowIdx, colIdx) && space.type.canBe(CellType.WALL)}.count(),
                    board.colReqs[colIdx] - col.filterIndexed{ rowIdx, space -> !box.contains(rowIdx, colIdx) && space.type.eq(CellType.WALL)}.count()
                )
                colReqs.add((wbbColMin..wbbColMax))
            }

            return WallBoundBox(box, rowReqs, colReqs)
        }
    }
}
