package game

class Grid(val cells: List<List<TypeRange>>) {
    val numRows = cells.size
    val rows = (0 until numRows)
    val maxRow = numRows-1
    val numCols = cells[0].size
    val cols = (0 until numCols)
    val maxCol = numCols-1
}