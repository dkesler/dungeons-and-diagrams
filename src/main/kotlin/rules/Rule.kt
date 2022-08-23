package rules

import Board

interface Rule {
    fun apply(board: Board): ApplyResult
}

data class ApplyResult(val applicable: Boolean, val rule: String, val description : String, val newBoard: Board)