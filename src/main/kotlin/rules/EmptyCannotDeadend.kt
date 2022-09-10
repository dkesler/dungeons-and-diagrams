package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point

//if an empty space ever has neighbors.count()-1 walls, it would be a dead end.  so if it has neighbors.count()-2 walls
//as neighbors, and it has more neighbors that might be walls but aren't known, those neighbors can't be walls
class EmptyCannotDeadend : Rule {
    override fun name() = "EmptyCannotDeadend"
    override fun apply(board: Board): ApplyResult {
        fun rule(point: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(point.row, point.col)
            if (neighbors.count{it.type.eq(Type.WALL) } == neighbors.count()-2 &&
                neighbors.count{it.type.canBe(Type.WALL)} > neighbors.count()-2) {
                val toUpdate = neighbors.filter{it.type.canBe(Type.WALL) && !it.type.known }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - Type.WALL)) }
                return Rule.Check(board.update(toUpdate), "row[${point.row}]col[${point.col}]")
            }
            return null
        }

        return each(
            board,
            { it.type.canBe(Type.HALLWAY, Type.ROOM) && !it.type.canBe(Type.WALL) },
            ::rule
        )
    }
}