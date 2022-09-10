package rules

import game.*
import utils.Box
import utils.Point
import utils.TreasureRoom

class TreasureRoomWithExitMustBeWalled : Rule {
    override fun name() = "TreasureRoomWithExitMustBeWalled"
    override fun apply(board: Board): ApplyResult {

        fun rule(treasureRoom: TreasureRoom): Rule.Check? {
            val treasureRoomHorizontalNeighbors = board.grid.horizontalNeighbors(treasureRoom.box)
            val treasureRoomVerticalNeighbors = board.grid.verticalNeighbors(treasureRoom.box)
            val treasureRoomNeighbors = treasureRoomHorizontalNeighbors + treasureRoomVerticalNeighbors
            //This treasure room has an exit.  if we cannot expand in a given direction, all unknown neighbors in that
            //direction must be a wall
            val treasureRoomHasExit = treasureRoomNeighbors.any { it.type.eq(CellType.HALL) } ||
                    (treasureRoom.box.width() == 3 && treasureRoomHorizontalNeighbors.any{!it.type.canBe(CellType.WALL)}) ||
                    (treasureRoom.box.height() == 3 && treasureRoomVerticalNeighbors.any{!it.type.canBe(CellType.WALL)})
            if (treasureRoomHasExit) {
                val toUpdate = mutableSetOf<Point>()
                if (treasureRoom.cannotExpandDown(board, 1)) {
                    val downNeighbors = board.grid.downNeighbors(treasureRoom.box)
                    toUpdate.addAll(downNeighbors.filter { it.type.canBe(CellType.WALL) && !it.type.known })
                }
                if (treasureRoom.cannotExpandUp(board, 1)) {
                    val upNeighbors = board.grid.upNeighbors(treasureRoom.box)
                    toUpdate.addAll(upNeighbors.filter { it.type.canBe(CellType.WALL) && !it.type.known })
                }
                if (treasureRoom.cannotExpandLeft(board, 1)) {
                    val leftNeighbors = board.grid.leftNeighbors(treasureRoom.box)
                    toUpdate.addAll(leftNeighbors.filter { it.type.canBe(CellType.WALL) && !it.type.known })
                }
                if (treasureRoom.cannotExpandRight(board, 1)) {
                    val rightNeighbors = board.grid.rightNeighbors(treasureRoom.box)
                    toUpdate.addAll(rightNeighbors.filter { it.type.canBe(CellType.WALL) && !it.type.known })
                }
                if (toUpdate.isNotEmpty()) {
                    val update = board.update(
                        toUpdate.map{ Point(it.row, it.col, TypeRange(setOf(CellType.WALL))) }
                    )
                    return Rule.Check(update, "row[${treasureRoom.box.minCol}].col[${treasureRoom.box.maxCol}]")
                }
            }
            return null
        }

        return eachTreasureRoom(board, ::rule)
    }
}