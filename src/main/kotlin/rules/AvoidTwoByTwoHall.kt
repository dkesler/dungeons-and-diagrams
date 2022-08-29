package rules

import game.Board
import game.CellType
import utils.Box
import java.lang.RuntimeException

class AvoidTwoByTwoHall : Rule {
    override fun apply(board: Board): ApplyResult {
        //if any 2x2 area contains 3 halls and 1 unknown, the unknown must be a wall
        for (row in (0 until board.grid.size-1)) {
            for (col in (0 until board.grid[0].size-1)) {
                val box = Box(row, col, row + 1, col + 1)
                val subGrid = board.subgrid(box).flatten()
                if (subGrid.count{it.eq(CellType.HALL) } == 3 && subGrid.count{it.canBe(CellType.HALL, CellType.TREASURE_ROOM) && !it.known} == 1) {
                    return unknownToWall(box, board)
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)

    }

    private fun unknownToWall(box: Box, board: Board): ApplyResult {
        for (point in box.points()) {
            val typeRange = board.grid[point.first][point.second]
            if (typeRange.canBe(CellType.HALL, CellType.TREASURE_ROOM) && !typeRange.known) {
                val update = board.update(point.first, point.second, typeRange.types - setOf(CellType.HALL, CellType.TREASURE_ROOM))
                if (!update.valid) {
                    return ApplyResult(true, true, name(), "", update.board)
                } else {
                    return ApplyResult(true, false, name(), "${name()}.row[${point.first}].col[${point.second}]", update.board)
                }
            }
        }
        throw RuntimeException("Couldn't find unkown")
    }

    override fun name() = "AvoidTwoByTwoHall"
}