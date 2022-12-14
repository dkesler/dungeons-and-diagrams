package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point

class MonsterRequiresHallway : Rule {
    override fun name() = "MonsterRequiresHallway"
    override fun apply(board: Board): ApplyResult {
        fun rule(monster: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(monster.row, monster.col)
            val numAdjacentHall = neighbors.count{it.type.eq(Type.HALLWAY)}
            val adjacentPossibleHall = neighbors.filter{it.type.canBe(Type.HALLWAY) }
            //If the monster does not already have an adjacent hall, and there is only one neighbor that could
            //be a hall, it must be a hall
            if (numAdjacentHall == 0 && adjacentPossibleHall.count() == 1) {
                val toUpdate = adjacentPossibleHall.map{ Point(it.row, it.col, TypeRange(setOf(Type.HALLWAY))) }
                return Rule.Check(board.update(toUpdate), "row[${monster.row}].col[${monster.col}]")
            }
            return null
        }
        return eachMonster(board, ::rule)
    }
}