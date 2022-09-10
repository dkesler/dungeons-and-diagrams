package rules

import game.Board
import game.Type
import game.findTreasureRoomStartingAt
import utils.Box
import utils.Point
import kotlin.math.max
import kotlin.math.min



class CantReachTreasure : Rule {
    override fun name() = "CantReachTreasure"
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

        fun rule(point: Point): Rule.Check? {
            val treasureHuntingBoundingBox = Box(
                max(0, point.row-3),
                max(0, point.col-3),
                min(board.grid.maxRow, point.row+3),
                min(board.grid.maxCol, point.col+3)
            )
            //Treasures we can even consider given the 3x3 max treasure room size rule
            val candidateTreasures = board.treasures.filter(treasureHuntingBoundingBox::contains)
            //If the current space cannot feasibly reach any treasure, it cannot be a treasure room
            if (candidateTreasures.none{ canFeasiblyReachTreasure(point.row, point.col, it)}) {
                val update = board.update(point.row, point.col, point.type.types - Type.ROOM)
                return Rule.Check(update, ".row[$${point.row}].col[${point.col}]")
            }
            return null
        }

        return each(
            board,
            {it.type.canBe(Type.ROOM)},
            ::rule
        )
    }
}