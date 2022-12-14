package rules

import game.Board
import game.Type
import game.TypeRange
import game.getAllTreasureRooms
import utils.Point

class AllTreasureRoomsComplete : Rule {
    override fun name() = "AllTreasureRoomsComplete"

    override fun apply(board: Board): ApplyResult {
        val treasureRooms = getAllTreasureRooms(board.grid)
        val allTreasureRoomsComplete = treasureRooms.all{ it.box.width() == 3 && it.box.height() == 3}
        if (allTreasureRoomsComplete) {
            val anyUnknownsThatCanBeTreasureRoom = board.grid.points().filter {
                !it.type.known && it.type.canBe(Type.ROOM)
            }

            if (anyUnknownsThatCanBeTreasureRoom.isNotEmpty()) {
                return update(
                    board,
                    anyUnknownsThatCanBeTreasureRoom.map{ Point(it.row, it.col, TypeRange(it.type.types - Type.ROOM)) },
                    ""
                )
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}