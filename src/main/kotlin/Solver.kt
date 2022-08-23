import rules.ApplyResult
import rules.EmptyExhausted
import rules.Rule
import rules.WallsExhausted
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
    val rules = listOf<Rule>(WallsExhausted(), EmptyExhausted());
    var b = board

    while(!b.solved()) {
        val newBoard = applyRules(b, rules)
        if (!newBoard.applicable) {
            return Optional.empty();
        }
        b = newBoard.newBoard
        println("Applying rule: ${newBoard.description}")
        b.draw()
        println("")
    }

    return Optional.of(b)
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


