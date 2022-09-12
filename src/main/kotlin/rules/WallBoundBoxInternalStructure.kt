package rules

import game.Board
import game.Type
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
                    val toUpdate = wallBoundBox.checkWalls(board)
                    if (toUpdate.isNotEmpty()) {
                        return Rule.Check(board.update(toUpdate), "row[${wallBoundBox.box.minRow}].col[${wallBoundBox.box.minCol}")
                    }
                }
            }
            return null
        }
        return eachTwoByTwo(board, ::rule)
    }


    private fun isAllUnknown(box: Box, board: Board): Boolean {
        return box.points().map{ board.grid.cells[it.first][it.second] }.all { it.canBe(Type.WALL) && it.canBe(Type.ROOM, Type.HALLWAY) }
    }
}