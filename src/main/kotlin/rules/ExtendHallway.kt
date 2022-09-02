package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

class ExtendHallway : Rule {
    override fun name() = "ExtendHallway"
    override fun apply(board: Board): ApplyResult {
        fun rule(point: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(point.row, point.col)
            //if a hallway ever has neighbors.count()-1 walls, it would be a dead end.  so if it has neighbors.count()-2 walls
            //as neighbors, and it has more neighbors that might be walls but aren't known, those neighbors can't be walls
            if (neighbors.count{it.type.eq(CellType.WALL) } == neighbors.count()-2 &&
                neighbors.count{it.type.canBe(CellType.WALL)} > neighbors.count()-2) {
                val toUpdate = neighbors.filter{it.type.canBe(CellType.WALL) && !it.type.known }
                    .map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.WALL)) }
                return Rule.Check(board.update(toUpdate), "row[${point.row}]col[${point.col}]")
            }
            return null
        }

        return each(
            board,
            { it.type.eq(CellType.HALL) },
            ::rule
        )
    }
}