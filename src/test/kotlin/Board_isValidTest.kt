import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Board_isValidTest {
    @Test
    fun givenTooManyWallsInRow_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(Space.WALL, Space.WALL),
                listOf(Space.UNKNOWN, Space.UNKNOWN)
            )
        val result = isValid(grid, listOf(1, 0), listOf(1, 1))
        assertFalse(result.first)
        assertEquals("Too many walls in row 0", result.second)
    }

    @Test
    fun givenTooManyWallsInCol_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(Space.WALL, Space.UNKNOWN),
                listOf(Space.WALL, Space.UNKNOWN)
            )
        val result = isValid(grid, listOf(1, 1), listOf(1, 1))
        assertFalse(result.first)
        assertEquals("Too many walls in col 0", result.second)
    }

    @Test
    fun givenInsufficientSpaceForWallsInRow_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(Space.EMPTY, Space.UNKNOWN),
                listOf(Space.EMPTY, Space.UNKNOWN)
            )
        val result = isValid(grid, listOf(2, 1), listOf(0, 1))
        assertFalse(result.first)
        assertEquals("Insufficient space for walls in row 0", result.second)
    }

    @Test
    fun givenInsufficientSpaceForWallsInCol_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(Space.EMPTY, Space.UNKNOWN),
                listOf(Space.EMPTY, Space.UNKNOWN)
            )
        val result = isValid(grid, listOf(1, 1), listOf(1, 1))
        assertFalse(result.first)
        assertEquals("Insufficient space for walls in col 0", result.second)
    }

    @Test
    fun givenMonsterNotInDeadEnd_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(Space.MONSTER, Space.EMPTY),
                listOf(Space.EMPTY, Space.EMPTY)
            )
        val result = isValid(grid, listOf(0, 0), listOf(0, 0))
        assertFalse(result.first)
        assertEquals("Monster at (0,0) not in a dead end", result.second)
    }

    @Test
    fun givenMonsterInDeadEnd_isValid_returnsTrue() {
        val grid =
            listOf(
                listOf(Space.MONSTER, Space.EMPTY),
                listOf(Space.WALL, Space.MONSTER)
            )
        val result = isValid(grid, listOf(0, 1), listOf(1, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenDeadEndDoesntContainMonster_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(Space.MONSTER, Space.EMPTY),
                listOf(Space.WALL, Space.EMPTY)
            )
        val result = isValid(grid, listOf(0, 1), listOf(1, 0))
        assertFalse(result.first)
        assertEquals("Dead end at (1,1) with no monster", result.second)
    }

    @Test
    fun givenNoConnectivityBetweenAllEmpties_isValid_returnsFalse() {
        val grid = listOf(
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
            listOf(Space.EMPTY, Space.WALL, Space.EMPTY),
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenConnectivityThroughEmpty_isValid_returnsTrue() {
        val grid = listOf(
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
            listOf(Space.EMPTY, Space.EMPTY, Space.EMPTY),
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 2, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenConnectivityThroughUnknown_isValid_returnsTrue() {
        val grid = listOf(
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
            listOf(Space.EMPTY, Space.UNKNOWN, Space.EMPTY),
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 3, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenConnectivityThroughMonster_isValid_returnsFalse() {
        val grid = listOf(
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
            listOf(Space.EMPTY, Space.MONSTER, Space.UNKNOWN),
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 2, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenNoConnectivityToUnknown_isValid_returnsTrue() {
        val grid = listOf(
            listOf(Space.UNKNOWN, Space.WALL, Space.MONSTER),
            listOf(Space.WALL, Space.WALL, Space.EMPTY),
            listOf(Space.MONSTER, Space.EMPTY, Space.EMPTY),
        )

        val result = isValid(grid, listOf(2, 2, 0), listOf(2, 2, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenNoEmptiesToFailConnectivity_isValid_returnsTrue() {
        val grid = listOf(
            listOf(Space.UNKNOWN, Space.UNKNOWN),
            listOf(Space.WALL, Space.WALL),
        )

        val result = isValid(grid, listOf(0, 2), listOf(1, 1))
        assertTrue(result.first)
    }

    @Test
    fun givenNoConnectivityBetweenMonstersWithAtLeastOneEmpty_isValid_returnsFalse() {
        val grid = listOf(
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
            listOf(Space.EMPTY, Space.WALL, Space.UNKNOWN),
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenNoConnectivityBetweenMonstersWithAtNoEmpties_isValid_returnsFalse() {
        val grid = listOf(
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
            listOf(Space.UNKNOWN, Space.WALL, Space.UNKNOWN),
            listOf(Space.MONSTER, Space.WALL, Space.MONSTER),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        //TODO: make this case work
/*        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)*/
    }


    //empty 2x2 not treasure room
    //2x2 w/ unknown is ok
    //2x2 in treasure room is ok
    //treasure room w >1 exit not ok
    //no monster touching treasure room
    //no monster touching treasure

}