package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Box
import utils.Point

class EmptyishTwoByTwoIsTreasureRoom : Rule {
    override fun name() = "EmptyishTwoByTwoIsTreasureRoom"
    override fun apply(board: Board): ApplyResult {

        //if any 2x2 area contains solely empty, treasure room, or treasure, each empty must be treasure room
        fun rule(box: Box): Rule.Check? {
            val subGrid = board.grid.subgrid(box).flatten()
            if (containsAtLeastOnePossibleHall(subGrid) && isEmptyish(subGrid)) {
                val toUpdate = subGrid.filter { it.type.canBe(CellType.HALL) }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.HALL)) }
                return Rule.Check(board.update(toUpdate), ".row[${box.minRow}].col[${box.minCol}]")
            }
            return null
        }

        return eachTwoByTwo(
            board,
            ::rule
        )
    }

    private fun isEmptyish(subGrid: List<Point>): Boolean {
        return subGrid.all { it.type.cannotBe(CellType.WALL, CellType.MONSTER) }
    }
    private fun containsAtLeastOnePossibleHall(subGrid: List<Point>): Boolean {
        return subGrid.any { it.type.canBe(CellType.HALL) }
    }
}