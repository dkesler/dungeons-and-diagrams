package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

class MonsterCantTouchTreasureRoom : Rule {
    override fun name() = "MonsterCantTouchTreasureRoom"

    override fun apply(board: Board): ApplyResult {

        fun rule(monster: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(monster.row, monster.col)
            if (neighbors.any { it.type.canBe(CellType.ROOM)}) {
                val toUpdate = neighbors.filter {it.type.canBe(CellType.ROOM) }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.ROOM)) }
                return Rule.Check(board.update(toUpdate), "row[${monster.row}].col[${monster.col}]")
            }
            return null
        }

        return eachMonster(board, ::rule)
    }
}