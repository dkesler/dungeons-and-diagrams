package utils

import game.Board
import game.Type

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

    private fun expandedRoomInvalid(augmentedRoom: Box, board: Board): Boolean {
        //if the expanded room contains a wall, monster, or 2+ treasures, it's invalid
        val augmentedRoomPoints = board.grid.subgrid(augmentedRoom).flatten()

        if (augmentedRoomPoints.any{ it.type.mustBe(Type.WALL, Type.MONSTER)}) {
            return true
        }
        if (augmentedRoomPoints.count{it.type.eq(Type.TREASURE)} > 1) {
            return true
        }

        //if the expanded room neighbors a monster or a treasure, it's invalid
        val leftNeighbors = board.grid.leftNeighbors(augmentedRoom)
        val rightNeighbors = board.grid.rightNeighbors(augmentedRoom)
        val upNeighbors = board.grid.upNeighbors(augmentedRoom)
        val downNeighbors = board.grid.downNeighbors(augmentedRoom)
        val horizontalNeighbors = leftNeighbors + rightNeighbors
        val verticalNeighbors = upNeighbors + downNeighbors
        val neighbors = horizontalNeighbors + verticalNeighbors

        if (neighbors.any{ it.type.mustBe(Type.TREASURE, Type.MONSTER) }) {
            return true
        }

        //if the expanded room has more than one hall neighbor, it's invalid
        if (neighbors.count{it.type.eq(Type.HALLWAY)} > 1) {
            return true
        }

        //if the expanded room is max width and it has multple empty horizontal neighbors, it's invalid
        if (augmentedRoom.width() == 3) {
            val nonWallHorizontalNeighbors = horizontalNeighbors
                .count { !it.type.canBe(Type.WALL) }
            if (nonWallHorizontalNeighbors > 1) {
                return true
            }
        }

        //if the expanded room is max height and it has multple empty vertical neighbors, it's invalid
        if (augmentedRoom.height() == 3) {
            val nonWallVerticalNeighbors = verticalNeighbors
                .count { !it.type.canBe(Type.WALL) }
            if (nonWallVerticalNeighbors > 1) {
                return true
            }
        }


        //if the expanded room's rows + cols gaps don't leave enough room for walls in the row/col, it's invalid
        for (rowIdx in augmentedRoom.minRow..augmentedRoom.maxRow) {
            val maxWallsInRow = board.grid.row(rowIdx)
                .filter { !augmentedRoom.contains(it.row, it.col) }
                .count { it.type.canBe(Type.WALL) }
            if (board.rowReqs[rowIdx] > maxWallsInRow) return true

            //The check above is only checking the current width/height of the room which may not be max width/height
            //since we don't know which direction the room might expand.  but if the row needs more than nCols-2 walls
            //there's no way we could fit a treasure room in
            if (board.rowReqs[rowIdx] >= board.grid.numCols - 2) {
                return true
            }
        }


        for (colIdx in augmentedRoom.minCol..augmentedRoom.maxCol) {
            val maxWallsInCol = board.grid.col(colIdx)
                .filter { !augmentedRoom.contains(it.row, it.col) }
                .count { it.type.canBe(Type.WALL) }
            if (board.colReqs[colIdx] > maxWallsInCol) return true

            //The check above is only checking the current width/height of the room which may not be max width/height
            //since we don't know which direction the room might expand.  but if the row needs more than nCols-2 walls
            //there's no way we could fit a treasure room in
            if (board.colReqs[colIdx] >= board.grid.numRows - 2) {
                return true
            }
        }


        val hasExit = neighbors.any{ it.type.eq(Type.HALLWAY) } ||
                (augmentedRoom.width() == 3 && horizontalNeighbors.any{ it.type.mustBe(Type.ROOM, Type.HALLWAY) }) ||
                (augmentedRoom.height() == 3 && verticalNeighbors.any{ it.type.mustBe(Type.ROOM, Type.HALLWAY) })
        val hasLeftExit = leftNeighbors.any {it.type.eq(Type.HALLWAY) } ||
                (augmentedRoom.width() == 3 && leftNeighbors.any{ it.type.mustBe(Type.ROOM, Type.HALLWAY) })
        val hasRightExit = rightNeighbors.any {it.type.eq(Type.HALLWAY) } ||
                (augmentedRoom.width() == 3 && rightNeighbors.any{ it.type.mustBe(Type.ROOM, Type.HALLWAY) })
        val hasUpExit = upNeighbors.any {it.type.eq(Type.HALLWAY) } ||
                (augmentedRoom.height() == 3 && upNeighbors.any{ it.type.mustBe(Type.ROOM, Type.HALLWAY) })
        val hasDownExit = downNeighbors.any {it.type.eq(Type.HALLWAY) } ||
                (augmentedRoom.height() == 3 && downNeighbors.any{ it.type.mustBe(Type.ROOM, Type.HALLWAY) })

        //if the expanded room is max width and it's horizontal neighbor cols don't have enough room for walls, it's invalid
        if (augmentedRoom.width() == 3) {
            val leftColIdx = augmentedRoom.minCol-1
            if (leftColIdx in board.grid.cols) {
                //if the col to the left can't even hold 2 walls, the augmented room must be invalid
                if (board.colReqs[leftColIdx] <= 1) return true
                //if the room has an exit but it's not on the left, and the left col can't hold 3 walls, the augmented room must be invalid
                if (hasExit && !hasLeftExit && board.colReqs[leftColIdx] <= 2) return true

                val leftCol = board.grid.col(leftColIdx)
                val wallsForcedInLeftCol = leftCol.count{
                    it.type.eq(Type.WALL) && !leftNeighbors.contains(it)
                } + augmentedRoom.height() - if (hasLeftExit || !hasExit) 1 else 0

                if (wallsForcedInLeftCol > board.colReqs[leftColIdx]) return true
            }

            val rightColIdx = augmentedRoom.maxCol+1
            if (rightColIdx in board.grid.cols) {
                //if the col to the right can't even hold 2 walls, the augmented room must be invalid
                if (board.colReqs[rightColIdx] <= 1) return true
                //if the room has an exit but it's not on the right, and the right col can't hold 3 walls, the augmented room must be invalid
                if (hasExit && !hasRightExit && board.colReqs[rightColIdx] <= 2) return true

                val rightCol = board.grid.col(rightColIdx)
                val wallsForcedInRightCol = rightCol.count{
                    it.type.eq(Type.WALL) && !rightNeighbors.contains(it)
                } + augmentedRoom.height() - if (hasRightExit || !hasExit) 1 else 0
                if (wallsForcedInRightCol > board.colReqs[rightColIdx]) return true
            }
        }

        //if the expanded room is max height and it's vertical neighbor rows don't have enough room for walls, it's invalid
        if (augmentedRoom.height() == 3) {
            val upRowIdx = augmentedRoom.minRow-1
            if (upRowIdx in board.grid.rows) {
                //if the row above can't even hold 2 walls, the augmented room must be invalid
                if (board.rowReqs[upRowIdx] <= 1) return true
                //if the room has an exit but it's not above, and the above row can't hold 3 walls, the augmented room must be invalid
                if (hasExit && !hasUpExit && board.rowReqs[upRowIdx] <= 2) return true

                val upRow = board.grid.row(upRowIdx)
                val wallsForcedInUpRow = upRow.count{
                    it.type.eq(Type.WALL) && !upNeighbors.contains(it)
                } + augmentedRoom.width() - if (hasUpExit || !hasExit) 1 else 0

                if (wallsForcedInUpRow > board.rowReqs[upRowIdx]) return true
            }

            val downRowIdx = augmentedRoom.maxRow+1
            if (downRowIdx in board.grid.rows) {
                //if the row below can't even hold 2 walls, the augmented room must be invalid
                if (board.rowReqs[downRowIdx] <= 1) return true
                //if the room has an exit but it's not below, and the row below can't hold 3 walls, the augmented room must be invalid
                if (hasExit && !hasDownExit && board.rowReqs[downRowIdx] <= 2) return true

                val downRow = board.grid.row(downRowIdx)
                val wallsForcedInDownRow = downRow.count{
                    it.type.eq(Type.WALL) && !downNeighbors.contains(it)
                } + augmentedRoom.width() - if (hasDownExit || !hasExit) 1 else 0
                if (wallsForcedInDownRow > board.rowReqs[downRowIdx]) return true
            }
        }

        return false
    }

    fun cannotExpandLeft(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.minCol - offset < 0) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol-offset, box.maxRow, box.maxCol)

        return expandedRoomInvalid(augmentedRoom, board)
    }

    fun cannotExpandRight(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.maxCol + offset >= grid[0].size) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow, box.maxCol+offset)

        return expandedRoomInvalid(augmentedRoom, board)
    }

    fun cannotExpandDown(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.maxRow + offset >= grid.size) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow+offset, box.maxCol)

        return expandedRoomInvalid(augmentedRoom, board)
    }

    fun cannotExpandUp(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.minRow - offset < 0) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow-offset, box.minCol, box.maxRow, box.maxCol)
        return expandedRoomInvalid(augmentedRoom, board)
    }
}