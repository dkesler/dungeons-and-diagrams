package utils

import game.TypeRange

data class Point(val row: Int, val col: Int, val type: TypeRange) {
    fun toPair(): Pair<Int, Int> = Pair(row, col)
}
