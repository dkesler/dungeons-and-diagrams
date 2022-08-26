import rules.*
import java.util.Optional

fun main(args: Array<String>) {
    val file = args[0]
    val board = Loader.load(file)
    board.draw()
    println("")
    val solved = solve(board);
    if (solved.isEmpty) {
        println("Could not solve board")
    } else {
        println("Solved")
    }
}

fun solve(board: Board): Optional<Board> {
    val rules = listOf(
        WallsExhausted(),
        EmptyExhausted(),
        EmptyCantReachTreasure(),
        EmptyishTwoByTwoIsTreasureRoom()
    )
    var b = board

    while(!b.solved()) {
        val newBoard = applyRules(b, rules)
        if (!newBoard.applicable) {
            println("No rules appllicable, bifurcating")
            return bifurcate(b)
        }
        b = newBoard.newBoard
        println("Applying rule: ${newBoard.description}")
        b.draw()
        println("")
    }

    return Optional.of(b)
}

fun bifurcate(board: Board): Optional<Board> {
    //no rules apply, bifurcate
    for (rowIdx in board.grid.indices) {
        for (colIdx in board.grid[0].indices) {
            //if space is unknown, make it a wall and try to solve.
            //if we successfully solve, return solve, otherwise try next unknown
            if (board.grid[rowIdx][colIdx] == Space.UNKNOWN) {
                println("Checking rule: Bifurcation.row[$rowIdx].col[$colIdx]")
                val update = board.update(rowIdx, colIdx, Space.WALL)
                if (update.valid) {
                    println("Applying rule: Bifurcation.row[$rowIdx].col[$colIdx]")
                    update.board.draw()
                    val solve = solve(update.board)
                    if (!solve.isEmpty) {
                        return solve
                    }
                } else {
                    println("Invalid bifurcation probe: ${update.invalidReason}")
                }
            }
        }
    }
    return Optional.empty()
}
fun applyRules(board: Board, rules: List<Rule>): ApplyResult {
    for (rule in rules) {
        val tryApply = rule.apply(board)
        if (tryApply.applicable) {
            return tryApply
        }
    }
    return ApplyResult(false, "", "", board)
}


