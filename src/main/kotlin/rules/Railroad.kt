package rules

import game.Board
import game.Type
import game.TypeRange
import utils.Box
import utils.Claim
import utils.Point
import kotlin.math.ceil
import kotlin.math.max

//Since a monster can only have one exit, any row/col containing a monster must use at least one wall (if not two)
//on the cells surrounding the monster.  If all walls in a row/col are claimed by a monster, any cell not in that claim
//must be empty.
class Railroad : Rule {
    override fun apply(board: Board): ApplyResult {
        return eachStripe(
            board,
            ::rowRule
        )
    }

    private fun rowRule(row: List<Point>, rowIdx: Int, board: Board): Rule.Check? {
        val claims = board.monsters.filter{it.first == rowIdx}
            .map{ board.grid.horizontalNeighbors(it.first, it.second).map{Pair(it.row, it.col)} + it }
            .map {Claim(Box.fromPoints(it), it.size-2)}
        val unsatisfiedClaims = claims.filter { claim ->
            val points = board.grid.subgrid(claim.box).flatten()
            points.count { it.type.eq(Type.WALL) } < claim.minWalls
        }
        val mergedUnsatisfiedClaims = mergeClaims(unsatisfiedClaims)
        val wallsRemaining = board.rowReqs[rowIdx] - row.count{it.type.eq(Type.WALL)}

        //if the number of walls left to place equals the minimum number of walls needed to satisfy all unsatisfied
        //claims, then each claim must get the minimum number required and no walls can be used outside of an
        //unsatisfied claim
        if (mergedUnsatisfiedClaims.sumOf { it.minWalls } == wallsRemaining) {
            val toUpdate = mutableSetOf<Point>()
            //any cell not in an unsat claim must be empty
            for (cell in row) {
                if (!cell.type.known && cell.type.canBe(Type.WALL) && unsatisfiedClaims.none{ it.box.contains(cell.row, cell.col) }) {
                    toUpdate.add(Point(cell.row, cell.col, TypeRange(cell.type.types - Type.WALL)))
                }
            }

            //any unsatisfied claim must have wall crossbar
            for (claim in unsatisfiedClaims) {
                val middleCol = (claim.box.maxCol + claim.box.minCol)/2
                val crossbar = board.grid.verticalNeighbors(rowIdx, middleCol)
                crossbar.filter{ !it.type.known}
                    .map{ Point(it.row, it.col, TypeRange(setOf(Type.WALL))) }
                    .forEach(toUpdate::add)
            }

            //any merged claim of exactly 2 monsters must have wall in the middle
            for (claim in mergedUnsatisfiedClaims) {
                if (claim.box.width() == 5) {
                    val middleCol = (claim.box.maxCol + claim.box.minCol)/2
                    toUpdate.add( Point(rowIdx, middleCol, TypeRange(setOf(Type.WALL))))
                }
            }

            if (toUpdate.isNotEmpty()) {
                return Rule.Check(board.update(toUpdate), "row[$rowIdx]")
            }
        }
        return null
    }

    private fun mergeClaims(claims: List<Claim>): List<Claim> {
        //this is inefficient, but we'll only ever have 4 claims max
        for (i in claims.indices) {
            for (j in (i+1 until claims.size)) {
                if (claims[i].box.overlaps(claims[j].box)) {
                    val mergedBox = claims[i].box.merge(claims[j].box)
                    val mergedClaim = Claim(
                        mergedBox,
                        ceil(((max(mergedBox.width(), mergedBox.height())-1)/2) / 2.0).toInt()
                    )
                    return mergeClaims(
                        claims.subList(0, i) +
                                mergedClaim +
                                claims.subList(j+1, claims.size)

                    )
                }
            }
        }
        return claims
    }

    override fun name() = "Railroad"
}