package game

class TypeRange(val types: Set<CellType>) {
    //TODO:  add validation regarding type mixes (e.g. cannot have a range of monster+wall)
    val known = types.count() == 1

    fun eq(t: CellType): Boolean {
        return types.count() == 1 && types.contains(t)
    }

    //It is possible for this cell to be one of the types passed in
    fun canBe(vararg t: CellType): Boolean {
        return t.any{it in types}
    }

    fun cannotBe(vararg t: CellType): Boolean {
        return t.none{it in types}
    }

    //This cell must be one of the types passed in and cannot be anything else
    fun mustBe(vararg t: CellType): Boolean {
        return t.any{it in types} && (types - t.toSet()).isEmpty()
    }

    fun toChar(): Char {
        return when(types) {
            setOf(CellType.TREASURE) -> 'T'
            setOf(CellType.TREASURE_ROOM) -> 'O'
            setOf(CellType.MONSTER) -> 'M'
            setOf(CellType.WALL) -> '#'
            setOf(CellType.HALL, CellType.TREASURE_ROOM) -> 'o'
            setOf(CellType.HALL) -> '.'
            setOf(CellType.WALL, CellType.HALL) -> '!'
            setOf(CellType.WALL, CellType.TREASURE_ROOM) -> '$'
            else -> '?'
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is TypeRange) {
            return this.types == other.types
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return types.hashCode()
    }

    override fun toString(): String {
        return toChar().toString()
    }

    companion object {
        fun fromChar(c: Char): TypeRange {
            return when (c) {
                '?' -> TypeRange(setOf(CellType.WALL, CellType.HALL, CellType.TREASURE_ROOM))
                'o' -> TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))
                '.' -> TypeRange(setOf(CellType.HALL))
                '#' -> TypeRange(setOf(CellType.WALL))
                'T' -> TypeRange(setOf(CellType.TREASURE))
                'M' -> TypeRange(setOf(CellType.MONSTER))
                'O' -> TypeRange(setOf(CellType.TREASURE_ROOM))
                '!' -> TypeRange(setOf(CellType.WALL, CellType.HALL))
                '$' -> TypeRange(setOf(CellType.WALL, CellType.TREASURE_ROOM))

                else -> throw RuntimeException("Unknown char [${c}] in TypeRange.fromChar")
            }
        }
    }
}