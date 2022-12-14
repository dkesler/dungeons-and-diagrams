package utils

import kotlin.math.max
import kotlin.math.min

data class Box(val minRow: Int, val minCol: Int, val maxRow: Int, val maxCol: Int) {
    fun contains(row: Int, col: Int): Boolean {
        return row in minRow..maxRow && col in minCol..maxCol
    }

    fun contains(point: Pair<Int, Int>): Boolean {
        return contains(point.first, point.second)
    }

    fun containsRow(rowIdx: Int): Boolean {
        return rowIdx in minRow..maxRow
    }

    fun containsCol(colIdx: Int): Boolean {
        return colIdx in minCol..maxCol
    }

    fun points(): List<Pair<Int, Int>> {
        return (minRow..maxRow).flatMap { row ->
            (minCol..maxCol).map{ col ->
                Pair(row, col)
            }
        }
    }

    fun width(): Int {
        return maxCol - minCol + 1
    }

    fun height(): Int {
        return maxRow - minRow + 1
    }

    fun overlaps(box: Box): Boolean {
        return box.points().any { contains(it) }
    }

    fun merge(other: Box): Box {
        return Box(
            min(this.minRow, other.minRow),
            min(this.minCol, other.minCol),
            max(this.maxRow, other.maxRow),
            max(this.maxCol, other.maxCol)
        )
    }

    companion object {
        fun fromPoints(points: Collection<Pair<Int, Int>>): Box {
            val minRow = points.minOfOrNull { it.first }!!
            val maxRow = points.maxOfOrNull { it.first }!!
            val minCol = points.minOfOrNull { it.second }!!
            val maxCol = points.maxOfOrNull { it.second }!!
            return Box(minRow, minCol, maxRow, maxCol)
        }
    }
}