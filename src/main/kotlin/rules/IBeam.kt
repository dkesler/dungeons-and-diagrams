package rules

import game.Board
import utils.Box
import utils.WallBoundBox

//Divide two adjacent rows/cols up into 2x2 blocks.  Determine the minimum number of additional walls each block
//requires to be valid and the maximum number of walls each could take and still be valid.  If the number of walls yet
//to be placed exactly equals the minimum number of walls required by each 2x2 to be valid, then each 2x2 can only get
//the minimum number of walls.  Resolve each as a WBB.  If the number of walls to be placed exactly equals the maximum
//number of walls that can be placed in each 2x2, then each 2x2 must receive the maximum number of walls.  Resolve each
//as a WBB
class IBeam : Rule {
    override fun apply(board: Board): ApplyResult {
        for (rowIdx in (0 until board.grid.maxRow)) {
            //find # walls to be placed in both rows
            val wallsToBePlaced = (board.rowReqs[rowIdx] + board.rowReqs[rowIdx+1])
            //chop into 2x2s
            val wbbs = (0 until board.grid.maxCol step 2).map{ Box(rowIdx, it, rowIdx+1, it+1) }
                .map{WallBoundBox.fromBox(it, board)}

            val minWalls = wbbs.sumOf { it.minWalls }

            if (minWalls == wallsToBePlaced) {
                val minifiedWbbs = wbbs.map { wbb -> WallBoundBox(wbb.box, wbb.rowReqs, wbb.colReqs, wbb.minWalls, wbb.minWalls) }
                val pointsToUpdate = minifiedWbbs.flatMap{ it.checkWalls(board) }
                if (pointsToUpdate.isNotEmpty()) {
                    return update(board, pointsToUpdate, "row[${rowIdx}]")
                }
            }
        }

        for (colIdx in (0 until board.grid.maxCol)) {
            //find # walls to be placed in both cols
            val wallsToBePlaced = (board.colReqs[colIdx] + board.colReqs[colIdx+1])
            //chop into 2x2s
            val wbbs = (0 until board.grid.maxRow step 2).map{ Box(it, colIdx, it+1, colIdx+1) }
                .map{WallBoundBox.fromBox(it, board)}

            val minWalls = wbbs.sumOf { it.minWalls }

            if (minWalls == wallsToBePlaced) {
                val minifiedWbbs = wbbs.map { wbb -> WallBoundBox(wbb.box, wbb.rowReqs, wbb.colReqs, wbb.minWalls, wbb.minWalls) }
                val pointsToUpdate = minifiedWbbs.flatMap{ it.checkWalls(board) }
                if (pointsToUpdate.isNotEmpty()) {
                    return update(board, pointsToUpdate, "col[${colIdx}]")
                }
            }
        }

        return ApplyResult(false, false, name(), "", board)

    }

    override fun name() = "IBeam"


}