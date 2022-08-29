package metrics

import game.Board

data class Solve(val board: Board, val successful: Boolean, val steps: List<Step>)
