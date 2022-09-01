package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Box

class EmptyishTwoByTwoIsTreasureRoom : Rule {
    override fun name() = "EmptyishTwoByTwoIsTreasureRoom"
    override fun apply(board: Board): ApplyResult {
        //if any 2x2 area contains solely empty, treasure room, or treasure, each empty must be treasure room
        for (row in (0 until board.grid.maxRow)) {
            for (col in (0 until board.grid.maxCol)) {
                val box = Box(row, col, row + 1, col + 1)
                val subGrid = board.subgrid(box).flatten()
                if (containsAtLeastOnePossibleHall(subGrid) && isEmptyish(subGrid)) {
                    return emptyToTreasureRoom(box, board)
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }


    private fun emptyToTreasureRoom(box: Box, board: Board): ApplyResult {
        var b = board
        for (point in box.points()) {
            val cell = board.grid.cells[point.first][point.second]
            if (cell.canBe(CellType.HALL)) {
                val update = b.update(point.first, point.second, cell.types - CellType.HALL)
                if (!update.valid) {
                    return ApplyResult(true, true, name(), "${name()}.row[${box.minRow}].col[${box.minCol}]", b)
                } else {
                    b = update.board
                }
            }
        }
        return ApplyResult(true, false,name(), "${name()}.row[${box.minRow}].col[${box.minCol}]", b)
    }

    private fun isEmptyish(subGrid: List<TypeRange>): Boolean {
        return subGrid.all { it.cannotBe(CellType.WALL, CellType.MONSTER) }
    }
    private fun containsAtLeastOnePossibleHall(subGrid: List<TypeRange>): Boolean {
        return subGrid.any { it.canBe(CellType.HALL) }
    }
}