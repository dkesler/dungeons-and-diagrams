package rules

import game.Board
import game.CellType
import game.TypeRange
import game.findTreasureRoomStartingAt
import utils.Box
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
                treasureRoom.box.leftNeighbors().map {Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandRight(board, 1)) {
                treasureRoom.box.rightNeighbors(board.grid.numCols).map { Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandDown(board, 1)) {
                treasureRoom.box.downNeighbors(board.grid.numRows).map {Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandUp(board, 1)) {
                treasureRoom.box.upNeighbors().map {Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (toUpdate.isNotEmpty()) {
                val update = board.update(
                    toUpdate.map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.TREASURE_ROOM)) }
                )
                return Rule.Check(update, "row[${treasureRoom.box.minRow}].col[${treasureRoom.box.minCol}]")
            }

            return null
        }
        return eachTreasureRoom(board, ::rule)
    }
}