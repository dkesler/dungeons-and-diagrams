package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point
import utils.TreasureRoom

//If a treasure room cannot expand in a direction and any neighbors on that side could be treasure room
//remove their ability to be a treasure room
class TreasureRoomCannotExpand : Rule {
    override fun name() = "TreasureRoomCannotExpand"

    override fun apply(board: Board): ApplyResult {

        fun rule(treasureRoom: TreasureRoom): Rule.Check? {
            val toUpdate = mutableSetOf<Point>()

            if (treasureRoom.cannotExpandLeft(board, 1)) {
                board.grid.leftNeighbors(treasureRoom.box)
                    .filter { it.type.canBe(Type.ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandRight(board, 1)) {
                board.grid.rightNeighbors(treasureRoom.box)
                    .filter { it.type.canBe(Type.ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandDown(board, 1)) {
                board.grid.downNeighbors(treasureRoom.box)
                    .filter { it.type.canBe(Type.ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandUp(board, 1)) {
                board.grid.upNeighbors(treasureRoom.box)
                    .filter { it.type.canBe(Type.ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (toUpdate.isNotEmpty()) {
                val update = board.update(
                    toUpdate.map{ Point(it.row, it.col, TypeRange(it.type.types - Type.ROOM)) }
                )
                return Rule.Check(update, "row[${treasureRoom.box.minRow}].col[${treasureRoom.box.minCol}]")
            }

            return null
        }
        return eachTreasureRoom(board, ::rule)
    }
}