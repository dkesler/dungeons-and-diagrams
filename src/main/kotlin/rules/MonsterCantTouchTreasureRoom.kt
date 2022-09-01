package rules

import game.Board
import game.CellType

class MonsterCantTouchTreasureRoom : Rule {
    override fun name() = "MonsterCantTouchTreasureRoom"

    override fun apply(board: Board): ApplyResult {
        for (monster in board.monsters) {
            val neighbors = board.grid.neighbors(monster.first, monster.second)
            if (neighbors.any { it.type.canBe(CellType.TREASURE_ROOM)}) {
                var b = board
                for (neighbor in neighbors.filter { it.type.canBe(CellType.TREASURE_ROOM) }) {
                    val update = b.update(neighbor.row, neighbor.col, neighbor.type.types - CellType.TREASURE_ROOM)
                    if (!update.valid) {
                        return ApplyResult(true, true, name(), "", b)
                    }
                    b = update.board
                }
                return ApplyResult(true, false, name(), "${name()}.row[${monster.first}].col[${monster.second}]", b)
            }
        }
        return ApplyResult(false, false, name(), "", board)
    }
}