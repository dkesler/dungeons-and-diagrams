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
        return rowsSatisfied && colsSatisfied && isValid(grid, rowReqs, colReqs).first
    }

    fun row(rowIdx: Int): List<Space> {
        return grid[rowIdx]
    }

    fun col(colIdx: Int): List<Space> {
        return grid.map{it[colIdx]}
    }

    fun update(rowIdx: Int, colIdx: Int, space: Space): Update {
        if (grid[rowIdx][colIdx] != Space.UNKNOWN) {
            throw RuntimeException("Tried to update grid[$rowIdx][$colIdx] of type [${grid[rowIdx][colIdx]}]")
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
            ).all { grid[it.first][it.second] == Space.EMPTY }
            if (emptyTwoByTwo) return Pair(false, "Empty 2x2 starting on ($rowIdx,$colIdx)")
        }
    }

    for (rowIdx in (rowReqs.indices)) {
        for (colIdx in (colReqs.indices)) {
            val isMonster = grid[rowIdx][colIdx] == Space.MONSTER
            if (isMonster) {
                val neighbors = neighbors(rowIdx, colIdx, rowReqs.size, colReqs.size)
                //if there are more than one neighboring empty, the monster isn't in a dead end
                val neighboringEmpties = neighbors.count{ grid[it.first][it.second] == Space.EMPTY}
                if (neighboringEmpties > 1) return Pair(false, "Monster at ($rowIdx,$colIdx) not in a dead end")
                //monsters can't go directly into treasure rooms
                val neighboringTreasuresOrRoom = neighbors.map{ grid[it.first][it.second]}.count{it == Space.TREASURE_ROOM || it == Space.TREASURE}
                if (neighboringTreasuresOrRoom > 0) return Pair(false, "Monster at ($rowIdx,$colIdx) neighbors treasure room")
            } else if (grid[rowIdx][colIdx] == Space.EMPTY) {
                //free so cannot be a dead end
                val neighbors = neighbors(rowIdx, colIdx, rowReqs.size, colReqs.size)
                val neighboringWalls = neighbors.count{ grid[it.first][it.second] == Space.WALL}
                if (neighboringWalls == neighbors.size - 1) return Pair(false, "Dead end at ($rowIdx,$colIdx) with no monster")
            }
        }
    }

    if (!canBeContiguous(grid)) return Pair(false, "Cannot be contiguous")

    return Pair(true, "")
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
            if (grid[rowIdx][colIdx] == Space.EMPTY) return Pair(rowIdx, colIdx)
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