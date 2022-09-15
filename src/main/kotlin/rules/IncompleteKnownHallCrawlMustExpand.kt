package rules

import game.Board
import game.Type
import game.getAllTreasureRooms
import utils.Point

//For each hallway, perform a hall crawl.  If the resulting island does not contain every known hallway and at least
//one neighbor of every treasure room, and the island has exactly one unknown neighbor, that unknown neighbor must be
//hallway
class IncompleteKnownHallCrawlMustExpand : Rule {
    override fun apply(board: Board): ApplyResult {
        //if we have nothing in the board we know isn't wall, this rule can't do anything so return early
        val knownHalls = board.grid.points().filter{ it.type.eq(Type.HALLWAY) }
        val visitedHalls = mutableSetOf<Point>()
        val treasureRooms = getAllTreasureRooms(board.grid)

        fun includesNeighborOfEachTreasureRoom(hallGraph: Set<Point>): Boolean {
            for (treasureRoom in treasureRooms) {
                if (board.grid.neighbors(treasureRoom.box).intersect(hallGraph).isEmpty()) {
                    return false
                }
            }
            return true
        }

        fun rule(point: Point): Rule.Check? {
            if (point in visitedHalls) {
                return null
            }
            val hallGraph = findContiguousHalls(point, board)
            val hallsNotInGraph = knownHalls.size > hallGraph.size

            if (hallsNotInGraph || !includesNeighborOfEachTreasureRoom(hallGraph) ) {
                val hallGraphNeighbors = neighbors(hallGraph, board)
                val unknownHallGraphNeighbors = hallGraphNeighbors.filter{ !it.type.known }
                if (unknownHallGraphNeighbors.size == 1) {
                    val pointToUpdate = unknownHallGraphNeighbors.first()
                    return Rule.Check(
                        board.update(pointToUpdate.row, pointToUpdate.col, setOf(Type.HALLWAY)),
                        "row[${pointToUpdate.row}].col[${pointToUpdate.col}]"
                    )
                }
            }
            return null
        }

        return each(
            board,
            {it.type.eq(Type.HALLWAY)},
            ::rule
        )
    }

    private fun findContiguousHalls(point: Point, board: Board): Set<Point> {
        val visited = mutableSetOf(point)
        val toVisit = mutableSetOf(point)

        while(toVisit.isNotEmpty()) {
            val visiting = toVisit.first()
            toVisit.remove(visiting)
            val neighbors = board.grid.neighbors(visiting.row, visiting.col)
            neighbors.filter { it !in visited }
                .filter{ it.type.eq(Type.HALLWAY) }
                .forEach { visited.add(it); toVisit.add(it) }
        }
        return visited
    }

    fun neighbors(emptyGraph: Set<Point>, board: Board): Set<Point> {
        return emptyGraph.flatMap { board.grid.neighbors(it.row, it.col) }.filter { it !in emptyGraph }.toSet()
    }

    override fun name() = "IncompleteKnownHallCrawlMustExpand"
}