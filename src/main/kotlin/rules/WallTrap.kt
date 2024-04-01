package rules

import game.Board
import game.Type
import utils.Point

class WallTrap: Rule {
    fun rule(row: List<Point>, index: Int, board: Board): Rule.Check? {

        fun trapsPointsOfInterest(point: Point): Boolean {
            fun canReach(start: Point): Boolean {
                val toVisit = mutableSetOf(start)
                val visited = mutableSetOf(start)
                while(toVisit.isNotEmpty()) {
                    val visiting = toVisit.first()
                    toVisit.remove(visiting)
                    visited.add(visiting)

                    if (visiting == point) return true

                    toVisit.addAll(
                        board.grid.neighbors(visiting.row, visiting.col).filter{n ->
                            !n.type.eq(Type.WALL) && !n.type.eq(Type.MONSTER)
                        }.filter{ n ->
                            n == point || n.row != point.row || n.type.cannotBe(Type.WALL)
                        }.filter{ n -> n !in visited }
                    )

                }
                return false
            }

            for (start in board.grid.points()) {
                if (start.type.mustBe(Type.TREASURE, Type.ROOM, Type.HALLWAY, Type.MONSTER)) {
                    //We must be able to find a path from the start to the point we are investigating.  If all of the points of interest can reach that point
                    //then making that point a gap doesn't trap any points of interest
                    if (!canReach(start)) {
                        return true
                    }
                }
            }
            return false
        }

        //if the row is not the first or last and it has only one hallway cell left to place, then the column it is placed
        //in may be restricted.  When placing the last gap, the rest of the row becomes walled in.  If this traps any
        //points of interest (i.e. monsters, treasure, or known empty), then that cell cannot be a gap after all


        //The first and last rows are not relevant because they can't wall anything off
        if (index == 0 || index == board.rowReqs.size-1) return null

        //The rule does not apply if we can place more than one gap in the wall
        val gapsRemaining = row.count{ it.type.canBe(Type.WALL)} - board.rowReqs[index]
        if (gapsRemaining != 1) {
            return null
        }

        for (point in row) {
            if (!point.type.known && point.type.canBe(Type.WALL)) {
                if (trapsPointsOfInterest(point)) {
                    return Rule.Check( board.update(point.row, point.col, setOf(Type.WALL)), "row[${point.row}].col[${point.col}]")
                }
            }
        }

        return null
    }

    override fun apply(board: Board): ApplyResult {
        return eachStripe(board, ::rule)
    }

    override fun name() = "WallTrap"
}