package metrics

import Board

data class Solve(val board: Board, val successful: Boolean, val steps: List<Step>)
