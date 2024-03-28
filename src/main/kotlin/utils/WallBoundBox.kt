package utils

import game.Board
import game.Type
import game.TypeRange
import kotlin.math.max
import kotlin.math.min

data class WallBoundBox(val box: Box, val rowReqs: List<IntRange>, val colReqs: List<IntRange>, val minWalls: Int, val maxWalls: Int) {

    fun checkWalls(board: Board): Set<Point> {
        //our check functions in here don't properly handle if one of the cells is a monster or treasure so bail early
        //We may want to add functionality later to consider WBBs with monsters and/or treasures
        if ( box.points().map{ board.grid.cells[it.first][it.second]}.any{ it.eq(Type.MONSTER) || it.eq(Type.TREASURE)} ) {
            return setOf()
        }
        if (minWalls == maxWalls && minWalls == 2) {
            return checkWalls2(board)
        } else if (minWalls == maxWalls && minWalls == 1) {
            return checkWalls1(board)
        }

        return setOf()
    }

    private fun checkWalls1(board: Board): Set<Point> {
        val couldBeWall = mutableSetOf<Pair<Int, Int>>()
        val couldBeNonWall = mutableSetOf<Pair<Int, Int>>()

        //top left
        if (rowReqs[0].contains(1) &&
            colReqs[0].contains(1) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.maxCol, 1, board, box ) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.minCol, 1, board, box )
        ) {
            couldBeWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //top right
        if (rowReqs[0].contains(1) &&
            colReqs[1].contains(1) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.minCol, 1, board, box ) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.maxCol, 1, board, box )
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //bot left
        if (rowReqs[1].contains(1) &&
            colReqs[0].contains(1) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.minCol, 1, board, box ) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.maxCol, 1, board, box )
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //bot right
        if (rowReqs[1].contains(1) &&
            colReqs[1].contains(1) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.maxCol, 1, board, box ) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.minCol, 1, board, box )
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeWall.add(Pair(1, 1))
        }

        val updatePossible = couldBeWall != couldBeNonWall
        if (!updatePossible) {
            return setOf()
        }

        return toPointsToUpdate(couldBeWall, couldBeNonWall, board)
    }

    //check each of the 6 layouts of 2 walls.  if, in each layout, a cell is always the same value, return that point
    private fun checkWalls2(board: Board): Set<Point> {
        val couldBeWall = mutableSetOf<Pair<Int, Int>>()
        val couldBeNonWall = mutableSetOf<Pair<Int, Int>>()

        //top horizontal bar.  The top row must be able to fit both walls and the bottom two cells must
        //have at least one empty neighbor outside the box to not be a dead end
        if (
            rowReqs[0].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.minCol, 1, board, box) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.maxCol, 1, board, box)
        ) {
            couldBeWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //bot horizontal bar.  The bottom row must be able to fit both walls and the top two cells
        //must have at least one empty neighbor outside the box to not be a dead end
        if (
            rowReqs[1].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.minCol, 1, board, box) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.maxCol, 1, board, box)
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeWall.add(Pair(1, 1))
        }

        //left vert bar.  The left col must be able to fit both walls and the right two cells
        //must have at least one empty neighbor outside the box to not be  adead end
        if (
            colReqs[0].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.maxCol, 1, board, box) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.maxCol, 1, board, box)
        ) {
            couldBeWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //right vert bar.  The right col must be able to fit both walls and the left two cells
        //must have at least one empty neighbor outside the box to not be  adead end
        if (
            colReqs[1].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.minCol, 1, board, box) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.minCol, 1, board, box)
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeWall.add(Pair(1, 1))
        }

        //slash.  We are guaranteed each row and col can fit at least one wall or they would've been changed to empty already
        //each empty cell must have two empty neighbors to not be a dead end
        if (
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.minCol, 2, board, box) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.minCol, 2, board, box)
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //backslash.  We are guaranteed each row and col can fit at least one wall or they would've been changed to empty already
        //each empty cell must have two empty neighbors to not be a dead end
        if (
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.minRow, box.minCol, 2, board, box) &&
            hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(box.maxRow, box.minCol, 2, board, box)
        ) {
            couldBeWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeWall.add(Pair(1, 1))
        }

        val updatePossible = couldBeWall != couldBeNonWall
        if (!updatePossible) {
            return setOf()
        }

        return toPointsToUpdate(couldBeWall, couldBeNonWall, board)
    }

    private fun toPointsToUpdate(
        couldBeWall: MutableSet<Pair<Int, Int>>,
        couldBeNonWall: MutableSet<Pair<Int, Int>>,
        board: Board
    ): Set<Point> {
        val toUpdate = mutableSetOf<Point>()
        for (rowOffset in (0..1)) {
            for (colOffset in (0..1)) {
                if (couldBeWall.contains(Pair(rowOffset, colOffset)) && !couldBeNonWall.contains(
                        Pair(
                            rowOffset,
                            colOffset
                        )
                    )
                ) {
                    val rowIdx = box.minRow + rowOffset
                    val colIdx = box.minCol + colOffset
                    toUpdate.add(Point(rowIdx, colIdx, TypeRange(setOf(Type.WALL))))
                } else if (!couldBeWall.contains(Pair(rowOffset, colOffset)) && couldBeNonWall.contains(
                        Pair(
                            rowOffset,
                            colOffset
                        )
                    )
                ) {
                    val rowIdx = box.minRow + rowOffset
                    val colIdx = box.minCol + colOffset
                    toUpdate.add(Point(rowIdx, colIdx, TypeRange(board.grid.cells[rowIdx][colIdx].types - Type.WALL)))
                }

            }
        }
        return toUpdate.filter{ board.grid.cells[it.row][it.col] != it.type }.toSet()
    }

    private fun hasAtLeastNPotentiallyEmptyNeighborsOutsideBox(row: Int, col: Int, n: Int, board: Board, box: Box): Boolean {
        return board.grid.neighbors(row, col)
            .filter { !box.contains(it.toPair()) }
            .map{ it.type }
            .count{ !it.eq(Type.WALL) } >= n

    }

    companion object {
        fun fromBox(box: Box, board: Board): WallBoundBox {
            val rowReqs = mutableListOf<IntRange>()
            for (rowIdx in (box.minRow..box.maxRow)) {
                val row = board.grid.row(rowIdx)
                //if we cram as many walls elsewhere in the row, how many walls do we still need to put in the box
                //if we already have more walls placed than required, the min must be the number of walls placed
                val wbbRowMin = max(
                    board.rowReqs[rowIdx] - row.filterIndexed{ colIdx, space -> !box.contains(rowIdx, colIdx) && space.type.canBe(Type.WALL)}.count(),
                    row.filterIndexed { colIdx, space -> box.contains(rowIdx, colIdx) && space.type.eq(Type.WALL)}.count()
                )
                //how many walls can we cram in the box.  it will be limited by the number of available slots in the row within the box
                //and the number of required walls for the row minus the number of placed walls
                val wbbRowMax = min(
                    row.filterIndexed{ colIdx, space -> box.contains(rowIdx, colIdx) && space.type.canBe(Type.WALL)}.count(),
                    board.rowReqs[rowIdx] - row.filterIndexed{ colIdx, space -> !box.contains(rowIdx, colIdx) && space.type.eq(Type.WALL)}.count()
                )
                rowReqs.add((wbbRowMin..wbbRowMax))
            }

            val colReqs = mutableListOf<IntRange>()
            for (colIdx in (box.minCol..box.maxCol)) {
                val col = board.grid.col(colIdx)
                //if we cram as many walls elsewhere in the col, how many walls do we still need to put in the box
                //if we already have more walls placed than required, the min must be the number of walls placed
                val wbbColMin = max(
                    board.colReqs[colIdx] - col.filterIndexed{ rowIdx, space -> !box.contains(rowIdx, colIdx) && space.type.canBe(Type.WALL)}.count(),
                    col.filterIndexed { rowIdx, space -> box.contains(rowIdx, colIdx) && space.type.eq(Type.WALL)}.count()
                )
                //how many walls can we cram in the box.  it will be limited by the number of available slots in the col and the number of required walls for the col minus the number of placed walls
                val wbbColMax = min(
                    col.filterIndexed{ rowIdx, space -> box.contains(rowIdx, colIdx) && space.type.canBe(Type.WALL)}.count(),
                    board.colReqs[colIdx] - col.filterIndexed{ rowIdx, space -> !box.contains(rowIdx, colIdx) && space.type.eq(Type.WALL)}.count()
                )
                colReqs.add((wbbColMin..wbbColMax))
            }

            //TODO: fix run logic
            val minWalls = run {
                val minByRow = rowReqs.sumOf { it.first }
                val minByCol = colReqs.sumOf { it.first }
                val minByRowAndCol = max(minByRow, minByCol)
                val boxPoints = board.grid.subgrid(box).flatten()
                //set actual minWalls to 1 if it would otherwise be 0 and the block can't be all room
                //because otherwise the block would be an illegal 2x2 of HALL
                if (minByRowAndCol == 0  && !boxPoints.all{ it.type.canBe(Type.ROOM, Type.TREASURE) }) {
                    1
                } else {
                    minByRowAndCol
                }

            }

            val maxWalls = run {
                val maxByRow = rowReqs.sumOf { it.last }
                val maxByCol = colReqs.sumOf { it.last }
                min(maxByRow, maxByCol)
            }

            return WallBoundBox(box, rowReqs, colReqs, minWalls, maxWalls)
        }
    }
}
