package rules

import Board
import findTreasureRoomStartingAt
import utils.Box
import kotlin.math.max
import kotlin.math.min



class EmptyCantReachTreasure : Rule {
    override fun name() = "EmptyCantReachTreasure"
    override fun apply(board: Board): ApplyResult {
        fun canFeasiblyReachTreasure(row: Int, col: Int, candidate: Pair<Int, Int>): Boolean {
            val candidateTreasureRoomPoints = findTreasureRoomStartingAt(candidate.first, candidate.second, board.grid)
            val candidateTreasureRoom = Box.fromPoints(candidateTreasureRoomPoints)

            //If the room dimensions would be too large with the empty point added, it can't reach the treasure
            val augmentedTreasureRoom = Box(
                min(row, candidateTreasureRoom.minRow),
                min(col, candidateTreasureRoom.minCol),
                max(row, candidateTreasureRoom.maxRow),
                max(col, candidateTreasureRoom.maxCol)
            )

            if (augmentedTreasureRoom.maxRow - augmentedTreasureRoom.minRow >= 3) {
                return false
            }

            if (augmentedTreasureRoom.maxCol - augmentedTreasureRoom.minCol >= 3) {
                return false
            }

            //TODO:  if the augmented treasure room would include a wall, hall, or monster, we can't reach the treasure

            return true
        }

        for (row in board.grid.indices) {
            for (col in board.grid[0].indices) {
                if (board.grid[row][col] == Space.EMPTY) {
                    val treasureHuntingBoundingBox = Box(
                        max(0, row-3),
                        max(0, col-3),
                        min(board.grid.size-1, row+3),
                        min(board.grid[0].size-1, col+3)
                    )
                    //Treasures we can even consider given the 3x3 max treasure room size rule
                    val candidateTreasures = board.treasures.filter(treasureHuntingBoundingBox::contains)
                    //If the current Empty space cannot feasibly reach any treasure, it must just be a hall
                    if (candidateTreasures.none{ canFeasiblyReachTreasure(row, col, it)}) {
                        val update = board.update(row, col, Space.HALL)
                        if (!update.valid) {
                            return ApplyResult(true, true, name(), "${name()}.row[$row].col[$col]", update.board)
                        } else {
                            return ApplyResult(true, false, name(), "${name()}.row[$row].col[$col]", update.board)
                        }
                    }
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}