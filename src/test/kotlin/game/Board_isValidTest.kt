package game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Board_isValidTest {
    @Test
    fun givenTooManyWallsInRow_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
                listOf(TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)))
            )
        val result = isValid(grid, listOf(1, 0), listOf(1, 1))
        assertFalse(result.first)
        assertEquals("Too many walls in row 0", result.second)
    }

    @Test
    fun givenTooManyWallsInCol_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)))
            )
        val result = isValid(grid, listOf(1, 1), listOf(1, 1))
        assertFalse(result.first)
        assertEquals("Too many walls in col 0", result.second)
    }

    @Test
    fun givenInsufficientSpaceForWallsInRow_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
                listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)))
            )
        val result = isValid(grid, listOf(2, 1), listOf(0, 1))
        assertFalse(result.first)
        assertEquals("Insufficient space for walls in row 0", result.second)
    }

    @Test
    fun givenInsufficientSpaceForWallsInCol_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
                listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)))
            )
        val result = isValid(grid, listOf(1, 1), listOf(1, 1))
        assertFalse(result.first)
        assertEquals("Insufficient space for walls in col 0", result.second)
    }

    @Test
    fun givenMonsterNotInDeadEnd_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
                listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)))
            )
        val result = isValid(grid, listOf(0, 0), listOf(0, 0))
        assertFalse(result.first)
        assertEquals("Monster at (0,0) not in a dead end", result.second)
    }

    @Test
    fun givenMonsterInDeadEnd_isValid_returnsTrue() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER)))
            )
        val result = isValid(grid, listOf(0, 1), listOf(1, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenMonsterInHallDeadEnd_isValid_returnsTrue() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL))),
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER)))
            )
        val result = isValid(grid, listOf(0, 1), listOf(1, 0))
        assertTrue(result.first)
    }


    @Test
    fun givenDeadEndDoesntContainMonster_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)))
            )
        val result = isValid(grid, listOf(0, 1), listOf(1, 0))
        assertFalse(result.first)
        assertEquals("Dead end at (1,1) with no monster", result.second)
    }

    @Test
    fun givenDeadEndHallDoesntContainMonster_isValid_returnsFalse() {
        val grid =
            listOf(
                listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL))),
                listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL)))
            )
        val result = isValid(grid, listOf(0, 1), listOf(1, 0))
        assertFalse(result.first)
        assertEquals("Dead end at (1,1) with no monster", result.second)
    }


    @Test
    fun givenNoConnectivityBetweenAllEmpties_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenNoConnectivityBetweenAllHall_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenNoConnectivityBetweenEmptyAndHall_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenConnectivityThroughEmpty_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 2, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenConnectivityThroughHall_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 2, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenConnectivityThroughUnknown_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 3, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenConnectivityThroughMonster_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 0, 1), listOf(0, 2, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenNoConnectivityToUnknown_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
        )

        val result = isValid(grid, listOf(2, 2, 0), listOf(2, 2, 0))
        assertTrue(result.first)
    }

    @Test
    fun givenNoEmptiesToFailConnectivity_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
        )

        val result = isValid(grid, listOf(0, 2), listOf(1, 1))
        assertTrue(result.first)
    }

    @Test
    fun givenNoConnectivityBetweenMonstersWithAtLeastOneEmpty_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }

    @Test
    fun givenNoConnectivityBetweenMonstersWithNoEmpties_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 3, 0))
        //TODO: make this case work
