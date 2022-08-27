package rules

import Board
import findTreasureRoomStartingAt
import utils.Box

class TreasureRoomCannotBeConcave : Rule {
    override fun name() = "TreasureRoomCannotBeConcave"
    override fun apply(board: Board): ApplyResult {
        for (treasure in board.treasures) {
            val treasureRoomPoints = findTreasureRoomStartingAt(treasure.first, treasure.second, board.grid)
            val treasureRoom = Box.fromPoints(treasureRoomPoints)
            val treasureRoomBoxPoints = treasureRoom.points()
            val toUpdate = treasureRoomBoxPoints - treasureRoomPoints

            if (toUpdate.isNotEmpty()) {
                var b = board
                for (point in toUpdate) {
                    val update = b.update(point.first, point.second, Space.TREASURE_ROOM)
                    if (!update.valid) {
                        return ApplyResult(true, true, name(), "${name()}.row[${treasure.first}].col[${treasure.second}]", board)
                    }
                    b = update.board
                }
                return ApplyResult(true, false, name(), "${name()}.row[${treasure.first}].col[${treasure.second}]", b)
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}