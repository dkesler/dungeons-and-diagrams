package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Point

//If you have an island of unknown cells that can't reach a hall (without going through a treasure room), that island
//cannot be hall
class UnknownIslandThatCantReachHallCantBeHall: Rule {
    override fun apply(board: Board): ApplyResult {
        val knownHalls = board.grid.points().filter{ it.type.eq(CellType.HALLWAY) }
        //if there's no known halls yet, we can't verify connectivity to those halls
        if (knownHalls.isEmpty()) {
            return ApplyResult(false, false, name(), "", board)
        }

        fun rule(point: Point): Rule.Check? {
            val island = findIslandOfCellsThatCouldBeHall(point, board)
            if (island.count{ it.type.eq(CellType.HALLWAY) } > 0) return null

            return Rule.Check(
                board.update(
                    island.map{ Point(it.row, it.col, TypeRange(it.type.types - CellType.HALLWAY)) }
                ),
                "row[${point.row}].col[${point.col}]"
            )
        }

        return each(
            board,
            { it.type.canBe(CellType.HALLWAY) && !it.type.known },
            ::rule
        )
    }

    private fun findIslandOfCellsThatCouldBeHall(point: Point, board: Board): Set<Point> {
        val visited = mutableSetOf(point)
        val toVisit = mutableSetOf(point)

        while(toVisit.isNotEmpty()) {
            val visiting = toVisit.first()
            toVisit.remove(visiting)
            val neighbors = board.grid.neighbors(visiting.row, visiting.col)
            neighbors.filter { it !in visited }
                .filter{ it.type.canBe(CellType.HALLWAY) }
                .forEach { visited.add(it); toVisit.add(it) }
        }
        return visited

    }

    override fun name() = "UnknownIslandThatCantReachHallCantBeHall"


}