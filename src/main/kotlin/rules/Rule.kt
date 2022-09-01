package rules

import game.Board
import utils.Point

interface Rule {
    fun apply(board: Board): ApplyResult
    fun name(): String

    fun update(board: Board, toUpdate: Collection<Point>, description: String): ApplyResult {
        val update =  board.update(toUpdate)
        if (!update.valid) {
            return ApplyResult(true, true, name(), description, update.board)
        }
        return ApplyResult(true, false, name(), description, update.board)
    }


    //each monster
    //each treasure
    //each row/col
    //each 2x2

    fun each(board: Board, filter: (Point) -> Boolean, callback: (Point) -> ApplyResult): ApplyResult {
        board.grid.points().forEach { point ->
            if (filter(point)) {
                val r = callback(point)
                if (r.applicable) return r
            }
        }
        return ApplyResult(false, false, name(), "", board)
    }
}

//Applicable:false means no part of the board matched the rule and we did not attempt to update
//Contradiction:false means part of the board matched the rule, but the update resulted in an invalid board state, likely due to an incorrect choice during bifurcation
data class ApplyResult(val applicable: Boolean, val contradiction: Boolean, val rule: String, val description : String, val newBoard: Board)