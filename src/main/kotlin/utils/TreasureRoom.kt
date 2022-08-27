package utils

import Space

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

    fun cannotExpandLeft(grid: List<List<Space>>, offset: Int): Boolean {
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
        return false
    }

    fun cannotExpandRight(grid: List<List<Space>>, offset: Int): Boolean {
        if (box.maxCol + offset >= grid[0].size) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow, box.maxCol+offset)

        //if the expanded room contains a wall, monster, or 2+ treasures, we can't expand left
        val augmentedRoomTypes = augmentedRoom.points().map{grid[it.first][it.second]}
        if (augmentedRoomTypes.contains(Space.WALL) || augmentedRoomTypes.contains(Space.MONSTER)) {
            return true
        }
        if (augmentedRoomTypes.count{it == Space.TREASURE} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand left
        val rightNeighbors = augmentedRoom.rightNeighbors(grid[0].size)
        val rightTypes = rightNeighbors.map { grid[it.first][it.second] }
        if (rightTypes.contains(Space.MONSTER) || rightTypes.contains(Space.TREASURE)) {
            return true
        }
        return false
    }

    fun cannotExpandDown(grid: List<List<Space>>, offset: Int): Boolean {
        if (box.maxRow + offset >= grid.size) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow+offset, box.maxCol)

        //if the expanded room contains a wall, monster, or 2+ treasures, we can't expand left
        val augmentedRoomTypes = augmentedRoom.points().map{grid[it.first][it.second]}
        if (augmentedRoomTypes.contains(Space.WALL) || augmentedRoomTypes.contains(Space.MONSTER)) {
            return true
        }
        if (augmentedRoomTypes.count{it == Space.TREASURE} > 1) {
            return true
        }

        //if the left neighbor of the expanded room is a treasure or a monster, we can't expand left
        val downNeighbors = augmentedRoom.downNeighbors(grid[0].size)
        val downTypes = downNeighbors.map { grid[it.first][it.second] }
        if (downTypes.contains(Space.MONSTER) || downTypes.contains(Space.TREASURE)) {
            return true
        }
        return false
    }

    fun cannotExpandUp(grid: List<List<Space>>, offset: Int): Boolean {
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
        return false
    }
}