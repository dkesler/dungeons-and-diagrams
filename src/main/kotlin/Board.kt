class Board(
        val rowReqs: List<Int>,
        val colReqs: List<Int>,
        val monsters: Set<Pair<Int, Int>>,
        val treasures: Set<Pair<Int, Int>>,
        val grid: List<List<Space>>
    ) {
    fun draw() {
        print("  ")
        colReqs.forEach { print(it) }
        println("")
        grid.forEachIndexed{ rIdx, row ->
            print(rowReqs[rIdx])
            print(" ")
            row.forEach{print(it.c)}
            println("")
        }
    }

    fun solved(): Boolean {
        val rowsSatisfied = rowReqs.mapIndexed { index, req -> row(index).filter{it == Space.WALL}.count() == req }.all { it }
        val colsSatisfied = colReqs.mapIndexed { index, req -> col(index).filter{it == Space.WALL}.count() == req }.all { it }
        val containsAnyEmpties = grid.flatten().contains(Space.EMPTY)
        return rowsSatisfied && colsSatisfied && !containsAnyEmpties && isValid(grid, rowReqs, colReqs).first
    }

    fun row(rowIdx: Int): List<Space> {
        return grid[rowIdx]
    }

    fun col(colIdx: Int): List<Space> {
        return grid.map{it[colIdx]}
    }

    fun update(rowIdx: Int, colIdx: Int, space: Space): Update {
        if (grid[rowIdx][colIdx] != Space.UNKNOWN) {
            if (grid[rowIdx][colIdx] != Space.EMPTY) {
                throw RuntimeException("Tried to update grid[$rowIdx][$colIdx] of type [${grid[rowIdx][colIdx]}]")
            } else {
                if (space != Space.HALL && space != Space.TREASURE_ROOM) {
                    throw RuntimeException("Tried to update grid[$rowIdx][$colIdx] from EMTPY to non-empty type $space")
                }
            }
        }
        //TODO: better way of doing this?
        val newGrid = toMutable(grid);
        newGrid[rowIdx][colIdx] = space;

        val isValid = isValid(newGrid, rowReqs, colReqs)
        val board = Board(
            rowReqs,
            colReqs,
            monsters,
            treasures,
            toImmutable(newGrid)
        )
        return Update(isValid.first, isValid.second, board)
    }

}

data class Update(val valid: Boolean, val invalidReason: String, val board: Board)

