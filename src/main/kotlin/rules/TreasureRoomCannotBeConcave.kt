package rules

import game.Board
import game.CellType
import game.TypeRange
import game.findTreasureRoomStartingAt
import utils.Box
import utils.Point

class TreasureRoomCannotBeConcave : Rule {
    override fun name() = "TreasureRoomCannotBeConcave"
    override fun apply(board: Board): ApplyResult {
        for (treasure in board.treasures) {
            val treasureRoomPoints = findTreasureRoomStartingAt(treasure.first, treasure.second, board.grid)
            val treasureRoom = Box.fromPoints(treasureRoomPoints)
            val treasureRoomBoxPoints = treasureRoom.points()
            val toUpdate = treasureRoomBoxPoints - treasureRoomPoints

            if (toUpdate.isNotEmpty()) {
                val update = board.update( toUpdate.map{ Point(it.first, it.second, TypeRange(setOf(CellType.TREASURE_ROOM))) })
                return ApplyResult(true, !update.valid, name(), "${name()}.row[${treasure.first}].col[${treasure.second}]", update.board)
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}