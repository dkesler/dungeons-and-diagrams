package rules

import game.Board
import game.CellType
import game.neighborsWithTypes

class MonsterRequiresHallway : Rule {
    override fun name() = "MonsterRequiresHallway"
    override fun apply(board: Board): ApplyResult {
        for (monster in board.monsters) {
            val neighborsWithTypes = neighborsWithTypes(monster.first, monster.second, board.grid)
            val numAdjacentHall = neighborsWithTypes.count{it.type.eq(CellType.HALL)}
            val adjacentPossibleHall = neighborsWithTypes.filter{it.type.canBe(CellType.HALL) }
            //If the monster does not already have an adjacent hall, and there is only one neighbor that could
            //be a hall, it must be a hall
            if (numAdjacentHall == 0 && adjacentPossibleHall.count() == 1) {
                val update = board.update(adjacentPossibleHall.first().row, adjacentPossibleHall.first().col, setOf(CellType.HALL))
                if (update.valid) {
                    return ApplyResult(true, false, name(), "${name()}.row[${monster.first}].col[${monster.second}]", update.board)
                } else {
                    return ApplyResult(true, true, name(), "${name()}.row[${monster.first}].col[${monster.second}]", update.board)
                }
            }
        }
        return ApplyResult(false, false, name(), "", board)
    }
}