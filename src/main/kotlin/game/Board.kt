package game

import utils.Box
import utils.Point
import utils.TreasureRoom

class Board(
        val rowReqs: List<Int>,
        val colReqs: List<Int>,
        val monsters: Set<Pair<Int, Int>>,
        val treasures: Set<Pair<Int, Int>>,
        val grid: Grid
    ) {
    fun draw(prev: Grid) {
        grid.draw(rowReqs, colReqs, prev)
    }
    fun draw() {
        grid.draw(rowReqs, colReqs, null)
    }

    fun solved(): Boolean {
        val rowsSatisfied = rowReqs.mapIndexed { index, req -> grid.row(index).count { it.type.eq(CellType.WALL) } == req }.all { it }
        val colsSatisfied = colReqs.mapIndexed { index, req -> grid.col(index).count { it.type.eq(CellType.WALL) } == req }.all { it }
        val containsAnyUnknown = grid.cells.flatten().count { !it.known } > 0
        return rowsSatisfied && colsSatisfied && !containsAnyUnknown && isValid(grid, rowReqs, colReqs).first
    }

    fun update(rowIdx: Int, colIdx: Int, typeRange: Set<CellType>): Update {
        //We cannot add types to cells, only remove them
        if (!grid.cells[rowIdx][colIdx].types.containsAll(typeRange)) {
            throw RuntimeException("Tried to add type(s) ${typeRange - grid.cells[rowIdx][colIdx]} to grid[$rowIdx][$colIdx]")
        }

        //TODO: better way of doing this?
        val newGrid = toMutable(grid.cells);
        newGrid[rowIdx][colIdx] = TypeRange(typeRange);

        val isValid = isValid(Grid(newGrid), rowReqs, colReqs)
        val board = Board(
            rowReqs,
            colReqs,
            monsters,
            treasures,
            Grid(toImmutable(newGrid))
        )
        return Update(isValid.first, isValid.second, board)
    }

    fun update(toUpdate: Collection<Point>): Update {
        //We cannot add types to cells, only remove them
        for (point in toUpdate) {
            if (!grid.cells[point.row][point.col].types.containsAll(point.type.types)) {
                throw RuntimeException("Tried to add type(s) ${point.type.types - grid.cells[point.row][point.col]} to grid[${point.row}][${point.col}]")
            }
        }

        //TODO: better way of doing this?
        val newGrid = toMutable(grid.cells);
        for (point in toUpdate) {
            newGrid[point.row][point.col] = point.type
        }

        val isValid = isValid(Grid(newGrid), rowReqs, colReqs)
        val board = Board(
            rowReqs,
            colReqs,
            monsters,
            treasures,
            Grid(toImmutable(newGrid))
        )
        return Update(isValid.first, isValid.second, board)
    }
}

data class Update(val valid: Boolean, val invalidReason: String, val board: Board)

fun isValid(grid: Grid, rowReqs: List<Int>, colReqs: List<Int>): Pair<Boolean, String> {
    for (rowIdx in rowReqs.indices) {
        val rowWalls = grid.cells[rowIdx].count{it.eq(CellType.WALL)}
        val rowUnknowns = grid.cells[rowIdx].count{!it.known && it.canBe(CellType.WALL)}
        if (rowWalls > rowReqs[rowIdx]) return Pair(false, "Too many walls in row $rowIdx")
        if (rowWalls + rowUnknowns < rowReqs[rowIdx]) return Pair(false, "Insufficient space for walls in row $rowIdx")
    }

    for (colIdx in colReqs.indices) {
        val col = grid.cells.map{it[colIdx]}
        val colWalls =  col.count{it.eq(CellType.WALL)}
        val colUnknowns = col.count{!it.known && it.canBe(CellType.WALL)}
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
            ).all { grid.cells[it.first][it.second].eq(CellType.HALLWAY) }
            if (emptyTwoByTwo) return Pair(false, "2x2 Hall starting on ($rowIdx,$colIdx)")
        }
    }

    for (rowIdx in (rowReqs.indices)) {
        for (colIdx in (colReqs.indices)) {
            val isMonster = grid.cells[rowIdx][colIdx].eq(CellType.MONSTER)
            if (isMonster) {
                val neighbors = grid.neighbors(rowIdx, colIdx)
                //if there are more than one neighboring empty, the monster isn't in a dead end
                val neighboringEmpties = neighbors.count{ !it.type.canBe(CellType.WALL) }
                if (neighboringEmpties > 1) return Pair(false, "Monster at ($rowIdx,$colIdx) not in a dead end")
                //monsters can't go directly into treasure rooms
                val neighboringTreasuresOrRoom = neighbors.map{ it.type }.count{it.mustBe(CellType.ROOM, CellType.TREASURE)}
                if (neighboringTreasuresOrRoom > 0) return Pair(false, "Monster at ($rowIdx,$colIdx) neighbors treasure room")
            } else if (!grid.cells[rowIdx][colIdx].canBe(CellType.WALL)) {
                //not a monster and not a wall so cannot be a dead end
                val neighbors = grid.neighbors(rowIdx, colIdx)
                val neighboringWalls = neighbors.count{ it.type.eq(CellType.WALL)}
                if (neighboringWalls == neighbors.size - 1) return Pair(false, "Dead end at ($rowIdx,$colIdx) with no monster")

                if (grid.cells[rowIdx][colIdx].eq(CellType.TREASURE)) {
                    val neighboringHalls = neighbors.count{ it.type.eq(CellType.HALLWAY)}
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
            (treasureRoom.minCol..treasureRoom.maxCol).map{ col -> grid.cells[row][col]}
        }

        val treasureInRoom = treasureRoomContents.count { it.eq(CellType.TREASURE) }
        if (treasureInRoom > 1) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) contains more than one treasure")
        }

        if (treasureRoomHeight == 3 && treasureRoomWidth == 3 && treasureInRoom == 0) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) is complete but does not contain a treasure")
        }

        if (treasureRoomContents.count{ it.eq(CellType.WALL) } > 0) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) contains a wall")
        }

        if (treasureRoomContents.count{ it.eq(CellType.HALLWAY) } > 0) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) contains a hall")
        }

        val treasureRoomNeighbors = grid.neighbors(treasureRoom.box)
        if (treasureRoomNeighbors.count{it.type.eq(CellType.HALLWAY)} > 1) {
            return Pair(false, "Treasure room starting at (${treasureRoom.minRow},${treasureRoom.minCol}) has multiple exits")
        }
    }

    return Pair(true, "")
}
fun getAllTreasureRooms(grid: Grid): Set<TreasureRoom> {
    val visited = mutableSetOf<Pair<Int, Int>>()
    val treasureRooms = mutableSetOf<TreasureRoom>()

    for (row in grid.rows) {
        for (col in grid.cols) {
            val type = grid.cells[row][col]
            if ((type.eq(CellType.ROOM) || type.eq(CellType.TREASURE)) && Pair(row, col) !in visited) {
                val treasureRoomCells = findTreasureRoomStartingAt(row, col, grid)
                visited.addAll(treasureRoomCells)
                val minRow = treasureRoomCells.minOfOrNull { it.first }!!
                val maxRow = treasureRoomCells.maxOfOrNull { it.first }!!
                val minCol = treasureRoomCells.minOfOrNull { it.second }!!
                val maxCol = treasureRoomCells.maxOfOrNull { it.second }!!
                treasureRooms.add(TreasureRoom(Box(minRow, minCol, maxRow, maxCol)))
            }
        }
    }

    return treasureRooms.toSet()
}

