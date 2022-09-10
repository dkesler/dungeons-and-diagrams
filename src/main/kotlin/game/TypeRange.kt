package game

class TypeRange(val types: Set<Type>) {
    //TODO:  add validation regarding type mixes (e.g. cannot have a range of monster+wall)
    val known = types.count() == 1

    fun eq(t: Type): Boolean {
        return types.count() == 1 && types.contains(t)
    }

    //It is possible for this cell to be one of the types passed in
    fun canBe(vararg t: Type): Boolean {
        return t.any{it in types}
    }

    fun cannotBe(vararg t: Type): Boolean {
        return t.none{it in types}
    }

    //This cell must be one of the types passed in and cannot be anything else
    fun mustBe(vararg t: Type): Boolean {
        return t.any{it in types} && (types - t.toSet()).isEmpty()
    }

    fun toChar(): Char {
        return when(types) {
            setOf(Type.TREASURE) -> 'T'
            setOf(Type.ROOM) -> 'O'
            setOf(Type.MONSTER) -> 'M'
            setOf(Type.WALL) -> '#'
            setOf(Type.HALLWAY, Type.ROOM) -> 'o'
            setOf(Type.HALLWAY) -> '.'
            setOf(Type.WALL, Type.HALLWAY) -> '!'
            setOf(Type.WALL, Type.ROOM) -> '$'
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
                '?' -> TypeRange(setOf(Type.WALL, Type.HALLWAY, Type.ROOM))
                'o' -> TypeRange(setOf(Type.HALLWAY, Type.ROOM))
                '.' -> TypeRange(setOf(Type.HALLWAY))
                '#' -> TypeRange(setOf(Type.WALL))
                'T' -> TypeRange(setOf(Type.TREASURE))
                'M' -> TypeRange(setOf(Type.MONSTER))
                'O' -> TypeRange(setOf(Type.ROOM))
                '!' -> TypeRange(setOf(Type.WALL, Type.HALLWAY))
                '$' -> TypeRange(setOf(Type.WALL, Type.ROOM))

                else -> throw RuntimeException("Unknown char [${c}] in TypeRange.fromChar")
            }
        }
    }
}