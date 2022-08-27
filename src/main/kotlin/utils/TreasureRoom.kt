package utils

import Board
import Space
import getTreasureRoomNeighbors

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
        if (augmentedRoomTypes.contains(Space.WALL) || augmentedRoomTypes.contains(Space.MONSTER)) {
            return true
        }
        if (augmentedRoomTypes.count{it == Space.TREASURE} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand left
        val leftNeighbors = augmentedRoom.leftNeighbors()
        val leftTypes = leftNeighbors.map { grid[it.first][it.second] }
        if (leftTypes.contains(Space.MONSTER) || leftTypes.contains(Space.TREASURE)) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand left
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second] == Space.HALL}
        if (hallNeighbors > 1) {
            return true
        }

        //if the col to the left has insufficient space for the expanded treasure room, we can't expand left
        val wallsAndUnknownsInColLeft = grid.indices.map { Pair(it, augmentedRoom.minCol) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it == Space.WALL || it == Space.UNKNOWN }

        if (wallsAndUnknownsInColLeft < board.colReqs[augmentedRoom.minCol]) {
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
        if (augmentedRoomTypes.contains(Space.WALL) || augmentedRoomTypes.contains(Space.MONSTER)) {
            return true
        }
        if (augmentedRoomTypes.count{it == Space.TREASURE} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand right
        val rightNeighbors = augmentedRoom.rightNeighbors(grid[0].size)
        val rightTypes = rightNeighbors.map { grid[it.first][it.second] }
        if (rightTypes.contains(Space.MONSTER) || rightTypes.contains(Space.TREASURE)) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand right
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second] == Space.HALL}
        if (hallNeighbors > 1) {
            return true
        }

        //if the col to the right has insufficient space for the expanded treasure room, we can't expand right
        val wallsAndUnknownsInColRight = grid.indices.map { Pair(it, augmentedRoom.maxCol) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it == Space.WALL || it == Space.UNKNOWN }

        if (wallsAndUnknownsInColRight < board.colReqs[augmentedRoom.maxCol]) {
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
        if (augmentedRoomTypes.contains(Space.WALL) || augmentedRoomTypes.contains(Space.MONSTER)) {
            return true
        }
        if (augmentedRoomTypes.count{it == Space.TREASURE} > 1) {
            return true
        }

        //if the down neighbor of the expanded room is a treasure or a monster, we can't expand down
        val downNeighbors = augmentedRoom.downNeighbors(grid[0].size)
        val downTypes = downNeighbors.map { grid[it.first][it.second] }
        if (downTypes.contains(Space.MONSTER) || downTypes.contains(Space.TREASURE)) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand down
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second] == Space.HALL}
        if (hallNeighbors > 1) {
            return true
        }

        //if the row below has insufficient space for the expanded treasure room, we can't expand down
        val wallsAndUnknownsInRowBelow = grid[0].indices.map { Pair(augmentedRoom.maxRow, it) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it == Space.WALL || it == Space.UNKNOWN }

        if (wallsAndUnknownsInRowBelow < board.rowReqs[augmentedRoom.maxRow]) {
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
        if (augmentedRoomTypes.contains(Space.WALL) || augmentedRoomTypes.contains(Space.MONSTER)) {
            return true
        }
        if (augmentedRoomTypes.count{it == Space.TREASURE} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand left
        val upNeighbors = augmentedRoom.upNeighbors()
        val upTypes = upNeighbors.map { grid[it.first][it.second] }
        if (upTypes.contains(Space.MONSTER) || upTypes.contains(Space.TREASURE)) {
            return true
        }

        //If the augmented room has more than one neighbors of type HALL, we can't expand up
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), grid)
            .count{grid[it.first][it.second] == Space.HALL}
        if (hallNeighbors > 1) {
            return true
        }

        //if the row above has insufficient space for the expanded treasure room, we can't expand up
        val wallsAndUnknownsInRowAbove = grid[0].indices.map { Pair(augmentedRoom.minRow, it) }
            .filter { !augmentedRoom.contains(it) }
            .map{ grid[it.first][it.second] }
            .count{ it == Space.WALL || it == Space.UNKNOWN }

        if (wallsAndUnknownsInRowAbove < board.rowReqs[augmentedRoom.minRow]) {
            return true
        }

        return false
    }
}