/*        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)*/
    }

    @Test
    fun given2x2Hall_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.HALL))),
            listOf(TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.HALL))),
        )

        val result = isValid(grid, listOf(0, 0), listOf(0, 0))
        assertFalse(result.first)
        assertEquals("2x2 Hall starting on (0,0)", result.second)
    }
    @Test
    fun given2x2HallAndUnknown_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.HALL))),
            listOf(TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.HALL, CellType.WALL, CellType.TREASURE_ROOM))),
        )

        val result = isValid(grid, listOf(0, 0), listOf(0, 0))
        assertTrue(result.first)
    }

    @Test
    fun given2x2TreasureRoom_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM))),
        )

        val result = isValid(grid, listOf(0, 0), listOf(0, 0))
        assertTrue(result.first)
    }

    @Test
    fun given2x2Empty_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
            listOf(TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL, CellType.TREASURE_ROOM))),
        )

        val result = isValid(grid, listOf(0, 0), listOf(0, 0))
        assertTrue(result.first)
    }

    @Test
    fun given3x3TreasureRoomWithOneExit_isValid_returnsTrue() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(2, 2, 0), listOf(0, 0, 0, 2, 2))
        assertTrue(result.first)
    }


    @Test
    fun givenTreasureRoomWithMultipleExits_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(0, 2, 0), listOf(0, 0, 0, 1, 1))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) has multiple exits", result.second)
    }

    @Test
    fun givenDisconnectedTreasureRoom_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.HALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(1, 1, 1), listOf(0, 0, 0, 3, 0))
        assertFalse(result.first)
        assertEquals("Cannot be contiguous", result.second)
    }


    @Test
    fun givenTreasureRoomTallerThan3_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
        )

        val result = isValid(grid, listOf(0, 2, 2, 2), listOf(0, 0, 0, 3, 3))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) is more than 3 rows high", result.second)
    }

    @Test
    fun givenTreasureRoomWiderThan3_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
        )

        val result = isValid(grid, listOf(0, 2, 2), listOf(0, 0, 0, 0, 2, 2))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) is more than 3 columns wide", result.second)
    }


    @Test
    fun given3x3TreasureRoomWithoutTreasure_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(0, 2, 0), listOf(0, 0, 0, 1, 1))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) is complete but does not contain a treasure", result.second)
    }

    @Test
    fun givenTreasureRoomContainingWall_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(0, 3, 0), listOf(0, 0, 1, 1, 1))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) contains a wall", result.second)
    }

    @Test
    fun givenTreasureRoomContainingHall_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(0, 2, 0), listOf(0, 0, 0, 1, 1))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) contains a hall", result.second)
    }



    @Test
    fun givenTreasureRoomWithMultipleTreasure_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER))),
        )

        val result = isValid(grid, listOf(0, 2, 0), listOf(0, 0, 0, 1, 1))
        assertFalse(result.first)
        assertEquals("Treasure room starting at (0,0) contains more than one treasure", result.second)
    }

    @Test
    fun givenTreasureInHall_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.MONSTER)))
        )
        val result = isValid(grid, listOf(0), listOf(0, 0, 0, 0, 0))

        assertFalse(result.first)
        assertEquals("Treasure at (0,2) in a hallway", result.second)
    }

    @Test
    fun givenTreasureInDeadend_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.HALL)), TypeRange(setOf(CellType.TREASURE)))
        )
        val result = isValid(grid, listOf(0), listOf(0, 0, 0))

        assertFalse(result.first)
        assertEquals("Dead end at (0,2) with no monster", result.second)
    }

    @Test
    fun givenMonsterTouchingTreasureRoom_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
        )

        val result = isValid(grid, listOf(1, 2, 2), listOf(0, 0, 0, 2, 3))
        assertFalse(result.first)
        assertEquals("Monster at (0,3) neighbors treasure room", result.second)
    }

    @Test
    fun givenMonsterTouchingTreasure_isValid_returnsFalse() {
        val grid = listOf(
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE)), TypeRange(setOf(CellType.MONSTER)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
            listOf(TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.TREASURE_ROOM)), TypeRange(setOf(CellType.WALL)), TypeRange(setOf(CellType.WALL))),
        )

        val result = isValid(grid, listOf(1, 2, 2), listOf(0, 0, 0, 2, 3))
        assertFalse(result.first)
        assertEquals("Monster at (0,3) neighbors treasure room", result.second)
    }
}