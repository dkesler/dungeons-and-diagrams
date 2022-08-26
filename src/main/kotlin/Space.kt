import java.lang.RuntimeException

enum class Space(val c: Char) {
    UNKNOWN('?'),
    EMPTY('o'), //Either hall or treasure room
    HALL('.'),
    WALL('W'),
    TREASURE('T'),
    TREASURE_ROOM('O'),
    MONSTER('M')
}

fun fromChar(c: Char): Space {
    return when (c) {
        '?' -> Space.UNKNOWN
        'o' -> Space.EMPTY
        '.' -> Space.HALL
        'W' -> Space.WALL
        'T' -> Space.TREASURE
        'M' -> Space.MONSTER
        'O' -> Space.TREASURE_ROOM

        else -> throw RuntimeException("Unknown char [${c}] in Space.fromChar")
    }
}


