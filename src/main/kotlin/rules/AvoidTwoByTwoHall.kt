package rules

import game.Board
import game.CellType
import utils.Box
import java.lang.RuntimeException

class AvoidTwoByTwoHall : Rule {
    override fun apply(board: Board): ApplyResult {
        fun rule(box: Box): Rule.Check? {
            val subGrid = board.grid.subgrid(box).flatten()
            val has3Halls = subGrid.count { it.type.eq(CellType.HALL) } == 3
            val hasUnknownThatCouldBeHall = subGrid.count { it.type.canBe(CellType.HALL, CellType.TREASURE_ROOM) && !it.type.known } == 1
            if (has3Halls && hasUnknownThatCouldBeHall) {
                val toUpdate = subGrid.first{ !it.type.known }
                return Rule.Check(
                    board.update(
                        toUpdate.row, toUpdate.col, toUpdate.type.types - setOf(CellType.HALL, CellType.TREASURE_ROOM)
                    ),
                    ".row[${toUpdate.row}].col[${toUpdate.col}]"
                )
            }
            return null
        }
        return eachTwoByTwo(board, ::rule)
    }

    override fun name() = "AvoidTwoByTwoHall"
}