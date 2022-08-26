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