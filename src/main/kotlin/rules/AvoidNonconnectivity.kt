package rules

import game.Board
import game.Type
import utils.Point

//For each empty cell, find the contiguous empty blocks attached to it.
//If a contiguous empty block graph does not contain every known non-wall cell
// and it has exactly one unknown neighbor, that unknown neighbor must be empty
class AvoidNonconnectivity : Rule{
    override fun apply(board: Board): ApplyResult {
        //if we have nothing in the board we know isn't wall, this rule can't do anything so return early
        val knownEmpties = board.grid.points().filter{ !it.type.eq(Type.WALL) }
        val visitedEmpties = mutableSetOf<Point>()

        fun rule(point: Point): Rule.Check? {
            if (point in visitedEmpties) {
                return null
            }
            val emptyGraph = findContiguousEmpties(point, board)
            if (knownEmpties.size > emptyGraph.size) {
                val emptyGraphNeighbors = neighbors(emptyGraph, board)
                val unknownEmptyGraphNeighbors = emptyGraphNeighbors.filter{ !it.type.known }
                if (unknownEmptyGraphNeighbors.size == 1) {
                    val pointToUpdate = unknownEmptyGraphNeighbors.first()
                    return Rule.Check(
                        board.update(pointToUpdate.row, pointToUpdate.col, pointToUpdate.type.types - Type.WALL),
                        "row[${pointToUpdate.row}].col[${pointToUpdate.col}]"
                    )
                }
            }
            return null
        }

        return each(
            board,
            {it.type.mustBe(Type.HALLWAY, Type.ROOM)},
            ::rule
        )


    }



    private fun findContiguousEmpties(point: Point, board: Board): Set<Point> {
        val visited = mutableSetOf(point)
        val toVisit = mutableSetOf(point)

        while(toVisit.isNotEmpty()) {
            val visiting = toVisit.first()
            toVisit.remove(visiting)
            val neighbors = board.grid.neighbors(visiting.row, visiting.col)
            neighbors.filter { it !in visited }
                .filter{ it.type.mustBe(Type.ROOM, Type.TREASURE, Type.HALLWAY, Type.MONSTER) }
                .forEach { visited.add(it); toVisit.add(it) }
        }
        return visited
    }

    //This will technically return neighbors of monsters despite neighbors being unable to propagate connectivity
    //however any monster reachable via empties will have any other neighbors known to be wall so they'll get filtered
    //out in the next step
    fun neighbors(emptyGraph: Set<Point>, board: Board): Set<Point> {
        return emptyGraph.flatMap { board.grid.neighbors(it.row, it.col) }.filter { it !in emptyGraph }.toSet()
    }

    override fun name() = "AvoidNonconnectivity"
}