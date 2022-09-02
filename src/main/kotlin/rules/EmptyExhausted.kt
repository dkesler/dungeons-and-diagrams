package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun name() = "EmptyExhausted"
    override fun apply(board: Board): ApplyResult {
        return eachRowAndCol(
            board,
            { row, rowIdx ->
                val rowWalls = row.count { it.type.eq(CellType.WALL) }
                val rowPotentialWalls = row.filter { it.type.canBe(CellType.WALL) && !it.type.known }
                if (rowPotentialWalls.size == board.rowReqs[rowIdx] - rowWalls && rowPotentialWalls.isNotEmpty()) {
                    val toUpdate = rowPotentialWalls.map { Point(it.row, it.col, TypeRange(setOf(CellType.WALL))) }
                    val update = board.update(toUpdate)
                    Rule.Check(update, "row[${rowIdx}]")
                } else {
                    null
                }
            },
            { col, colIdx ->
                val colWalls = col.count { it.type.eq(CellType.WALL) }
                val colPotentialWalls = col.filter { it.type.canBe(CellType.WALL) && !it.type.known }
                if (colPotentialWalls.size == board.colReqs[colIdx] - colWalls && colPotentialWalls.isNotEmpty()) {
                    val toUpdate = colPotentialWalls.map{ Point(it.row, it.col, TypeRange(setOf(CellType.WALL))) }
                    val update = board.update(toUpdate)
                    Rule.Check(update, "col[${colIdx}]")
                } else {
                    null
                }
            }
        )
    }
}