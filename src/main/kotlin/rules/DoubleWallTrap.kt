package rules

import game.Board
import game.Type
import game.getAllTreasureRooms
import utils.Point

class DoubleWallTrap: Rule {
    //TODO: This rule's code is very duplicative with WallTrap.  Should we just have this be a special case of WallTrap
    //or keep them separate but find a way to share code?
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
                            //we obviously can't trace through walls.  We can't trace through monsters because they are
                            //by definition in dead ends.  We can't trace through treasure or rooms because they only
                            //have one exit
                            !n.type.eq(Type.WALL) && !n.type.eq(Type.MONSTER) && !n.type.eq(Type.TREASURE) && !n.type.eq(Type.ROOM)
                        }.filter{ n ->
                            n == point || (n.row != point.row && n.col != point.col) || n.type.cannotBe(Type.WALL)
                        }.filter{ n -> n !in visited }
                    )

                }
                return false
            }

            for (start in board.grid.points()) {
                if (start.type.mustBe( Type.HALLWAY, Type.MONSTER)) {
                    //We must be able to find a path from the start to the point we are investigating.  If we cant trace
                    //a path from a hallway or monster to the point we are investigating, that start is cut off by the
                    //walls that would be added by making that point a gap
                    if (!canReach(start)) {
                        return true
                    }
                }
            }

            //Treasure rooms must also be able to trace a path to the point we are investigating.  However when tracing
            //from treasure rooms, actually trace from one of the treasure room's neighbors.  This is because we
            //don't trace through a treasure room in canReach.  Technically we should be able to trace _out_ of a
            //treasure room if that's where we started, but we can simulate that by just starting the trace from
            //treasure rooms neighbors.  (We're essentially fastfowarding the trace in canReach to the point where
            //all the treasure room's neighbors have been found)
            for (room in getAllTreasureRooms(board.grid)) {
                if (board.grid.neighbors(room.box).filter{!it.type.eq(Type.WALL)}.none { start -> canReach(start) }) {
                    return true
                }
            }

            return false
        }

        //if the row has only one hallway cell left to place then the column it is placed
        //in may be restricted.  When placing the last gap, the rest of the row becomes walled in.  If this is also
        // the last gap to be placed in that column the rest of the column becomes walled in.  If this traps any
        //points of interest (i.e. monsters, treasure, or known empty), then that cell cannot be a gap after all

        //The rule does not apply if we can place more than one gap in the row
        val gapsRemaining = row.count{ it.type.canBe(Type.WALL)} - board.rowReqs[index]
        if (gapsRemaining != 1) {
            return null
        }

        for (point in row) {
            val colGapsRemaining = board.grid.col(point.col).count{ it.type.canBe(Type.WALL)} - board.colReqs[point.col]
            //This rule does not apply if we can place more than one gap in the column
            if (colGapsRemaining == 1) {
                if (!point.type.known && point.type.canBe(Type.WALL)) {
                    if (trapsPointsOfInterest(point)) {
                        return Rule.Check( board.update(point.row, point.col, setOf(Type.WALL)), "row[${point.row}].col[${point.col}]")
                    }
                }
            }
        }

        return null
    }

    override fun apply(board: Board): ApplyResult {
        return eachStripe(board, ::rule)
    }

    override fun name() = "DoubleWallTrap"
}