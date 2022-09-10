package game

enum class CellType(val c: Char) {
    HALLWAY('.'),
    WALL('W'),
    TREASURE('T'),
    ROOM('O'),
    MONSTER('M')
}


