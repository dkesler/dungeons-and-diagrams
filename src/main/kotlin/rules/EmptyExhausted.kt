package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point

//If the number of unknowns in a column equal the number of walls required minues the number of current walls
//all unknowns must be walls
class EmptyExhausted : Rule {
    override fun name() = "EmptyExhausted"
    override fun apply(board: Board): ApplyResult {
        return eachStripe(
            board
        ) { row, rowIdx, b ->
            val rowWalls = row.count { it.type.eq(Type.WALL) }
            val rowPotentialWalls = row.filter { it.type.canBe(Type.WALL) && !it.type.known }
            if (rowPotentialWalls.size == b.rowReqs[rowIdx] - rowWalls && rowPotentialWalls.isNotEmpty()) {
                val toUpdate = rowPotentialWalls.map { Point(it.row, it.col, TypeRange(setOf(Type.WALL))) }
                val update = b.update(toUpdate)
                Rule.Check(update, "row[${rowIdx}]")
            } else {
                null
            }
        }
    }
}