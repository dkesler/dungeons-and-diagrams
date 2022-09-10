package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

//if an empty space ever has neighbors-1 non-empty neighbors, its last neighbor must be empty
//technically this only applies for puzzles with more than one empty square
class EmptyCannotBeIsolated : Rule {
    override fun name() = "EmptyCannotBeIsolated"
    override fun apply(board: Board): ApplyResult {
        fun rule(point: Point): Rule.Check? {
            val neighbors = board.grid.neighbors(point.row, point.col)
            val numNonemptyNeighbors = neighbors.count { !it.type.canBe(CellType.HALLWAY, CellType.ROOM, CellType.TREASURE) }
            val unknownNeighborsThatCanBeWall = neighbors.filter { it.type.canBe(CellType.WALL) && !it.type.known }
            if (numNonemptyNeighbors == neighbors.count()-1 && unknownNeighborsThatCanBeWall.isNotEmpty()) {
                val toUpdate = unknownNeighborsThatCanBeWall.map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.WALL)) }
                return Rule.Check(board.update(toUpdate), "row[${point.row}]col[${point.col}]")
            }
            return null
        }

        return each(
            board,
            { it.type.canBe(CellType.HALLWAY, CellType.ROOM) && !it.type.canBe(CellType.WALL) },
            ::rule
        )
    }
}