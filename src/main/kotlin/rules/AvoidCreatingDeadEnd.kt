package rules

import game.Board
import game.CellType
import utils.Point

//For a given cell that could be hall or treasure room, if every neighbor, or every neighbor but one is a wall,
// the cell must be a wall to avoid creating a dead end
class AvoidCreatingDeadEnd : Rule {
    override fun name() = "AvoidCreatingDeadEnd"
    override fun apply(board: Board): ApplyResult {
        fun rule(point: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(point.toPair())
            val wallNeighbors = neighbors.filter { it.type.eq(CellType.WALL) }
            if (wallNeighbors.count() >= neighbors.count()-1) {
                val update = board.update(point.row, point.col, point.type.types - setOf(CellType.TREASURE_ROOM, CellType.HALL))
                return Rule.Check(update, ".row[${point.row}].col[${point.col}}]")
            }
            return null
        }

        return each(
            board,
            { it.type.canBe(CellType.TREASURE_ROOM, CellType.HALL) },
            ::rule
        )
    }
}