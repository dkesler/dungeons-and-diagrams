package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Box
import utils.Point

/*
If placing the last wall or second to last wall in a row/col would form a cul-de-sac, that wall placement is invalid
A cul-de-sac is a 1x2 empty box which does not have an empty neighbor in at least two different directions.
Imagine the following
##
.?
.?
##

R1,2C1 cannot both be hallway or you would have a 2x2 of hallway.  however if either R1C1 or R2C1 is wall, then R1C0
and R2C0 would be a dead end respectively.  Even if we had an additional wall to place in C0, if we use it in the
cul-de-sac we'd then just end up with a dead end.
 */

class LastTwoWallsCantCreateCulDeSac: Rule {
    override fun apply(board: Board): ApplyResult {
        fun wouldBeCulDeSac(box: Box): Boolean {
            //both cells must be empty to be a cul de sac.  we ignore WALL here since even if they could be wall
            //our no-cul-de-sac logic is predicated on using up all the walls without modifying this potential cul de sac
            if (board.grid.subgrid(box).flatten().any{ !it.type.canBe(Type.ROOM, Type.HALLWAY) } ) {
                return false
            }

            val boxNeighbors = listOf(
                board.grid.leftNeighbors(box),
                board.grid.rightNeighbors(box),
                board.grid.upNeighbors(box),
                board.grid.downNeighbors(box)
            )
            return boxNeighbors.count { neighbors ->
                neighbors.any { !it.type.eq(Type.WALL) }
            } < 3
            //count is < 3 because we're testing what would happen if one of the neighbors were to turn to wall.  We
            //need at least 2 non-wall neighbors after that neighbor turns to wall
        }

        fun rowRule(row: List<Point>, rowIdx: Int): Rule.Check? {
            if (board.rowReqs[rowIdx] - row.count{it.type.eq(Type.WALL)} <= 2) {
                val toUpdate = mutableSetOf<Point>()
                for (colIdx in board.grid.cols) {
                    val t = board.grid.cells[rowIdx][colIdx]
                    if (!t.known && t.canBe(Type.WALL)) {
                        if (colIdx >= 2) {
                            if (wouldBeCulDeSac(Box(rowIdx, colIdx-2, rowIdx, colIdx-1))) {
                                toUpdate.add(Point(rowIdx, colIdx, TypeRange(t.types - Type.WALL)))
                            }
                        }
                        if (colIdx <= board.grid.maxCol - 2) {
                            if (wouldBeCulDeSac(Box(rowIdx, colIdx+1, rowIdx, colIdx+2))) {
                                toUpdate.add(Point(rowIdx, colIdx, TypeRange(t.types - Type.WALL)))
                            }
                        }
                    }
                }
                if (toUpdate.isNotEmpty()) {
                    return Rule.Check(board.update(toUpdate), "row[$rowIdx]")
                }
            }
            return null
        }

        fun colRule(col: List<Point>, colIdx: Int): Rule.Check? {
            if (board.colReqs[colIdx] - col.count{it.type.eq(Type.WALL)} <= 2) {
                val toUpdate = mutableSetOf<Point>()
                for (rowIdx in board.grid.rows) {
                    val t = board.grid.cells[rowIdx][colIdx]
                    if (!t.known && t.canBe(Type.WALL)) {
                        if (rowIdx >= 2) {
                            if (wouldBeCulDeSac(Box(rowIdx - 2, colIdx, rowIdx - 1, colIdx))) {
                                toUpdate.add(Point(rowIdx, colIdx, TypeRange(t.types - Type.WALL)))
                            }
                        }
                        if (rowIdx <= board.grid.maxRow - 2) {
                            if (wouldBeCulDeSac(Box(rowIdx + 1, colIdx, rowIdx + 2, colIdx))) {
                                toUpdate.add(Point(rowIdx, colIdx, TypeRange(t.types - Type.WALL)))
                            }
                        }
                    }
                }
                if (toUpdate.isNotEmpty()) {
                    return Rule.Check(board.update(toUpdate), "col[$colIdx]")
                }
            }

            return null
        }
        return eachRowAndCol(
            board,
            ::rowRule,
            ::colRule
        )
    }

    override fun name() = "LastTwoWallsCantCreateCulDeSac"
}