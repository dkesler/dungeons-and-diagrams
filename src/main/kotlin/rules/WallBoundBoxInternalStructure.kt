package rules

import Board
import Space
import neighbors
import utils.Box
import utils.WallBoundBox

class WallBoundBoxInternalStructure : Rule {
    override fun name() = "WallBoundBoxInternalStructure"
    override fun apply(board: Board): ApplyResult {
        //for each 2x2 of unknowns, determine if they are wall-bound (i.e. require exactly 1, 2, or 3 walls)
        //if wall bound, check each possible layout given required number of walls.  if a cell is the same type in
        //each layout, make it that type

        for (row in (0 until board.grid.size-1)) {
            for (col in (0 until board.grid[0].size-1)) {
                val box = Box(row, col, row+1, col+1)
                //TODO: should we handle boxes that aren't all unknown? do we gain anything from that?
                if (isAllUnknown(box, board)) {
                    val wallBoundBox = WallBoundBox.fromBox(box, board)
                    //TODO: handle walls == 1 or 3
                    if (wallBoundBox.minWalls == wallBoundBox.maxWalls && wallBoundBox.minWalls == 2) {
                        val wbb2 = checkWalls2(wallBoundBox, board)
                        if (wbb2.applicable) return wbb2
                    }
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)
    }

    //check each of the 6 layouts of 2 walls.  if, in each layout, a cell is always the same value, update it to
    //that value.  if no cell can be updated, return applicable=false
    private fun checkWalls2(wallBoundBox: WallBoundBox, board: Board): ApplyResult {
        val validLayouts = listOf(
            listOf(mutableSetOf<Space>(), mutableSetOf()),
            listOf(mutableSetOf(),mutableSetOf())
        )

        //top horizontal bar.  The top row must be able to fit both walls and the bottom two cells must
        //have at least one empty neighbor outside the box to not be a dead end
        if (
            wallBoundBox.rowReqs[0].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box)
        ) {

            validLayouts[0][0].add(Space.WALL)
            validLayouts[0][1].add(Space.WALL)
            validLayouts[1][0].add(Space.EMPTY)
            validLayouts[1][1].add(Space.EMPTY)
        }

        //bot horizontal bar.  The bottom row must be able to fit both walls and the top two cells
        //must have at least one empty neighbor outside the box to not be a dead end
        if (
            wallBoundBox.rowReqs[1].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box)
        ) {
            validLayouts[0][0].add(Space.EMPTY)
            validLayouts[0][1].add(Space.EMPTY)
            validLayouts[1][0].add(Space.WALL)
            validLayouts[1][1].add(Space.WALL)
        }

        //left vert bar.  The left col must be able to fit both walls and the right two cells
        //must have at least one empty neighbor outside the box to not be  adead end
        if (
            wallBoundBox.colReqs[0].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.maxCol, 1, board, wallBoundBox.box)
        ) {
            validLayouts[0][0].add(Space.WALL)
            validLayouts[0][1].add(Space.EMPTY)
            validLayouts[1][0].add(Space.WALL)
            validLayouts[1][1].add(Space.EMPTY)
        }

        //right vert bar.  The right col must be able to fit both walls and the left two cells
        //must have at least one empty neighbor outside the box to not be  adead end
        if (
            wallBoundBox.colReqs[1].contains(2) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 1, board, wallBoundBox.box)
        ) {
            validLayouts[0][0].add(Space.EMPTY)
            validLayouts[0][1].add(Space.WALL)
            validLayouts[1][0].add(Space.EMPTY)
            validLayouts[1][1].add(Space.WALL)
        }

        //slash.  We are guaranteed each row and col can fit at least one wall or they would've been changed to empty already
        //each empty cell must have two empty neighbors to not be a dead end
        if (
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box)
        ) {
            validLayouts[0][0].add(Space.EMPTY)
            validLayouts[0][1].add(Space.WALL)
            validLayouts[1][0].add(Space.WALL)
            validLayouts[1][1].add(Space.EMPTY)
        }

        //backslash.  We are guaranteed each row and col can fit at least one wall or they would've been changed to empty already
        //each empty cell must have two empty neighbors to not be a dead end
        if (
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.minRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box) &&
            hasAtLeastNPotentiallyEmptyNeighbors(wallBoundBox.box.maxRow, wallBoundBox.box.minCol, 2, board, wallBoundBox.box)
        ) {
            validLayouts[0][0].add(Space.WALL)
            validLayouts[0][1].add(Space.EMPTY)
            validLayouts[1][0].add(Space.EMPTY)
            validLayouts[1][1].add(Space.WALL)
        }

        val updatePossible = validLayouts.flatten().any { it.count() == 1 }
        if (!updatePossible) {
            return ApplyResult(false, false, name(), "", board)
        }

        var b = board
        for (rowOffset in (0..1)) {
            for (colOffset in (0..1)) {
                if (validLayouts[rowOffset][colOffset].count() == 1) {
                    val update = b.update(wallBoundBox.box.minRow+rowOffset, wallBoundBox.box.minCol+colOffset, validLayouts[rowOffset][colOffset].first())
                    if (!update.valid) {
                        return ApplyResult(true, true, name(), "", b)
                    }
                    b = update.board
                }
            }
        }
        return ApplyResult(true, false, name(), "${name()}.row[${wallBoundBox.box.minRow}].col[${wallBoundBox.box.minCol}]", b)

    }

    private fun hasAtLeastNPotentiallyEmptyNeighbors(row: Int, col: Int, n: Int, board: Board, box: Box): Boolean {
        return neighbors(row, col, board.grid.size, board.grid[0].size)
            .filter { !box.contains(it) }
            .map{ board.grid[it.first][it.second] }
            .count{ it != Space.WALL } >= n

    }

    private fun isAllUnknown(box: Box, board: Board): Boolean {
        return box.points().map{ board.grid[it.first][it.second] }.all { it == Space.UNKNOWN }
    }
}