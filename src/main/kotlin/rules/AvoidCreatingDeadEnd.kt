package rules

import game.Board
import game.Type
import utils.Point

//For a given cell that could be hall or treasure room, if every neighbor, or every neighbor but one is a wall,
// the cell must be a wall to avoid creating a dead end
class AvoidCreatingDeadEnd : Rule {
    override fun name() = "AvoidCreatingDeadEnd"
    override fun apply(board: Board): ApplyResult {
        fun rule(point: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(point.toPair())
            val wallNeighbors = neighbors.filter { it.type.eq(Type.WALL) }
            if (wallNeighbors.count() >= neighbors.count()-1) {
                val update = board.update(point.row, point.col, point.type.types - setOf(Type.ROOM, Type.HALLWAY))
                return Rule.Check(update, "row[${point.row}].col[${point.col}}]")
            }
            return null
        }

        return each(
            board,
            { it.type.canBe(Type.ROOM, Type.HALLWAY) },
            ::rule
        )
    }
}