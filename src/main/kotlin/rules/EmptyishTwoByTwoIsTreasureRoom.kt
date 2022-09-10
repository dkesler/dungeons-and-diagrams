package rules

import game.Board
import game.Type
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
                val toUpdate = subGrid.filter { it.type.canBe(Type.HALLWAY) }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - Type.HALLWAY)) }
                return Rule.Check(board.update(toUpdate), "row[${box.minRow}].col[${box.minCol}]")
            }
            return null
        }

        return eachTwoByTwo(
            board,
            ::rule
        )
    }

    private fun isEmptyish(subGrid: List<Point>): Boolean {
        return subGrid.all { it.type.cannotBe(Type.WALL, Type.MONSTER) }
    }
    private fun containsAtLeastOnePossibleHall(subGrid: List<Point>): Boolean {
        return subGrid.any { it.type.canBe(Type.HALLWAY) }
    }
}