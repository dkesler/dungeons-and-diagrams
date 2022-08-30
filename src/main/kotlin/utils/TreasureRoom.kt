package utils

import game.Board
import game.CellType
import game.getTreasureRoomNeighbors

class TreasureRoom(val box: Box) {
    val minRow: Int
        get() {
            return box.minRow
        }

    val maxRow: Int
        get() {
            return box.maxRow
        }

    val minCol: Int
        get() {
            return box.minCol
        }

    val maxCol: Int
        get() {
            return box.maxCol
        }

    fun cannotExpandLeft(board: Board, offset: Int): Boolean {
        val grid = board.grid
        if (box.minCol - offset < 0) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol-offset, box.maxRow, box.maxCol)

        //if the expanded room contains a wall, monster, or 2+ treasures, we can't expand left
        val augmentedRoomTypes = augmentedRoom.points().map{grid[it.first][it.second]}
        if (augmentedRoomTypes.any{ it.mustBe(CellType.WALL, CellType.MONSTER)}) {
            return true
        }
        if (augmentedRoomTypes.count{it.eq(CellType.TREASURE)} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand left
        val leftNeighbors = augmentedRoom.leftNeighbors()
        val leftTypes = leftNeighbors.map { grid[it.first][it.second] }
        if (leftTypes.any {it.mustBe(CellType.MONSTER, CellType.TREASURE)}) {
            return true
        }

        //If the augmented room has more than one neighbors that can't be wall, we can't expand left
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max width and has multiple non-wall cells in its horizontal neighbors, we can't expand left
        if (augmentedRoom.width() == 3) {
            val nonWallHorizontalNeighbors = (augmentedRoom.leftNeighbors() + augmentedRoom.rightNeighbors(board.colReqs.size))
                .count { !board.grid[it.first][it.second].canBe(CellType.WALL) }
            if (nonWallHorizontalNeighbors > 1) {
                return true
            }
        }


        //if the col to the left has insufficient CellType for the expanded treasure room, we can't expand left
        if (board.colReqs[augmentedRoom.minCol] >= board.rowReqs.size - 2) {
            return true
        }

        val cellsThatCanBeWallsInColLeft = grid.indices.map { Pair(it, augmentedRoom.minCol) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it.canBe(CellType.WALL) }

        if (cellsThatCanBeWallsInColLeft < board.colReqs[augmentedRoom.minCol]) {
            return true
        }

        return false
    }

    fun cannotExpandRight(board: Board, offset: Int): Boolean {
        val grid = board.grid
        if (box.maxCol + offset >= grid[0].size) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow, box.maxCol+offset)

        //if the expanded room contains a wall, monster, or 2+ treasures, we can't expand right
        val augmentedRoomTypes = augmentedRoom.points().map{grid[it.first][it.second]}
        if (augmentedRoomTypes.any{ it.mustBe(CellType.WALL, CellType.MONSTER)}) {
            return true
        }
        if (augmentedRoomTypes.count{it.eq(CellType.TREASURE)} > 1) {
            return true
        }

        //if the right neighbor of the expanded room is a treasure or a monster, we can't expand right
        val rightNeighbors = augmentedRoom.rightNeighbors(grid[0].size)
        val rightTypes = rightNeighbors.map { grid[it.first][it.second] }
        if (rightTypes.any{ it.mustBe(CellType.TREASURE, CellType.MONSTER)}) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand right
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max width and has multiple non-wall cells in its horizontal neighbors, we can't expand right
        if (augmentedRoom.width() == 3) {
            val nonWallHorizontalNeighbors = (augmentedRoom.leftNeighbors() + augmentedRoom.rightNeighbors(board.colReqs.size))
                .count { !board.grid[it.first][it.second].canBe(CellType.WALL) }
            if (nonWallHorizontalNeighbors > 1) {
                return true
            }
        }

        //if the col to the right has insufficient CellType for the expanded treasure room, we can't expand right
        if (board.colReqs[augmentedRoom.maxCol] >= board.rowReqs.size - 2) {
            return true
        }

