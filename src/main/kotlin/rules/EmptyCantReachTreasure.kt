package rules

import Board
import kotlin.math.max
import kotlin.math.min

private data class BoundingBox(val minRow: Int, val minCol: Int, val maxRow: Int, val maxCol: Int) {
    fun contains(row: Int, col: Int): Boolean {
        return row in minRow..maxRow && col in minCol..maxCol
    }

    fun contains(point: Pair<Int, Int>): Boolean {
        return contains(point.first, point.second)
    }

    fun points(): List<Pair<Int, Int>> {
        return (minRow..maxRow).flatMap { row ->
            (minCol..maxCol).map{ col ->
                Pair(row, col)
            }
        }
    }
}

class EmptyCantReachTreasure : Rule {

    override fun apply(board: Board): ApplyResult {
        fun canFeasiblyReachTreasure(row: Int, col: Int, candidate: Pair<Int, Int>): Boolean {
            //May be the full treasure room or just a slice of it, but it cannot contain hall, monster, or wall
            val candidateTreasureRoom = BoundingBox(
                min(row, candidate.first),
                min(col, candidate.second),
                max(row, candidate.first),
                max(row, candidate.second)
            )

            val candidateTreasureRoomPoints = candidateTreasureRoom.points()
            val candidateTreasureRoomTypes = candidateTreasureRoomPoints.map{board.grid[it.first][it.second]}.toSet()
            if (candidateTreasureRoomTypes.intersect(setOf(Space.WALL, Space.HALL, Space.MONSTER)).isEmpty()) {
                return true
            }

            return false
        }

        for (row in board.grid.indices) {
            for (col in board.grid[0].indices) {
                if (board.grid[row][col] == Space.EMPTY) {
                    val treasureHuntingBoundingBox = BoundingBox(
                        max(0, row-3),
                        max(0, col-3),
                        min(board.grid.size-1, row+3),
                        min(board.grid[0].size-1, col+3)
                    )
                    //Treasures we can even consider given the 3x3 max treasure room size rule
                    val candidateTreasures = board.treasures.filter(treasureHuntingBoundingBox::contains)
                    //If the current Empty space cannot feasibly reach any treasure, it must just be a hall
                    if (candidateTreasures.none{ canFeasiblyReachTreasure(row, col, it)}) {
                        val update = board.update(row, col, Space.HALL)
                        if (!update.valid) {
                            throw RuntimeException("Invalid update in EmptyCantReachTreasure: ${update.invalidReason}")
                        } else {
                            return ApplyResult(true, "EmptyCantReachTreasure", "EmptyCantReachTreasure.row[$row].col[$col]", update.board)
                        }
                    }
                }
            }
        }

        return ApplyResult(false, "EmptyCantReachTreasure", "", board)
    }
}