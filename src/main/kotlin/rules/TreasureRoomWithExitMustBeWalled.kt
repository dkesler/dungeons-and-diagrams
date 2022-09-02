package rules

import game.*
import utils.Box
import utils.Point
import utils.TreasureRoom

class TreasureRoomWithExitMustBeWalled : Rule {
    override fun name() = "TreasureRoomWithExitMustBeWalled"
    override fun apply(board: Board): ApplyResult {

        fun rule(treasureRoom: TreasureRoom): Rule.Check? {
            val treasureRoomNeighbors = getTreasureRoomNeighbors(treasureRoom, board.grid)
            val treasureRoomNeighborTypes = treasureRoomNeighbors.map{board.grid.cells[it.first][it.second]}
            //This treasure room has an exit.  if we cannot expand in a given direction, all unknown neighbors in that
            //direction must be a wall
            val treasureRoomHasExit = treasureRoomNeighborTypes.any { it.eq(CellType.HALL) }
            if (treasureRoomHasExit) {
                val toUpdate = mutableSetOf<Point>()
                if (treasureRoom.cannotExpandDown(board, 1)) {
                    val downNeighbors = treasureRoom.box.downNeighbors(board.grid.numRows)
                        .map{ Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    toUpdate.addAll(downNeighbors.filter { !it.type.known })
                }
                if (treasureRoom.cannotExpandUp(board, 1)) {
                    val upNeighbors = treasureRoom.box.upNeighbors()
                        .map{ Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    toUpdate.addAll(upNeighbors.filter { !it.type.known })
                }
                if (treasureRoom.cannotExpandLeft(board, 1)) {
                    val leftNeighbors = treasureRoom.box.leftNeighbors()
                        .map{ Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    toUpdate.addAll(leftNeighbors.filter { !it.type.known })
                }
                if (treasureRoom.cannotExpandRight(board, 1)) {
                    val rightNeighbors = treasureRoom.box.rightNeighbors(board.grid.numCols)
                        .map{ Point(it.first, it.second, board.grid.cells[it.first][it.second]) }
                    toUpdate.addAll(rightNeighbors.filter { !it.type.known })
                }
                if (toUpdate.isNotEmpty()) {
                    val update = board.update(
                        toUpdate.map{ Point(it.row, it.col, TypeRange(setOf(CellType.WALL))) }
                    )
                    return Rule.Check(update, ".row[${treasureRoom.box.minCol}].col[${treasureRoom.box.maxCol}]")
                }
            }
            return null
        }

        return eachTreasureRoom(board, ::rule)
    }
}