import game.Board
import game.CellType
import metrics.Bifurcation
import metrics.EvaluationMetric
import metrics.Solve
import metrics.Step
import rules.*

fun main(args: Array<String>) {
    val file = args[0]
    val board = Loader.load(file)
    board.draw()
    println("")
    val solved = solve(board, SolverConfiguration(true))
    if (!solved.successful) {
        println("Could not solve board")
    } else {
        println("Solved")
    }
}
data class SolverConfiguration(val allowBifurcation: Boolean)

fun solve(board: Board, config: SolverConfiguration): Solve {
    val rules = listOf(
        WallsExhausted(),
        EmptyExhausted(),
        MonsterRequiresHallway(),
        MonsterCantTouchTreasureRoom(),
        MonsterMayHaveAtMostOneHallway(),
        ExtendHallway(),
        AvoidCreatingDeadEnd(),
        AvoidTwoByTwoHall(),
        LastGapCantCreateDeadend(),
        TreasureRoomCannotBeConcave(),
        TreasureRoomCannotExpand(),
        TreasureExpandsAwayFromWall(),
        TreasureRoomWithExitMustBeWalled(),
        EmptyishTwoByTwoIsTreasureRoom(),
        WallBoundBoxInternalStructure(),
        AllTreasureRoomsComplete(),
        CantReachTreasure(),
    )
    var b = board
    val steps = mutableListOf<Step>()

    while(!b.solved()) {
        val (applyResult, evaluations) = applyRules(b, rules)
        if (applyResult.contradiction) {
            println("Contradiction found when applying rule ${applyResult.description}, no solution possible")
            steps.add(Step(evaluations, null))
            return Solve(b, false, steps)
        }
        if (!applyResult.applicable) {
            if (config.allowBifurcation) {
                println("No rules applicable, bifurcating")
                val (solvedBoard, successful, bifurcation) = bifurcate(b, config)
                steps.add(Step(evaluations, bifurcation))
                return Solve(solvedBoard, successful, steps)
            } else {
                println("No rules applicable, but bifurcation is disabled.  Returning failure")
                steps.add(Step(evaluations, null))
                return Solve(b, false, steps)
            }
        } else {
            steps.add(Step(evaluations, null))
            val prev = b
            b = applyResult.newBoard
            println("Applying rule: ${applyResult.description}")
            b.draw(prev.grid)
            println("")
        }
    }

    return Solve(b, true, steps)
}

fun bifurcate(board: Board, config: SolverConfiguration): Triple<Board, Boolean, Bifurcation> {
    //no rules apply, bifurcate
    var probes = 0
    var wastedTimeMillis = 0L

    for (rowIdx in board.grid.indices) {
        for (colIdx in board.grid[0].indices) {
            //if space is unknown, make it a wall and try to solve.
            //if we successfully solve, return solve, otherwise try next unknown
            val cell = board.grid[rowIdx][colIdx]
            if (!cell.known && cell.canBe(CellType.WALL)) {
                probes++
                val probeStart = System.currentTimeMillis()
                println("Checking rule: Bifurcation.row[$rowIdx].col[$colIdx]")
                val update = board.update(rowIdx, colIdx, setOf(CellType.WALL))
                if (update.valid) {
                    println("Applying rule: Bifurcation.row[$rowIdx].col[$colIdx]")
                    update.board.draw()
                    val solve = solve(update.board, config)
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
    return Pair(ApplyResult(false, false, "", "", board), evaluations)
}


