package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Point

//If a cell that could be a wall would create two or more disjoint islands which must be connected (i.e. both contain cells that must not be wall)
//if it were a wall, then that "roadblock" cell cannot be a wall
class Roadblock: Rule {
    override fun apply(board: Board): ApplyResult {
        fun findIslands(roadblock: Point): Set<Set<Point>> {
            val visitedPoints = mutableSetOf<Point>(roadblock)
            val islands = mutableSetOf<Set<Point>>()
            for (islandStart in board.grid.points()) {
                //We can't start an island on a wall because walls are never part of an island
                //We can't start an island on a monster because if we explore all paths out of the monster we may end
                //up creating an island that cannot actually exist because it requires passing through the monster
                //We will still try to create islands starting from the monster's neighbors so we will still find all
                //islands that contain that monster.
                if ( islandStart != roadblock && !islandStart.type.eq(Type.WALL) && !islandStart.type.eq(Type.MONSTER) && islandStart !in visitedPoints) {
                    val island = mutableSetOf(islandStart)
                    val toVisit = mutableSetOf(islandStart)

                    while(toVisit.isNotEmpty()) {
                        val visiting = toVisit.first()
                        toVisit.remove(visiting)
                        island.add(visiting)

                        //while the monster can be part of the island, we cannot proceed to the monster's neighbors because the monster
                        //must be in a dead end in the finished puzzle.
                        if (!visiting.type.eq(Type.MONSTER)) {
                            val neighbors = board.grid.neighbors(visiting.toPair())
                                .filter{ !it.type.eq(Type.WALL) }
                                .filter{ it != roadblock }
                                .filter{ it !in island}
                            toVisit.addAll(neighbors)
                        }
                    }

                    islands.add(island)
                    visitedPoints.addAll(island)
                }
            }

            return islands
        }

        fun rule(point: Point): Rule.Check? {
            val islands = findIslands(point)

            //if we have more than 1 island that has a cell that must be an empty space, the current point would be a roadblock and cannot be wall
            if (islands.count{ it.any{cell -> cell.type.mustBe(Type.HALLWAY, Type.ROOM, Type.TREASURE)} } > 1 ) {
                return Rule.Check(board.update(point.row, point.col, point.type.types - Type.WALL), "row[${point.row}].col[${point.col}]")
            }

            val mustSeeCells = board.grid.points().filter{ it.type.eq(Type.MONSTER) || it.type.eq(Type.TREASURE) || it.type.mustBe(Type.ROOM, Type.HALLWAY)}

            //for each monster, if it is not on at least one island that contains every cell that it must connect to, the current point is a roadblock
            for (monster in board.monsters) {
                val monsterIslands = islands.filter{ Point(monster.first, monster.second, TypeRange(setOf(Type.MONSTER))) in it }
                if (monsterIslands.none { island -> island.containsAll(mustSeeCells) }) {
                    return Rule.Check(board.update(point.row, point.col, point.type.types - Type.WALL), "row[${point.row}].col[${point.col}]")
                }
            }

            return null
        }

        return each(board, {it.type.canBe(Type.WALL) && !it.type.known}, ::rule)
    }

    override fun name() = "Roadblock"
}