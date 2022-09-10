package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

class MonsterMayHaveAtMostOneHallway : Rule {
    override fun name() = "MonsterMayHaveAtMostOneHallway"
    //If a monster is adjacent to a hallway, every other neighbor must be wall

    override fun apply(board: Board): ApplyResult {

        fun rule(monster: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(monster.row, monster.col)
            val numAdjacentHall = neighbors.count{!it.type.canBe(CellType.WALL) }
            val adjacentUnknown = neighbors.filter{it.type.canBe(CellType.HALLWAY, CellType.ROOM) && it.type.canBe(CellType.WALL) && !it.type.known }
            //If the monster has an adjacent empty, all other unknown neighbors must be wall
            if (numAdjacentHall == 1 && adjacentUnknown.isNotEmpty()) {
                val toUpdate = adjacentUnknown.map{ Point(it.row, it.col, TypeRange(it.type.types - setOf(CellType.HALLWAY, CellType.ROOM))) }
                val update = board.update(toUpdate)
                return Rule.Check(update, "row[${monster.row}].col[${monster.col}]")
            }
            return null
        }
        return eachMonster(board, ::rule)
    }
}