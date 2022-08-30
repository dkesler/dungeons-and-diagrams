package rules

import game.Board
import game.CellType
import game.Point
import game.findTreasureRoomStartingAt
import utils.Box
import utils.TreasureRoom

//If a treasure room cannot expand in a direction and any neighbors on that side could be treasure room
//remove their ability to be a treasure room
class TreasureRoomCannotExpand : Rule {
    override fun name() = "TreasureRoomCannotExpand"

    override fun apply(board: Board): ApplyResult {
        for (treasure in board.treasures) {
            val toUpdate = mutableSetOf<Point>()
            val treasureRoomPoints = findTreasureRoomStartingAt(treasure.first, treasure.second, board.grid)
            val treasureRoom = TreasureRoom(Box.fromPoints(treasureRoomPoints))

            if (treasureRoom.cannotExpandLeft(board, 1)) {
                treasureRoom.box.leftNeighbors().map {Point(it.first, it.second, board.grid[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandRight(board, 1)) {
                treasureRoom.box.rightNeighbors(board.grid[0].size).map {Point(it.first, it.second, board.grid[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandDown(board, 1)) {
                treasureRoom.box.downNeighbors(board.grid.size).map {Point(it.first, it.second, board.grid[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (treasureRoom.cannotExpandUp(board, 1)) {
                treasureRoom.box.upNeighbors().map {Point(it.first, it.second, board.grid[it.first][it.second]) }
                    .filter { it.type.canBe(CellType.TREASURE_ROOM) }
                    .forEach { toUpdate.add(it) }
            }

            if (toUpdate.isNotEmpty()) {
                var b = board
                for (cell in toUpdate) {
                    val update = b.update(cell.row, cell.col, cell.type.types - CellType.TREASURE_ROOM)
                    if (!update.valid) {
                        return ApplyResult(true, true, name(), "", b)
                    }
                    b = update.board
                }
                return ApplyResult(true, false, name(), "${name()}.row[${treasure.first}].col[${treasure.second}]", b)
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}