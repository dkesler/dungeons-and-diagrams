package rules

import game.Board
import game.CellType
import game.TypeRange
import utils.Box
import utils.Point
import utils.WallBoundBox

class WallBoundBoxInternalStructure : Rule {
    override fun name() = "WallBoundBoxInternalStructure"
    //for each 2x2 of unknowns, determine if they are wall-bound (i.e. require exactly 1, 2, or 3 walls)
    //if wall bound, check each possible layout given required number of walls.  if a cell is the same type in
    //each layout, make it that type
    override fun apply(board: Board): ApplyResult {
        fun rule(box: Box): Rule.Check? {
            if (isAllUnknown(box, board)) {
                val wallBoundBox = WallBoundBox.fromBox(box, board)
                //TODO: handle walls == 1 or 3?  can this actually ever give us anything?
                if (wallBoundBox.minWalls == wallBoundBox.maxWalls && wallBoundBox.minWalls == 2) {
                    val toUpdate = checkWalls2(wallBoundBox, board)
                    if (toUpdate.isNotEmpty()) {
                        return Rule.Check(board.update(toUpdate), "row[${wallBoundBox.box.minRow}].col[${wallBoundBox.box.minCol}")
                    }
                }
            }
            return null
        }
        return eachTwoByTwo(board, ::rule)
    }

    //check each of the 6 layouts of 2 walls.  if, in each layout, a cell is always the same value, return that point
    private fun checkWalls2(wallBoundBox: WallBoundBox, board: Board): Set<Point> {
        val couldBeWall = mutableSetOf<Pair<Int, Int>>()
        val couldBeNonWall = mutableSetOf<Pair<Int, Int>>()

        //top horizontal bar.  The top row must be able to fit both walls and the bottom two cells must
        //have at least one empty neighbor outside the box to not be a dead end
        if (
            wallBoundBox.rowReqs[0].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box)
        ) {
            couldBeWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //bot horizontal bar.  The bottom row must be able to fit both walls and the top two cells
        //must have at least one empty neighbor outside the box to not be a dead end
        if (
            wallBoundBox.rowReqs[1].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box)
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeWall.add(Pair(1, 1))
        }

        //left vert bar.  The left col must be able to fit both walls and the right two cells
        //must have at least one empty neighbor outside the box to not be  adead end
        if (
            wallBoundBox.colReqs[0].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box)
        ) {
            couldBeWall.add(Pair(0, 0))
            couldBeNonWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //right vert bar.  The right col must be able to fit both walls and the left two cells
        //must have at least one empty neighbor outside the box to not be  adead end
        if (
            wallBoundBox.colReqs[1].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box)
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeNonWall.add(Pair(1, 0))
            couldBeWall.add(Pair(1, 1))
        }

        //slash.  We are guaranteed each row and col can fit at least one wall or they would've been changed to empty already
        //each empty cell must have two empty neighbors to not be a dead end
        if (
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box)
        ) {
            couldBeNonWall.add(Pair(0, 0))
            couldBeWall.add(Pair(0, 1))
            couldBeWall.add(Pair(1, 0))
            couldBeNonWall.add(Pair(1, 1))
        }

        //backslash.  We are guaranteed each row and col can fit at least one wall or they would've been changed to empty already
        //each empty cell must have two empty neighbors to not be a dead end
        if (
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box)
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

        val toUpdate = mutableSetOf<Point>()
        for (rowOffset in (0..1)) {
            for (colOffset in (0..1)) {
                if (couldBeWall.contains(Pair(rowOffset, colOffset)) && !couldBeNonWall.contains(Pair(rowOffset, colOffset))) {
                    val rowIdx = wallBoundBox.box.minRow + rowOffset
                    val colIdx = wallBoundBox.box.minCol + colOffset
                    toUpdate.add(Point(rowIdx, colIdx, TypeRange(setOf(CellType.WALL))))
                } else if (!couldBeWall.contains(Pair(rowOffset, colOffset)) && couldBeNonWall.contains(Pair(rowOffset, colOffset))) {
                    val rowIdx = wallBoundBox.box.minRow + rowOffset
                    val colIdx = wallBoundBox.box.minCol + colOffset
                    toUpdate.add(Point(rowIdx, colIdx, TypeRange(board.grid.cells[rowIdx][colIdx].types - CellType.WALL)))
                }

            }
        }
        return toUpdate.toSet()
    }

    private fun hasAtLeastNPotentiallyEmptyNeighbors(row: Int, col: Int, n: Int, board: Board, box: Box): Boolean {
        return board.grid.neighbors(row, col)
            .filter { !box.contains(it.toPair()) }
            .map{ it.type }
            .count{ !it.eq(CellType.WALL) } >= n

    }

    private fun isAllUnknown(box: Box, board: Board): Boolean {
        return box.points().map{ board.grid.cells[it.first][it.second] }.all { it.canBe(CellType.WALL) && it.canBe(CellType.TREASURE_ROOM, CellType.HALL) }
    }
}