package rules

import Board
import neighborsWithTypes

class MonsterMayHaveAtMostOneHallway : Rule {
    override fun name() = "MonsterMayHaveAtMostOneHallway"
    override fun apply(board: Board): ApplyResult {
        for (monster in board.monsters) {
            val neighborsWithTypes = neighborsWithTypes(monster.first, monster.second, board.grid)
            val numAdjacentEmpty = neighborsWithTypes.count{it.type == Space.EMPTY || it.type == Space.HALL }
            val adjacentUnknown = neighborsWithTypes.filter{it.type == Space.UNKNOWN }
            //If the monster has an adjacent empty, all other unknown neighbors must be wall
            if (numAdjacentEmpty == 1 && adjacentUnknown.isNotEmpty()) {
                var b = board
                for (point in adjacentUnknown) {
                    val update = b.update(point.row, point.col, Space.WALL)
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