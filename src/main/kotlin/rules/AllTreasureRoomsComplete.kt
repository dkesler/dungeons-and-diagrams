package rules

import game.Board
import game.CellType
import game.getAllTreasureRooms

class AllTreasureRoomsComplete : Rule {
    override fun name() = "AllTreasureRoomsComplete"

    override fun apply(board: Board): ApplyResult {
        val treasureRooms = getAllTreasureRooms(board.grid)
        val allTreasureRoomsComplete = treasureRooms.all{ it.box.width() == 3 && it.box.height() == 3}
        if (allTreasureRoomsComplete) {
            val anyUnknownsThatCanBeTreasureRoom = mutableSetOf<Pair<Int, Int>>()
            for (rowIdx in board.grid.rows) {
                for (colIdx in board.grid.cols) {
                    val cell = board.grid.cells[rowIdx][colIdx]
                    if (!cell.known && cell.canBe(CellType.TREASURE_ROOM)) {
                        anyUnknownsThatCanBeTreasureRoom.add(Pair(rowIdx, colIdx))
                    }
                }
            }

            if (anyUnknownsThatCanBeTreasureRoom.isNotEmpty()) {
                var b = board
                for (cell in anyUnknownsThatCanBeTreasureRoom) {
                    val update = b.update(cell.first, cell.second, board.grid.cells[cell.first][cell.second].types - CellType.TREASURE_ROOM)
                    if (!update.valid) {
                        return ApplyResult(true, true, name(), "", b)
                    }
                    b = update.board
                }
                return ApplyResult(true, false, name(), name(), b)
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }
}