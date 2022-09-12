package rules

import game.Board
import game.Type
import game.TypeRange
import game.getAllTreasureRooms
import utils.Point

//If you have an island of unknown cells that could be hall but they can never reach a hallway or they can't reach each
//treasure room, they can't be hallway
class IncompleteUnknownHallCrawlCantBeHallway: Rule {
    override fun apply(board: Board): ApplyResult {

        val knownHalls = board.grid.points().filter{ it.type.eq(Type.HALLWAY) }
        val treasureRooms = getAllTreasureRooms(board.grid)

        //if there's no known halls yet and no treasure rooms we can't verify connectivity to anything
        if (knownHalls.isEmpty() && treasureRooms.isEmpty()) {
            return ApplyResult(false, false, name(), "", board)
        }

        fun doNotShareTreasureRoomNeighbor(visiting: Point, candidate: Point): Boolean {
            for (treasureRoom in treasureRooms) {
                val neighbors = board.grid.neighbors(treasureRoom.box)
                if (visiting in neighbors && candidate in neighbors) {
                    return false
                }
            }
            return true
        }

        fun findIslandOfCellsThatCouldBeHall(point: Point, board: Board): Set<Point> {
            val visited = mutableSetOf(point)
            val toVisit = mutableSetOf(point)

            while(toVisit.isNotEmpty()) {
                val visiting = toVisit.first()
                toVisit.remove(visiting)
                val neighbors = board.grid.neighbors(visiting.row, visiting.col)
                neighbors.filter { it !in visited }
                    .filter{ it.type.canBe(Type.HALLWAY) }
                    .filter{ doNotShareTreasureRoomNeighbor(visiting, it) }
                    .forEach { visited.add(it); toVisit.add(it) }
            }
            return visited

        }

        fun rule(point: Point): Rule.Check? {
            val island = findIslandOfCellsThatCouldBeHall(point, board)
            if (knownHalls.isNotEmpty() && island.count{ it.type.eq(Type.HALLWAY) } == 0) {
                return Rule.Check(
                    board.update(
                        island.map{ Point(it.row, it.col, TypeRange(it.type.types - Type.HALLWAY)) }
                    ),
                    "row[${point.row}].col[${point.col}]"
                )
            }

            for (treasureRoom in treasureRooms) {
                if (board.grid.neighbors(treasureRoom.box).intersect(island).isEmpty()) {
                    return Rule.Check(
                        board.update(
                            island.map{ Point(it.row, it.col, TypeRange(it.type.types - Type.HALLWAY)) }
                        ),
                        "row[${point.row}].col[${point.col}]"
                    )
                }
            }

            return null
        }

        return each(
            board,
            { it.type.canBe(Type.HALLWAY) && !it.type.known },
            ::rule
        )
    }

    override fun name() = "IncompleteUnknownHallCrawlCantBeHallway"
}