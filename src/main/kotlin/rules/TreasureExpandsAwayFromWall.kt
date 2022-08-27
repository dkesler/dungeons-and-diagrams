package rules

import Board
import Space
import findTreasureRoomStartingAt
import utils.Box
import utils.TreasureRoom

class TreasureExpandsAwayFromWall : Rule {
    override fun name() = "TreasureExpandsAwayFromWall"
    override fun apply(board: Board): ApplyResult {
        //if a wall < 3 squares away from a treasure, the treasure room must expand away from the wall
        for (treasure in board.treasures) {
            val toUpdate = mutableSetOf<Pair<Int, Int>>()
            val treasureRoomPoints = findTreasureRoomStartingAt(treasure.first, treasure.second, board.grid)
            val treasureRoom = TreasureRoom(Box.fromPoints(treasureRoomPoints))

            //if we can't expand left, then we must expand right
            if (treasureRoom.cannotExpandLeft(board, 1)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.minCol+2)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            } else if (treasureRoom.cannotExpandLeft(board, 2)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.minCol+1)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            }

            //if we can't expand right, then we must expand left
            if (treasureRoom.cannotExpandRight( board, 1)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.maxCol-2, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            } else if (treasureRoom.cannotExpandRight( board, 2)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.maxCol-1, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            }

            //if we can't expand down, then we must expand up
            if (treasureRoom.cannotExpandDown( board, 1)) {
                val newTreasureRoom = Box(treasureRoom.maxRow-2, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            } else if (treasureRoom.cannotExpandDown( board, 2)) {
                val newTreasureRoom = Box(treasureRoom.maxRow-1, treasureRoom.minCol, treasureRoom.maxRow, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            }

            //if we can't expand up, then we must expand down
            if (treasureRoom.cannotExpandUp( board, 1)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.minRow+2, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            } else if (treasureRoom.cannotExpandUp(board, 2)) {
                val newTreasureRoom = Box(treasureRoom.minRow, treasureRoom.minCol, treasureRoom.minRow+1, treasureRoom.maxCol)
                val pointsToUpdate = (newTreasureRoom.points() - treasureRoomPoints)
                    .filter{board.grid[it.first][it.second] != Space.TREASURE_ROOM }
                toUpdate.addAll(pointsToUpdate)
            }


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