        val cellsThatCanBeWallsInColRight = grid.indices.map { Pair(it, augmentedRoom.maxCol) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it.canBe(CellType.WALL) }

        if (cellsThatCanBeWallsInColRight < board.colReqs[augmentedRoom.maxCol]) {
            return true
        }

        return false
    }

    fun cannotExpandDown(board: Board, offset: Int): Boolean {
        val grid = board.grid
        if (box.maxRow + offset >= grid.size) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow+offset, box.maxCol)

        //if the expanded room contains a wall, monster, or 2+ treasures, we can't expand down
        val augmentedRoomTypes = augmentedRoom.points().map{grid[it.first][it.second]}
        if (augmentedRoomTypes.any{ it.mustBe(CellType.WALL, CellType.MONSTER)}) {
            return true
        }
        if (augmentedRoomTypes.count{it.eq(CellType.TREASURE)} > 1) {
            return true
        }

        //if the down neighbor of the expanded room is a treasure or a monster, we can't expand down
        val downNeighbors = augmentedRoom.downNeighbors(grid[0].size)
        val downTypes = downNeighbors.map { grid[it.first][it.second] }

        if (downTypes.any{ it.mustBe(CellType.MONSTER, CellType.TREASURE) }) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand down
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max height and has multiple non-wall cells in its vertical neighbors, we can't expand down
        if (augmentedRoom.height() == 3) {
            val nonWallVerticalNeighbors = (augmentedRoom.upNeighbors() + augmentedRoom.downNeighbors(board.rowReqs.size))
                .count { !board.grid[it.first][it.second].canBe(CellType.WALL) }
            if (nonWallVerticalNeighbors > 1) {
                return true
            }
        }

        //if the row below has insufficient CellType for the expanded treasure room, we can't expand down
        if (board.rowReqs[augmentedRoom.maxRow] >= board.colReqs.size - 2) {
            return true
        }

        val cellsThatCanBeWallsInRowBelow = grid[0].indices.map { Pair(augmentedRoom.maxRow, it) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it.canBe(CellType.WALL)}

        if (cellsThatCanBeWallsInRowBelow < board.rowReqs[augmentedRoom.maxRow]) {
            return true
        }


        return false
    }

    fun cannotExpandUp(board: Board, offset: Int): Boolean {
        val grid = board.grid
        if (box.minRow - offset < 0) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow-offset, box.minCol, box.maxRow, box.maxCol)

        //if the expanded room contains a wall, monster, or 2+ treasures, we can't expand left
        val augmentedRoomTypes = augmentedRoom.points().map{grid[it.first][it.second]}
        if (augmentedRoomTypes.any{ it.mustBe(CellType.WALL, CellType.MONSTER)}) {
            return true
        }
        if (augmentedRoomTypes.count{it.eq(CellType.TREASURE)} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand left
        val upNeighbors = augmentedRoom.upNeighbors()
        val upTypes = upNeighbors.map { grid[it.first][it.second] }
        if (upTypes.any{ it.mustBe(CellType.MONSTER, CellType.TREASURE)}) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand up
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max height and has multiple non-wall cells in its vertical neighbors, we can't expand up
        if (augmentedRoom.height() == 3) {
            val nonWallVerticalNeighbors = (augmentedRoom.upNeighbors() + augmentedRoom.downNeighbors(board.rowReqs.size))
                .count { !board.grid[it.first][it.second].canBe(CellType.WALL) }
            if (nonWallVerticalNeighbors > 1) {
                return true
            }
        }

        //if the row above has insufficient CellType for the expanded treasure room, we can't expand up
        if (board.rowReqs[augmentedRoom.minRow] >= board.colReqs.size - 2) {
            return true
        }

        val cellsThatCanBeWallsInRowAbove = grid[0].indices.map { Pair(augmentedRoom.minRow, it) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it.canBe(CellType.WALL)}

        if (cellsThatCanBeWallsInRowAbove < board.rowReqs[augmentedRoom.minRow]) {
            return true
        }

        return false
    }
}