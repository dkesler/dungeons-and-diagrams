package rules

import Board
import neighbors
import neighborsWithTypes

class MonsterRequiresHallway : Rule {
    override fun apply(board: Board): ApplyResult {
        for (monster in board.monsters) {
            val neighborsWithTypes = neighborsWithTypes(monster.first, monster.second, board.grid)
            val numAdjacentEmpty = neighborsWithTypes.count{it.type == Space.EMPTY || it.type == Space.HALL }
            val adjacentUnknown = neighborsWithTypes.filter{it.type == Space.UNKNOWN }
            if (numAdjacentEmpty == 0 && adjacentUnknown.count() == 1) {
                val update = board.update(adjacentUnknown.first().row, adjacentUnknown.first().col, Space.HALL)
                if (update.valid) {
                    return ApplyResult(true, false, "MonsterRequiresHallway", "MonsterRequiresHallway.row[${monster.first}].col[${monster.second}]", update.board)
                } else {
                    return ApplyResult(true, true, "MonsterRequiresHallway", "MonsterRequiresHallway.row[${monster.first}].col[${monster.second}]", update.board)
                }
            }
        }
        return ApplyResult(false, false, "MonsterRequiresHallway", "", board)
    }
}