fun isValid(grid: List<List<Space>>, rowReqs: List<Int>, colReqs: List<Int>): Pair<Boolean, String> {
    for (rowIdx in rowReqs.indices) {
        val rowWalls = grid[rowIdx].count{it == Space.WALL}
        val rowUnknowns = grid[rowIdx].count{it == Space.UNKNOWN}
        if (rowWalls > rowReqs[rowIdx]) return Pair(false, "Too many walls in row $rowIdx")
        if (rowWalls + rowUnknowns < rowReqs[rowIdx]) return Pair(false, "Insufficient space for walls in row $rowIdx")
    }

    for (colIdx in colReqs.indices) {
        val col = grid.map{it[colIdx]}
        val colWalls =  col.count{it == Space.WALL}
        val colUnknowns = col.count{it == Space.UNKNOWN}
        if (colWalls > colReqs[colIdx]) return Pair(false, "Too many walls in col $colIdx")
        if (colWalls + colUnknowns < colReqs[colIdx]) return Pair(false, "Insufficient space for walls in col $colIdx")
    }

    for (rowIdx in (0 until rowReqs.size - 1)) {
        for (colIdx in (0 until colReqs.size - 1)) {
            val emptyTwoByTwo = listOf(
                Pair(rowIdx, colIdx),
                Pair(rowIdx+1, colIdx),
                Pair(rowIdx, colIdx+1),
                Pair(rowIdx+1, colIdx+1)
            ).all { grid[it.first][it.second] == Space.HALL }
            if (emptyTwoByTwo) return Pair(false, "2x2 Hall starting on ($rowIdx,$colIdx)")
        }
    }

    for (rowIdx in (rowReqs.indices)) {
        for (colIdx in (colReqs.indices)) {
            val isMonster = grid[rowIdx][colIdx] == Space.MONSTER
            if (isMonster) {
                val neighbors = neighbors(rowIdx, colIdx, rowReqs.size, colReqs.size)
                //if there are more than one neighboring empty, the monster isn't in a dead end
                val neighboringEmpties = neighbors.count{ grid[it.first][it.second] == Space.EMPTY || grid[it.first][it.second] == Space.HALL}
                if (neighboringEmpties > 1) return Pair(false, "Monster at ($rowIdx,$colIdx) not in a dead end")
                //monsters can't go directly into treasure rooms
                val neighboringTreasuresOrRoom = neighbors.map{ grid[it.first][it.second]}.count{it == Space.TREASURE_ROOM || it == Space.TREASURE}
                if (neighboringTreasuresOrRoom > 0) return Pair(false, "Monster at ($rowIdx,$colIdx) neighbors treasure room")
            } else if (grid[rowIdx][colIdx] == Space.EMPTY || grid[rowIdx][colIdx] == Space.HALL || grid[rowIdx][colIdx] == Space.TREASURE) {
                //free so cannot be a dead end
                val neighbors = neighbors(rowIdx, colIdx, rowReqs.size, colReqs.size)
                val neighboringWalls = neighbors.count{ grid[it.first][it.second] == Space.WALL}
                if (neighboringWalls == neighbors.size - 1) return Pair(false, "Dead end at ($rowIdx,$colIdx) with no monster")

                if (grid[rowIdx][colIdx] == Space.TREASURE) {
                    val neighboringHalls = neighbors.count{ grid[it.first][it.second] == Space.HALL}
                    if (neighboringHalls > 1) {
                        return Pair(false, "Treasure at ($rowIdx,$colIdx) in a hallway")
                    }
                }
            }
        }
    }

    if (!canBeContiguous(grid)) return Pair(false, "Cannot be contiguous")

    val treasureRooms = getAllTreasureRooms(grid)

    for (treasureRoom in treasureRooms) {
        val treasureRoomHeight = treasureRoom.maxRow - treasureRoom.minRow + 1
        if (treasureRoomHeight > 3) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) is more than 3 rows high")
        }

        val treasureRoomWidth = treasureRoom.maxCol - treasureRoom.minCol + 1
        if (treasureRoomWidth > 3) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) is more than 3 columns wide")
        }

        val treasureRoomContents = (treasureRoom.minRow..treasureRoom.maxRow).flatMap{ row ->
            (treasureRoom.minCol..treasureRoom.maxCol).map{ col -> grid[row][col]}
        }

        val treasureInRoom = treasureRoomContents.count { it == Space.TREASURE }
        if (treasureInRoom > 1) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) contains more than one treasure")
        }

        if (treasureRoomHeight == 3 && treasureRoomWidth == 3 && treasureInRoom == 0) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) is complete but does not contain a treasure")
        }

        if (treasureRoomContents.count{ it == Space.WALL } > 0) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) contains a wall")
        }

        if (treasureRoomContents.count{ it == Space.HALL } > 0) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) contains a hall")
        }

        val treasureRoomNeighborPoints = getTreasureRoomNeighbors(treasureRoom, grid)
        val treasureRoomNeighborTypes = treasureRoomNeighborPoints.map{ grid[it.first][it.second] }
        if (treasureRoomNeighborTypes.count{it == Space.HALL} > 1) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) has multiple exits")
        }
    }

    return Pair(true, "")
}

fun getTreasureRoomNeighbors(treasureRoom: TreasureRoom, grid: List<List<Space>>): List<Pair<Int, Int>> {
    val neighbors = mutableSetOf<Pair<Int, Int>>()

    for (col in (treasureRoom.minCol..treasureRoom.maxCol)) {
        neighbors.add(Pair(treasureRoom.minRow-1, col))
        neighbors.add(Pair(treasureRoom.maxRow+1, col))
    }
    for (row in (treasureRoom.minRow..treasureRoom.maxRow)) {
        neighbors.add(Pair(row, treasureRoom.minCol-1))
        neighbors.add(Pair(row, treasureRoom.maxCol+1))
    }

    return neighbors.filter{it.first >= 0}
        .filter{it.first < grid.size}
        .filter{it.second >= 0}
        .filter{it.second < grid[0].size}
}

data class TreasureRoom(val minRow: Int, val minCol: Int, val maxRow: Int, val maxCol: Int)
fun getAllTreasureRooms(grid: List<List<Space>>): Set<TreasureRoom> {
    val visited = mutableSetOf<Pair<Int, Int>>()
    val treasureRooms = mutableSetOf<TreasureRoom>()

    for (row in grid.indices) {
        for (col in grid[0].indices) {
            val type = grid[row][col]
            if ((type == Space.TREASURE_ROOM || type == Space.TREASURE) && Pair(row, col) !in visited) {
                val treasureRoomCells = findTreasureRoomStartingAt(row, col, grid)
                visited.addAll(treasureRoomCells)
                val minRow = treasureRoomCells.minOfOrNull { it.first }!!
                val maxRow = treasureRoomCells.maxOfOrNull { it.first }!!
                val minCol = treasureRoomCells.minOfOrNull { it.second }!!
                val maxCol = treasureRoomCells.maxOfOrNull { it.second }!!
                treasureRooms.add(TreasureRoom(minRow, minCol, maxRow, maxCol))
            }
        }
    }

    return treasureRooms.toSet()
}

