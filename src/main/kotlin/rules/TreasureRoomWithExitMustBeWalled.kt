package rules

import Board
import Point
import findTreasureRoomStartingAt
import getTreasureRoomNeighbors
import utils.Box
import utils.TreasureRoom

class TreasureRoomWithExitMustBeWalled : Rule {
    override fun name() = "TreasureRoomWithExitMustBeWalled"
    override fun apply(board: Board): ApplyResult {
        for (treasure in board.treasures) {
            val treasureRoomPoints = findTreasureRoomStartingAt(treasure.first, treasure.second, board.grid)
            val treasureRoom = TreasureRoom(Box.fromPoints(treasureRoomPoints))
            val treasureRoomNeighbors = getTreasureRoomNeighbors(treasureRoom, board.grid)
            val treasureRoomNeighborTypes = treasureRoomNeighbors.map{board.grid[it.first][it.second]}
            //This treasure room has an exit.  if we cannot expand in a given direction, all unknown neighbors in that
            //direction must be a wall
            if (Space.HALL in treasureRoomNeighborTypes) {
                val toUpdate = mutableSetOf<Point>()
                if (treasureRoom.cannotExpandDown(board, 1)) {
                    val downNeighbors = treasureRoom.box.downNeighbors(board.grid.size)
                        .map{ Point(it.first, it.second, board.grid[it.first][it.second]) }
                    toUpdate.addAll(downNeighbors.filter { it.type == Space.UNKNOWN })
                }
                if (treasureRoom.cannotExpandUp(board, 1)) {
                    val upNeighbors = treasureRoom.box.upNeighbors()
                        .map{ Point(it.first, it.second, board.grid[it.first][it.second]) }
                    toUpdate.addAll(upNeighbors.filter { it.type == Space.UNKNOWN })
                }
                if (treasureRoom.cannotExpandLeft(board, 1)) {
                    val leftNeighbors = treasureRoom.box.leftNeighbors()
                        .map{ Point(it.first, it.second, board.grid[it.first][it.second]) }
                    toUpdate.addAll(leftNeighbors.filter { it.type == Space.UNKNOWN })
                }
                if (treasureRoom.cannotExpandRight(board, 1)) {
                    val rightNeighbors = treasureRoom.box.rightNeighbors(board.grid[0].size)
                        .map{ Point(it.first, it.second, board.grid[it.first][it.second]) }
                    toUpdate.addAll(rightNeighbors.filter { it.type == Space.UNKNOWN })
                }
                if (toUpdate.isNotEmpty()) {
                    var b = board
                    for(point in toUpdate) {
                        val update = b.update(point.row, point.col, Space.WALL)
                        if (!update.valid) {
                            return ApplyResult(true, true, name(), "${name()}.row[${treasure.first}].col[${treasure.second}]", board)
                        }
                        b = update.board
                    }
                    return ApplyResult(true, false, name(), "${name()}.row[${treasure.first}].col[${treasure.second}]", b)
                }
            }

        }

        return ApplyResult(false, false, name(), "", board)
    }
}