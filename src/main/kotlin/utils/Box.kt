package utils

data class Box(val minRow: Int, val minCol: Int, val maxRow: Int, val maxCol: Int) {
    fun contains(row: Int, col: Int): Boolean {
        return row in minRow..maxRow && col in minCol..maxCol
    }

    fun contains(point: Pair<Int, Int>): Boolean {
        return contains(point.first, point.second)
    }

    fun points(): List<Pair<Int, Int>> {
        return (minRow..maxRow).flatMap { row ->
            (minCol..maxCol).map{ col ->
                Pair(row, col)
            }
        }
    }

    fun leftNeighbors(): List<Pair<Int, Int>> {
        return (minRow..maxRow).map{ Pair(it, minCol-1) }.filter{it.second >= 0 }
    }

    fun rightNeighbors(cols: Int): List<Pair<Int, Int>> {
        return (minRow..maxRow).map{ Pair(it, maxCol+1) }.filter{it.second < cols }
    }

    fun upNeighbors(): List<Pair<Int, Int>> {
        return (minCol..maxCol).map{ Pair(minRow-1, it) }.filter{it.first >= 0 }
    }

    fun downNeighbors(rows: Int): List<Pair<Int, Int>> {
        return (minCol..maxCol).map{ Pair(maxRow+1, it) }.filter{it.first < rows }
    }

    fun width(): Int {
        return maxCol - minCol + 1
    }

    fun height(): Int {
        return maxRow - minRow + 1
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