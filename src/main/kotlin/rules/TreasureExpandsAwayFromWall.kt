package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Box
import utils.Point
import utils.TreasureRoom

class TreasureExpandsAwayFromWall : Rule {
    override fun name() = "TreasureExpandsAwayFromWall"
    override fun apply(board: Board): ApplyResult {
        //if a wall < 3 squares away from a treasure, the treasure room must expand away from the wall
        fun rule(treasureRoom: TreasureRoom): Rule.Check? {
            val toUpdate = mutableSetOf<Pair<Int, Int>>()
            val box = treasureRoom.box
            val treasureRoomPoints = box.points().toSet()

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
                val update = board.update(toUpdate.map { Point(it.first, it.second, TypeRange(setOf(CellType.TREASURE_ROOM))) })
                return Rule.Check(update, "row[${box.minRow}].col[${box.minCol}]")
            }
            return null
        }
        return eachTreasureRoom(board, ::rule)
    }

    private fun allCanBeTreasureRoom(neighbors: List<Pair<Int, Int>>, board: Board): Boolean {
        return neighbors.all { board.grid.cells[it.first][it.second].canBe(CellType.TREASURE_ROOM) }
    }
}