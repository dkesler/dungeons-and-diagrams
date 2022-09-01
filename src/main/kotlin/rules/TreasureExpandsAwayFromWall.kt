package rules

import game.Board
import game.CellType
import game.findTreasureRoomStartingAt
import utils.Box
import utils.TreasureRoom

class TreasureExpandsAwayFromWall : Rule {
    override fun name() = "TreasureExpandsAwayFromWall"
    override fun apply(board: Board): ApplyResult {
        //if a wall < 3 squares away from a treasure, the treasure room must expand away from the wall
        for (treasure in board.treasures) {
            val toUpdate = mutableSetOf<Pair<Int, Int>>()
            val treasureRoomPoints = findTreasureRoomStartingAt(treasure.first, treasure.second, board.grid)
            val box = Box.fromPoints(treasureRoomPoints)
            val treasureRoom = TreasureRoom(box)

            //if we can't expand left, then we must expand right
            if (box.minCol == 0 || !allCanBeTreasureRoom(box.leftNeighbors(), board)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.minCol+2)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            } else if (box.minCol == 1 || !allCanBeTreasureRoom(box.leftXNeighbors(2), board)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.minCol+1)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            }

            //if we can't expand right, then we must expand left
            if (box.maxCol == board.rowReqs.size - 1 || !allCanBeTreasureRoom(box.rightNeighbors(board.colReqs.size), board)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.maxCol-2, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            } else if (box.maxCol == board.rowReqs.size - 2 || !allCanBeTreasureRoom(box.rightXNeighbors(2, board.colReqs.size), board)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.maxCol-1, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            }

            //if we can't expand down, then we must expand up
            if (box.maxRow == board.rowReqs.size - 1 || !allCanBeTreasureRoom(box.downNeighbors(board.rowReqs.size), board)) {
                val newTreasureRoom = Box(treasureRoom.maxRow-2, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            } else if (box.maxRow == board.rowReqs.size - 2 || !allCanBeTreasureRoom(box.downXNeighbors(2, board.rowReqs.size), board)) {
                val newTreasureRoom = Box(treasureRoom.maxRow-1, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            }

            //if we can't expand up, then we must expand down
            if (box.minRow == 0 || !allCanBeTreasureRoom(box.upNeighbors(), board)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.minRow+2, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            } else if (box.minRow == 1 || !allCanBeTreasureRoom(box.upXNeighbors(2), board)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.minRow+1, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{!board.grid.cells[it.first][it.second].eq(CellType.TREASURE_ROOM) }
                toUpdate.addAll(pointsToUpdate)
            }


            if (toUpdate.isNotEmpty()) {
                var b = board
                for (point in toUpdate) {
                    val update = b.update(point.first, point.second, setOf(CellType.TREASURE_ROOM))
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

    private fun allCanBeTreasureRoom(neighbors: List<Pair<Int, Int>>, board: Board): Boolean {
        return neighbors.all { board.grid.cells[it.first][it.second].canBe(CellType.TREASURE_ROOM) }
    }
}