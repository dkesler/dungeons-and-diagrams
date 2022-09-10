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

    private fun expandedRoomInvalid(augmentedRoom: Box, board: Board): Boolean {
        //if the expanded room contains a wall, monster, or 2+ treasures, it's invalid
        val augmentedRoomPoints = board.grid.subgrid(augmentedRoom).flatten()

        if (augmentedRoomPoints.any{ it.type.mustBe(CellType.WALL, CellType.MONSTER)}) {
            return true
        }
        if (augmentedRoomPoints.count{it.type.eq(CellType.TREASURE)} > 1) {
            return true
        }

        //if the expanded room neighbors a monster or a treasure, it's invalid
        val horizontalNeighbors = board.grid.horizontalNeighbors(augmentedRoom)
        val verticalNeighbors = board.grid.verticalNeighbors(augmentedRoom)
        val neighbors = horizontalNeighbors + verticalNeighbors

        if (neighbors.any{ it.type.mustBe(CellType.TREASURE, CellType.MONSTER) }) {
            return true
        }

        //if the expanded room has more than one hall neighbor, it's invalid
        if (neighbors.count{it.type.eq(CellType.HALL)} > 1) {
            return true
        }

        //if the expanded room is max width and it has multple empty horizontal neighbors, it's invalid
        if (augmentedRoom.width() == 3) {
            val nonWallHorizontalNeighbors = horizontalNeighbors
                .count { !it.type.canBe(CellType.WALL) }
            if (nonWallHorizontalNeighbors > 1) {
                return true
            }
        }

        //if the expanded room is max height and it has multple empty vertical neighbors, it's invalid
        if (augmentedRoom.height() == 3) {
            val nonWallVerticalNeighbors = verticalNeighbors
                .count { !it.type.canBe(CellType.WALL) }
            if (nonWallVerticalNeighbors > 1) {
                return true
            }
        }


        //if the expanded room's rows + cols gaps don't leave enough room for walls in the row/col, it's invalid
        for (rowIdx in augmentedRoom.minRow..augmentedRoom.maxRow) {
            val maxWallsInRow = board.grid.row(rowIdx)
                .filter { !augmentedRoom.contains(it.row, it.col) }
                .count { it.type.canBe(CellType.WALL) }
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
                .count { it.type.canBe(CellType.WALL) }
            if (board.colReqs[colIdx] > maxWallsInCol) return true

            //The check above is only checking the current width/height of the room which may not be max width/height
            //since we don't know which direction the room might expand.  but if the row needs more than nCols-2 walls
            //there's no way we could fit a treasure room in
            if (board.colReqs[colIdx] >= board.grid.numRows - 2) {
                return true
            }
        }


        //if the expanded room is max width and it's horizontal neighbor cols don't have enough room for walls, it's invalid
        //if the expanded room is max height and it's vertical neighbor rows don't have enough room for walls, it's invalid

        return false
    }

    fun cannotExpandLeft(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.minCol - offset < 0) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol-offset, box.maxRow, box.maxCol)

        //if the augmented room is width 3 and the col to the right of the augmented room can't fit the necessary walls
        //we can't expand left
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        val hasExit = hallNeighbors == 1
        if (augmentedRoom.width() == 3 && augmentedRoom.maxCol+1 < board.grid.maxCol ) {
            val colToRight = augmentedRoom.maxCol + 1
            if (board.colReqs[colToRight] <= 1) {
                return true
            }

            //number of walls we can add to col to right
            val wallsRemaining = board.colReqs[colToRight] - board.grid.col(colToRight).count{it.type.eq(CellType.WALL) }
            //minimum number of walls in col to right that we will end up having to convert to wall if we expand the
            //treasure room left.  if we don't yet have an exit, subtract 1 since the exit could be in the right neighbors
            val cellsToRightThatMustBeChangedToWall = augmentedRoom.rightNeighbors(board.grid.numCols)
                .map{ board.grid.cells[it.first][it.second] }
                .count{ !it.known && it.canBe(CellType.WALL) } -
                    if (hasExit) 0 else 1


            if (cellsToRightThatMustBeChangedToWall > wallsRemaining) {
                return true
            }
        }

        //if the resulting number of empty cells in any row is greater than the max possible, we can't expand left
        for (rowIdx in (augmentedRoom.minRow..augmentedRoom.maxRow)) {
            val maxEmptiesPossibleInRow = board.grid.numCols - board.rowReqs[rowIdx]
            val emptiesKnownInRow = board.grid.row(rowIdx)
                .count{ !augmentedRoom.containsCol(it.col) && !it.type.canBe(CellType.WALL) } +
                    augmentedRoom.width()
            if (emptiesKnownInRow > maxEmptiesPossibleInRow) {
                return true
            }
        }

        return expandedRoomInvalid(augmentedRoom, board)
    }

    fun cannotExpandRight(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.maxCol + offset >= grid[0].size) return true
        if (box.maxCol - box.minCol + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow, box.maxCol+offset)

        //if the augmented room is width 3 and the col to the left of the augmented room can't fit the necessary walls
        //we can't expand right
        //If the augmented room has more than one neighbors of type HALL, we can't expand right
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        val hasExit = hallNeighbors == 1
        if (augmentedRoom.width() == 3 && augmentedRoom.minCol-1 >= 0 ) {
            val colToLeft = augmentedRoom.minCol-1
            if (board.colReqs[colToLeft] <= 1) {
                return true
            }

            //number of walls we can add to col to left
            val wallsRemaining = board.colReqs[colToLeft] - board.grid.col(colToLeft).count{it.type.eq(CellType.WALL) }
            //minimum number of walls in col to left that we will end up having to convert to wall if we expand the
            //treasure room right.  if we don't yet have an exit, subtract 1 since the exit could be in the left neighbors
            val cellsToLeftThatMustBeChangedToWall = augmentedRoom.leftNeighbors()
                .map{ board.grid.cells[it.first][it.second] }
                .count{ !it.known && it.canBe(CellType.WALL) } -
                    if (hasExit) 0 else 1


            if (cellsToLeftThatMustBeChangedToWall > wallsRemaining) {
                return true
            }
        }


        //if the resulting number of empty cells in any row is greater than the max possible, we can't expand right
        for (rowIdx in (augmentedRoom.minRow..augmentedRoom.maxRow)) {
            val maxEmptiesPossibleInRow = board.grid.numCols - board.rowReqs[rowIdx]
            val emptiesKnownInRow = board.grid.row(rowIdx)
                .count{ !augmentedRoom.containsCol(it.col) && !it.type.canBe(CellType.WALL) } +
                    augmentedRoom.width()
            if (emptiesKnownInRow > maxEmptiesPossibleInRow) {
                return true
            }
        }

        return expandedRoomInvalid(augmentedRoom, board)
    }

    fun cannotExpandDown(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.maxRow + offset >= grid.size) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow, box.minCol, box.maxRow+offset, box.maxCol)

        //if the augmented room is height 3 and the row above the augmented room can't fit the necessary walls
        //we can't expand down
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}

        val hasExit = hallNeighbors == 1
        if (augmentedRoom.height() == 3 && augmentedRoom.minRow-1 >= 0 ) {
            val rowAbove = augmentedRoom.minRow-1
            if (board.rowReqs[rowAbove] <= 1) {
                return true
            }

            //number of walls we can add to row above
            val wallsRemaining = board.rowReqs[rowAbove] - board.grid.row(rowAbove).count{it.type.eq(CellType.WALL) }
            //minimum number of walls in row above that we will end up having to convert to wall if we expand the
            //treasure room down.  if we don't yet have an exit, subtract 1 since the exit could be in the up neighbors
            val cellsAboveThatMustBeChangedToWall = augmentedRoom.upNeighbors()
                .map{ board.grid.cells[it.first][it.second] }
                .count{ !it.known && it.canBe(CellType.WALL) } -
                    if (hasExit) 0 else 1


            if (cellsAboveThatMustBeChangedToWall > wallsRemaining) {
                return true
            }
        }


        //if the resulting number of empty cells in any col is greater than the max possible, we can't expand down
        for (colIdx in (augmentedRoom.minCol..augmentedRoom.maxCol)) {
            val maxEmptiesPossibleInCol = board.grid.numRows - board.colReqs[colIdx]
            val emptiesKnownInCol = board.grid.col(colIdx)
                .count{ !augmentedRoom.containsRow(it.row) && !it.type.canBe(CellType.WALL) } +
                    augmentedRoom.height()
            if (emptiesKnownInCol > maxEmptiesPossibleInCol) {
                return true
            }
        }

        return expandedRoomInvalid(augmentedRoom, board)
    }

    fun cannotExpandUp(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
        if (box.minRow - offset < 0) return true
        if (box.maxRow - box.minRow + offset > 2) return true

        val augmentedRoom = Box(box.minRow-offset, box.minCol, box.maxRow, box.maxCol)

        //if the augmented room is height 3 and the row below the augmented room can't fit the necessary walls
        //we can't expand up
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        val hasExit = hallNeighbors == 1
        if (augmentedRoom.height() == 3 && augmentedRoom.maxRow+1 < board.grid.numRows ) {
            val rowBelow = augmentedRoom.maxRow+1
            if (board.rowReqs[rowBelow] <= 1) {
                return true
            }

            //number of walls we can add to row above
            val wallsRemaining = board.rowReqs[rowBelow] - board.grid.row(rowBelow).count{it.type.eq(CellType.WALL) }
            //minimum number of walls in row above that we will end up having to convert to wall if we expand the
            //treasure room down.  if we don't yet have an exit, subtract 1 since the exit could be in the up neighbors
            val cellsBelowThatMustBeChangedToWall = augmentedRoom.downNeighbors(board.grid.numRows)
                .map{ board.grid.cells[it.first][it.second] }
                .count{ !it.known && it.canBe(CellType.WALL) } -
                    if (hasExit) 0 else 1


            if (cellsBelowThatMustBeChangedToWall > wallsRemaining) {
                return true
            }
        }

        //if the resulting number of empty cells in any col is greater than the max possible, we can't expand up
        for (colIdx in (augmentedRoom.minCol..augmentedRoom.maxCol)) {
            val maxEmptiesPossibleInCol = board.grid.numRows - board.colReqs[colIdx]
            val emptiesKnownInCol = board.grid.col(colIdx)
                .count{ !augmentedRoom.containsRow(it.row) && !it.type.canBe(CellType.WALL) } +
                    augmentedRoom.height()
            if (emptiesKnownInCol > maxEmptiesPossibleInCol) {
                return true
            }
        }

        return expandedRoomInvalid(augmentedRoom, board)
    }
}