fun findTreasureRoomStartingAt(row: Int, col: Int, grid: Grid): Set<Pair<Int, Int>> {
    val visited = mutableSetOf(Pair(row, col))
    val toVisit = mutableSetOf(Pair(row, col))

    while(toVisit.isNotEmpty()) {
        val visiting = toVisit.first()
        toVisit.remove(visiting)
        val neighbors = grid.neighbors(visiting.first, visiting.second)
        val treasureNeighbors = neighbors.filter{ it.type.mustBe(CellType.TREASURE, CellType.ROOM)}
            .map{it.toPair()}
            .filter{ it !in visited }
        visited.addAll(treasureNeighbors)
        toVisit.addAll(treasureNeighbors)
    }
    return visited
}

fun canBeContiguous(grid: Grid): Boolean {
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
        if (grid.cells[visiting.first][visiting.second].cannotBe(CellType.MONSTER)) {
            val neighbors = grid.neighbors(visiting.first, visiting.second)
            neighbors.filter { !it.type.eq(CellType.WALL) }
                .map{it.toPair()}
                .filter { !visited.contains(it) }
                .forEach { toVisit.add(it); visited.add(it) }
        }
    }

    val allEmptyAndMonsters = grid.cells.flatMapIndexed { rowIdx, row -> row.mapIndexed { colIdx, space -> Triple(rowIdx, colIdx, space) } }
        .filter { it.third.cannotBe(CellType.WALL) }
        .map{ Pair(it.first, it.second)}
        .toSet()

    return visited.containsAll(allEmptyAndMonsters)
}

fun findFirstEmpty(grid: Grid): Pair<Int, Int>? {
    for (rowIdx in grid.rows) {
        for (colIdx in grid.cols) {
            val s = grid.cells[rowIdx][colIdx]
            if (s.cannotBe(CellType.MONSTER, CellType.WALL)) return Pair(rowIdx, colIdx)
        }
    }

    return null
}

fun createBoard(
    rowReqs: List<Int>,
    colReqs: List<Int>,
    monsters: Set<Pair<Int, Int>>,
    treasures: Set<Pair<Int, Int>>
) : Board {
    val grid = mutableListOf<MutableList<TypeRange>>()
    for (rIdx in rowReqs.indices) {
        grid.add(mutableListOf())
        for (cIdx in colReqs.indices) {
            val point = Pair(rIdx, cIdx)
            if (point in monsters) {
                grid[rIdx].add(TypeRange(setOf(CellType.MONSTER)))
            } else if (point in treasures) {
                grid[rIdx].add(TypeRange(setOf(CellType.TREASURE)))
            } else {
                grid[rIdx].add(TypeRange(setOf(CellType.WALL, CellType.HALLWAY, CellType.ROOM)))
            }
        }
    }
    return Board(rowReqs, colReqs, monsters, treasures, Grid(toImmutable(grid)));
}

private fun toMutable(l: List<List<TypeRange>>): MutableList<MutableList<TypeRange>> {
    return l.map{ it.toMutableList()}.toMutableList()
}

private fun toImmutable(l: MutableList<MutableList<TypeRange>>): List<List<TypeRange>> {
    return l.map{ it.toList()}.toList()
}
