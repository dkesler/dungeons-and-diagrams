package rules

import game.Board
import game.CellType
import game.neighborsWithTypes

class MonsterMayHaveAtMostOneHallway : Rule {
    override fun name() = "MonsterMayHaveAtMostOneHallway"
    //If a monster is adjacent to a hallway, every other neighbor must be wall
    override fun apply(board: Board): ApplyResult {
        for (monster in board.monsters) {
            val neighborsWithTypes = neighborsWithTypes(monster.first, monster.second, board.grid)
            val numAdjacentHall = neighborsWithTypes.count{it.type.eq(CellType.HALL) }
            val adjacentUnknown = neighborsWithTypes.filter{it.type.canBe(CellType.HALL, CellType.TREASURE_ROOM) && !it.type.known }
            //If the monster has an adjacent empty, all other unknown neighbors must be wall
            if (numAdjacentHall == 1 && adjacentUnknown.isNotEmpty()) {
                var b = board
                for (point in adjacentUnknown) {
                    val update = b.update(point.row, point.col, point.type.types - setOf(CellType.HALL, CellType.TREASURE_ROOM))
                    if (!update.valid) {
                        return ApplyResult(true, true, name(), "${name()}.row[${monster.first}].col[${monster.second}]", board)
                    }
                    b = update.board
                }
                return ApplyResult(true, false, name(), "${name()}.row[${monster.first}].col[${monster.second}]", b)
            }
        }
        return ApplyResult(false, false, name(), "", board)
    }
}