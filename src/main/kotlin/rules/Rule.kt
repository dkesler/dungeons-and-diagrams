package rules

import game.Board
import game.Update
import game.findTreasureRoomStartingAt
import utils.Box
import utils.Point
import utils.TreasureRoom

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
    //each treasure?

    fun eachRowAndCol(
        board: Board,
        rowCallback: (List<Point>, Int) -> Check?,
        colCallback: (List<Point>, Int) -> Check?
    ): ApplyResult {
        val rowCheck = board.grid.rows.fold(null) { check: Check?, rowIdx ->
            if (check != null) check
            else rowCallback(board.grid.row(rowIdx), rowIdx)
        }
        if (rowCheck != null) {
            return checkToApplyResult(rowCheck, board)
        }

        val colCheck = board.grid.cols.fold(null) { check: Check?, colIdx ->
            if (check != null) check
            else colCallback(board.grid.col(colIdx), colIdx)
        }
        return checkToApplyResult(colCheck, board)
    }

    fun eachTwoByTwo(board: Board, callback: (Box) -> Check?): ApplyResult {
        val check = (0 until board.grid.maxRow).flatMap { rowIdx ->
            (0 until board.grid.maxCol).map { colIdx ->
                Box(rowIdx, colIdx, rowIdx+1, colIdx+1)
            }
        }.fold(null) { check: Check?, box ->
            if (check != null) check
            else callback(box)
        }

        return checkToApplyResult(check, board)
    }

    fun eachTreasureRoom(board: Board, callback: (TreasureRoom) -> Check?): ApplyResult {
        val check = board.treasures
            .map{ findTreasureRoomStartingAt(it.first, it.second, board.grid) }
            .map{ TreasureRoom(Box.fromPoints(it)) }
            .fold(null) { check: Check?, treasureRoom ->
                if (check != null) check
                else callback(treasureRoom)
            }

        return checkToApplyResult(check, board)
    }

    fun each(board: Board, filter: (Point) -> Boolean, callback: (Point) -> Check?): ApplyResult {
        val check = board.grid.points().fold(null) { check: Check?, point ->
            if (check != null) check
            else if (!filter(point)) null
            else callback(point)
        }
        return checkToApplyResult(check, board)
    }

    fun checkToApplyResult(check: Check?, board: Board): ApplyResult {
        if (check == null) {
            return ApplyResult(false, false, name(), "", board)
        } else {
            return ApplyResult(true, !check.update.valid, name(), check.description, check.update.board)
        }
    }

    data class Check(val update: Update, val description: String)
}

//Applicable:false means no part of the board matched the rule and we did not attempt to update
//Contradiction:false means part of the board matched the rule, but the update resulted in an invalid board state, likely due to an incorrect choice during bifurcation
data class ApplyResult(val applicable: Boolean, val contradiction: Boolean, val rule: String, val description : String, val newBoard: Board)