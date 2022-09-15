package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point

//If the total number of walls in a row or column matches the number of walls required, every other CellType
//in that row or column must be free
class WallsExhausted : Rule {
    override fun name() = "WallsExhausted"
    override fun apply(board: Board): ApplyResult {

        fun rule(row: List<Point>, rowIdx: Int, board: Board): Rule.Check? {
            val rowWalls = row.count { it.type.eq(Type.WALL) }
            val rowPotentialWalls = row.count { it.type.canBe(Type.WALL) && !it.type.known }
            if (rowWalls == board.rowReqs[rowIdx] && rowPotentialWalls > 0) {
                val toUpdate = row.filter{ it.type.canBe(Type.WALL) && !it.type.known }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - Type.WALL)) }
                val update = board.update(toUpdate)
                return Rule.Check(update, "row[$rowIdx]")
            }
            return null
        }

        return eachStripe(board, ::rule)
    }
}