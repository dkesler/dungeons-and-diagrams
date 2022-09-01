package game

import utils.Point

class Grid(val cells: List<List<TypeRange>>) {
    //These mostly exist for readability of code using them
    val numRows = cells.size
    val rows = (0 until numRows)
    val maxRow = numRows-1
    val numCols = cells[0].size
    val cols = (0 until numCols)
    val maxCol = numCols-1

    fun neighbors(row: Int, col: Int): Set<Point> {
        val n = mutableSetOf<Point>()
        if (row-1 >= 0) {
            n.add(Point(row-1, col, cells[row-1][col]))
        }
        if (row+1 < numRows) {
            n.add(Point(row+1, col, cells[row+1][col]))
        }
        if (col-1 >= 0) {
            n.add(Point(row, col-1, cells[row][col-1]))
        }
        if (col+1 < numCols) {
            n.add(Point(row, col+1, cells[row][col+1]))
        }
        return n.toSet()
    }

    fun neighbors(pt: Pair<Int, Int>): Set<Point> {
        return neighbors(pt.first, pt.second)
    }
}