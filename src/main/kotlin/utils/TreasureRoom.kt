package utils

import game.Board
import game.CellType
import game.getTreasureRoomNeighbors
import kotlin.math.sign

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
        val grid = board.grid.cells
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
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max width and has multiple non-wall cells in its horizontal neighbors, we can't expand left
        if (augmentedRoom.width() == 3) {
            val nonWallHorizontalNeighbors = (augmentedRoom.leftNeighbors() + augmentedRoom.rightNeighbors(board.colReqs.size))
                .count { !board.grid.cells[it.first][it.second].canBe(CellType.WALL) }
            if (nonWallHorizontalNeighbors > 1) {
                return true
            }
        }


        //if the col to the left has insufficient empties for the expanded treasure room, we can't expand left
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

        //if the augmented room is width 3 and the col to the right of the augmented room can't fit the necessary walls
        //we can't expand left
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

        return false
    }

    fun cannotExpandRight(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
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
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max width and has multiple non-wall cells in its horizontal neighbors, we can't expand right
        if (augmentedRoom.width() == 3) {
            val nonWallHorizontalNeighbors = (augmentedRoom.leftNeighbors() + augmentedRoom.rightNeighbors(board.colReqs.size))
                .count { !board.grid.cells[it.first][it.second].canBe(CellType.WALL) }
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

        //if the augmented room is width 3 and the col to the left of the augmented room can't fit the necessary walls
        //we can't expand right
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

        return false
    }

    fun cannotExpandDown(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
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
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max height and has multiple non-wall cells in its vertical neighbors, we can't expand down
        if (augmentedRoom.height() == 3) {
            val nonWallVerticalNeighbors = (augmentedRoom.upNeighbors() + augmentedRoom.downNeighbors(board.rowReqs.size))
                .count { !board.grid.cells[it.first][it.second].canBe(CellType.WALL) }
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

        //if the augmented room is height 3 and the row above the augmented room can't fit the necessary walls
        //we can't expand down
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

        return false
    }

    fun cannotExpandUp(board: Board, offset: Int): Boolean {
        val grid = board.grid.cells
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
        val hallNeighbors = getTreasureRoomNeighbors(TreasureRoom(augmentedRoom), board.grid)
            .count{grid[it.first][it.second].eq(CellType.HALL)}
        if (hallNeighbors > 1) {
            return true
        }

        //if the agumented room is max height and has multiple non-wall cells in its vertical neighbors, we can't expand up
        if (augmentedRoom.height() == 3) {
            val nonWallVerticalNeighbors = (augmentedRoom.upNeighbors() + augmentedRoom.downNeighbors(board.rowReqs.size))
                .count { !board.grid.cells[it.first][it.second].canBe(CellType.WALL) }
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


        //if the augmented room is height 3 and the row below the augmented room can't fit the necessary walls
        //we can't expand up
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

        return false
    }
}