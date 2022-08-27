import metrics.Bifurcation
import metrics.EvaluationMetric
import metrics.Solve
import metrics.Step
import rules.*
import java.util.Optional
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val file = args[0]
    val board = Loader.load(file)
    board.draw()
    println("")
    val solved = solve(board);
    if (!solved.successful) {
        println("Could not solve board")
    } else {
        println("Solved")
    }
}

fun solve(board: Board): Solve {
    val rules = listOf(
        WallsExhausted(),
        EmptyExhausted(),
        EmptyCantReachTreasure(),
        EmptyishTwoByTwoIsTreasureRoom()
    )
    var b = board
    val steps = mutableListOf<Step>()

    while(!b.solved()) {

        val (applyResult, evaluations) = applyRules(b, rules)
        if (!applyResult.applicable) {
            println("No rules applicable, bifurcating")
            val (solvedBoard, successful, bifurcation) = bifurcate(b)
            steps.add(Step(evaluations, bifurcation))
            return Solve(solvedBoard, successful, steps)
        } else {
            steps.add(Step(evaluations, null))
            b = applyResult.newBoard
            println("Applying rule: ${applyResult.description}")
            b.draw()
            println("")
        }
    }

    return Solve(b, true, steps)
}

fun bifurcate(board: Board): Triple<Board, Boolean, Bifurcation> {
    //no rules apply, bifurcate
    var probes = 0
    var wastedTimeMillis = 0L

    for (rowIdx in board.grid.indices) {
        for (colIdx in board.grid[0].indices) {
            //if space is unknown, make it a wall and try to solve.
            //if we successfully solve, return solve, otherwise try next unknown
            if (board.grid[rowIdx][colIdx] == Space.UNKNOWN) {
                probes++
                val probeStart = System.currentTimeMillis()
                println("Checking rule: Bifurcation.row[$rowIdx].col[$colIdx]")
                val update = board.update(rowIdx, colIdx, Space.WALL)
                if (update.valid) {
                    println("Applying rule: Bifurcation.row[$rowIdx].col[$colIdx]")
                    update.board.draw()
                    val solve = solve(update.board)
                    if (solve.successful) {
                        return Triple(solve.board, solve.successful, Bifurcation(probes, wastedTimeMillis, solve.steps))
                    }
                } else {
                    println("Invalid bifurcation probe: ${update.invalidReason}")
                    wastedTimeMillis += System.currentTimeMillis() - probeStart
                }
            }
        }
    }
    return Triple(board, false, Bifurcation(probes, wastedTimeMillis, listOf()))
}
fun applyRules(board: Board, rules: List<Rule>): Pair<ApplyResult, List<EvaluationMetric>> {
    var evaluations = mutableListOf<EvaluationMetric>()
    for (rule in rules) {
        val evalStart = System.currentTimeMillis()
        val tryApply = rule.apply(board)
        val evalTime = System.currentTimeMillis() - evalStart;
        evaluations.add(EvaluationMetric(tryApply.rule, tryApply.applicable, evalTime))
        if (tryApply.applicable) {
            return Pair(tryApply, evaluations)
        }
    }
    return Pair(ApplyResult(false, "", "", board), evaluations)
}


