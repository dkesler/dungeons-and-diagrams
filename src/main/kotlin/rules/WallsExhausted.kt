package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

//If the total number of walls in a row or column matches the number of walls required, every other CellType
//in that row or column must be free
class WallsExhausted : Rule {
    override fun name() = "WallsExhausted"
    override fun apply(board: Board): ApplyResult {

        fun rowRule(row: List<Point>, rowIdx: Int): Rule.Check? {
            val rowWalls = row.count { it.type.eq(CellType.WALL) }
            val rowPotentialWalls = row.count { it.type.canBe(CellType.WALL) && !it.type.known }
            if (rowWalls == board.rowReqs[rowIdx] && rowPotentialWalls > 0) {
                val toUpdate = row.filter{ it.type.canBe(CellType.WALL) && !it.type.known }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.WALL)) }
                val update = board.update(toUpdate)
                return Rule.Check(update, "row[$rowIdx]")
            }
            return null
        }

        fun colRule(col: List<Point>, colIdx: Int): Rule.Check? {
            val colWalls = col.count { it.type.eq(CellType.WALL) }
            val colPotentialWalls = col.count { it.type.canBe(CellType.WALL) && !it.type.known }
            if (colWalls == board.colReqs[colIdx] && colPotentialWalls > 0) {
                val toUpdate = col.filter{ it.type.canBe(CellType.WALL) && !it.type.known }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.WALL)) }
                val update = board.update(toUpdate)
                return Rule.Check(update, "col[$colIdx]")
            }
            return null
        }
        return eachRowAndCol(board, ::rowRule, ::colRule)
    }
}