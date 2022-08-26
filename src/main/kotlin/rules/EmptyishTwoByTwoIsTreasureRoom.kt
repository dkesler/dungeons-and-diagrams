package rules

import Board
import Space
import utils.Box

class EmptyishTwoByTwoIsTreasureRoom : Rule {
    override fun apply(board: Board): ApplyResult {
        //if any 2x2 area contains solely empty, treasure room, or treasure, each empty must be treasure room
        for (row in (0 until board.grid.size-1)) {
            for (col in (0 until board.grid[0].size-1)) {
                val box = Box(row, col, row + 1, col + 1)
                val subGrid = board.subgrid(box).flatten()
                if (containsAtLeastOneEmpty(subGrid) && isEmptyish(subGrid)) {
                    return emptyToTreasureRoom(box, board)
                }
            }
        }

        return ApplyResult(false, "EmptyishTwoByTwoIsTreasureRoom", "", board)
    }


    private fun emptyToTreasureRoom(box: Box, board: Board): ApplyResult {
        var b = board
        for (point in box.points()) {
            if (board.grid[point.first][point.second] == Space.EMPTY) {
                val update = b.update(point.first, point.second, Space.TREASURE_ROOM)
                if (!update.valid) {
                    throw RuntimeException("Invalid update in EmptyishTwoByTwoIsTreasureRoom: ${update.invalidReason}")
                } else {
                    b = update.board
                }
            }
        }
        return ApplyResult(true, "EmptyishTwoByTwoIsTreasureRoom", "EmptyishTwoByTwoIsTreasureRoom.row[${box.minRow}].col[${box.minCol}]", b)
    }

    private fun isEmptyish(subGrid: List<Space>): Boolean {
        val emptyishTypes = setOf(Space.EMPTY, Space.TREASURE_ROOM, Space.TREASURE)
        return subGrid.all { emptyishTypes.contains(it) }
    }
    private fun containsAtLeastOneEmpty(subGrid: List<Space>): Boolean {
        return subGrid.any { it == Space.EMPTY }
    }
}