package game

import java.lang.RuntimeException

enum class CellType(val c: Char) {
    HALL('.'),
    WALL('W'),
    TREASURE('T'),
    TREASURE_ROOM('O'),
    MONSTER('M')
}