fun findTreasureRoomStartingAt(row: Int, col: Int, grid: List<List<Space>>): Set<Pair<Int, Int>> {
    val treasureTypes = setOf(Space.TREASURE_ROOM, Space.TREASURE)

    val visited = mutableSetOf(Pair(row, col))
    val toVisit = mutableSetOf(Pair(row, col))

    while(toVisit.isNotEmpty()) {
        val visiting = toVisit.first()
        toVisit.remove(visiting)
        val neighbors = neighbors(visiting.first, visiting.second, grid.size, grid[0].size)
        val treasureNeighbors = neighbors.filter{ grid[it.first][it.second] in treasureTypes }.filter{ it !in visited }
        visited.addAll(treasureNeighbors)
        toVisit.addAll(treasureNeighbors)
    }
    return visited
}

fun canBeContiguous(grid: List<List<Space>>): Boolean {
    val visited = mutableSetOf<Pair<Int, Int>>()
    val toVisit = mutableSetOf<Pair<Int, Int>>()

    val firstEmpty = findFirstEmpty(grid)
    if (firstEmpty == null) return true

    visited.add(firstEmpty)
    toVisit.add(firstEmpty)

    while(toVisit.isNotEmpty()) {
        val visiting = toVisit.first()
        toVisit.remove(visiting)
        //We can visit a monster, but a monster can't propagate travel
        if (grid[visiting.first][visiting.second] != Space.MONSTER) {
            val neighbors = neighbors(visiting.first, visiting.second, grid.size, grid[0].size)
            neighbors.filter { grid[it.first][it.second] != Space.WALL }
                .filter { !visited.contains(it) }
                .forEach { toVisit.add(it); visited.add(it) }
        }
    }

    val allEmptyAndMonsters = grid.flatMapIndexed { rowIdx, row -> row.mapIndexed { colIdx, space -> Triple(rowIdx, colIdx, space) } }
        .filter { it.third == Space.EMPTY || it.third == Space.MONSTER }
        .map{ Pair(it.first, it.second)}
        .toSet()

    return visited.containsAll(allEmptyAndMonsters)
}

fun findFirstEmpty(grid: List<List<Space>>): Pair<Int, Int>? {
    for (rowIdx in grid.indices) {
        for (colIdx in grid[0].indices) {
            val s = grid[rowIdx][colIdx]
            if (s == Space.EMPTY || s == Space.HALL || s == Space.TREASURE_ROOM || s == Space.TREASURE_ROOM) return Pair(rowIdx, colIdx)
        }
    }

    return null
}

fun neighbors(row: Int, col: Int, rows: Int, cols: Int): Set<Pair<Int, Int>> {
    val n = mutableSetOf<Pair<Int, Int>>()
    if (row-1 >= 0) {
        n.add(Pair(row-1, col))
    }
    if (row+1 < rows) {
        n.add(Pair(row+1, col))
    }
    if (col-1 >= 0) {
        n.add(Pair(row, col-1))
    }
    if (col+1 < cols) {
        n.add(Pair(row, col+1))
    }
    return n.toSet()
}


fun createBoard(
    rowReqs: List<Int>,
    colReqs: List<Int>,
    monsters: Set<Pair<Int, Int>>,
    treasures: Set<Pair<Int, Int>>
) : Board {
    val grid = mutableListOf<MutableList<Space>>()
    for (rIdx in rowReqs.indices) {
        grid.add(mutableListOf())
        for (cIdx in colReqs.indices) {
            val point = Pair(rIdx, cIdx)
            if (point in monsters) {
                grid[rIdx].add(Space.MONSTER)
            } else if (point in treasures) {
                grid[rIdx].add(Space.TREASURE)
            } else {
                grid[rIdx].add(Space.UNKNOWN)
            }
        }
    }
    return Board(rowReqs, colReqs, monsters, treasures, toImmutable(grid));
}

private fun toMutable(l: List<List<Space>>): MutableList<MutableList<Space>> {
    return l.map{ it.toMutableList()}.toMutableList()
}

private fun toImmutable(l: MutableList<MutableList<Space>>): List<List<Space>> {
    return l.map{ it.toList()}.toList()
}