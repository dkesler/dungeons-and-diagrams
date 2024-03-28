import game.Board
import game.Type
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
    val rules = listOf<Rule>(
        WallsExhausted(),
        EmptyExhausted(),
        MonsterRequiresHallway(),
        MonsterMayHaveAtMostOneHallway(),
        TreasureRoomCannotBeConcave(),
        EmptyCannotDeadend(),
        EmptyCannotBeIsolated(),
        AvoidCreatingDeadEnd(),
        AvoidTwoByTwoHall(),

        LastGapCantCreateDeadend(),
        LastWallCantCreateDeadend(),
        TreasureExpandsAwayFromWall(),
        EmptyishTwoByTwoIsTreasureRoom(),
        LastTwoWallsCantCreateCulDeSac(),

        Railroad(),
        WallBoundBoxInternalStructure(),
        IBeam(),
        IncompleteKnownHallCrawlMustExpand(),
        IncompleteUnknownHallCrawlCantBeHallway(),

        //Generally, if we reach these rules (other than AllTreasureRoomsComplete as the very last step), we're not
        //solving very human-like or elegantly
        AllTreasureRoomsComplete(),
        TreasureRoomCannotExpand(),
        MonsterCantTouchTreasureRoom(),
        CantReachTreasure(),
    )
    var b = board
    val steps = mutableListOf<Step>()

    while(!b.solved()) {
        val (applyResult, evaluations) = applyRules(b, rules)
        if (applyResult.contradiction) {
            println("Contradiction found when applying rule ${applyResult.rule}.${applyResult.description}, no solution possible")
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
            println("Applying rule: ${applyResult.rule}.${applyResult.description}")
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

    val firstUnknownWallableCoordinates = (board.grid.rows).flatMap{ row -> board.grid.cols.map{Pair(row, it)}}
        .first{
            val cell = board.grid.cells[it.first][it.second]
            !cell.known && cell.canBe(Type.WALL)
        }
    val rowIdx = firstUnknownWallableCoordinates.first
    val colIdx = firstUnknownWallableCoordinates.second
    val cellTypes = board.grid.cells[rowIdx][colIdx]

    //TOOO: Dedup the these three blocks

    probes++
    val probeStartWall = System.currentTimeMillis()
    println("Checking rule: Bifurcation.row[$rowIdx].col[$colIdx] = WALL")
    if (cellTypes.canBe(Type.WALL)) {
        val updateWall = board.update(rowIdx, colIdx, setOf(Type.WALL))
        wastedTimeMillis += if (updateWall.valid) {
            println("Applying rule: Bifurcation.row[$rowIdx].col[$colIdx] = WALL")
            updateWall.board.draw()
            val solve = solve(updateWall.board, config)
            if (solve.successful) {
                return Triple(solve.board, solve.successful, Bifurcation(probes, wastedTimeMillis, solve.steps))
            } else {
                println("Failed bifurcation probe")
                System.currentTimeMillis() - probeStartWall
            }
        } else {
            println("Invalid bifurcation probe: ${updateWall.invalidReason}")
            System.currentTimeMillis() - probeStartWall
        }
    }

    val probeStartHall = System.currentTimeMillis()
    println("Checking rule: Bifurcation.row[$rowIdx].col[$colIdx] = HALLWAY")
    if (cellTypes.canBe(Type.HALLWAY)) {
        val updateHall = board.update(rowIdx, colIdx, setOf(Type.HALLWAY))
        if (updateHall.valid) {
            println("Applying rule: Bifurcation.row[$rowIdx].col[$colIdx] = HALLWAY")
            updateHall.board.draw()
            val solve = solve(updateHall.board, config)
            if (solve.successful) {
                return Triple(solve.board, solve.successful, Bifurcation(probes, wastedTimeMillis, solve.steps))
            } else {
                println("Failed bifurcation probe")
                System.currentTimeMillis() - probeStartHall
            }
        } else {
            println("Invalid bifurcation probe: ${updateHall.invalidReason}")
            wastedTimeMillis += System.currentTimeMillis() - probeStartHall
        }
    }

    val probeStartRoom = System.currentTimeMillis()
    println("Checking rule: Bifurcation.row[$rowIdx].col[$colIdx] = ROOM")
    if (cellTypes.canBe(Type.ROOM)) {
        val updateRoom = board.update(rowIdx, colIdx, setOf(Type.ROOM))
        if (updateRoom.valid) {
            println("Applying rule: Bifurcation.row[$rowIdx].col[$colIdx] = ROOM")
            updateRoom.board.draw()
            val solve = solve(updateRoom.board, config)
            if (solve.successful) {
                return Triple(solve.board, solve.successful, Bifurcation(probes, wastedTimeMillis, solve.steps))
            } else {
                println("Failed bifurcation probe")
                System.currentTimeMillis() - probeStartRoom
            }
        } else {
            println("Invalid bifurcation probe: ${updateRoom.invalidReason}")
            wastedTimeMillis += System.currentTimeMillis() - probeStartRoom
        }
    }

    return Triple(board, false, Bifurcation(probes, wastedTimeMillis, listOf()))
}
fun applyRules(board: Board, rules: List<Rule>): Pair<ApplyResult, List<EvaluationMetric>> {
    val evaluations = mutableListOf<EvaluationMetric>()
    for (rule in rules) {
        val evalStart = System.currentTimeMillis()
        val tryApply = rule.apply(board)
        val evalTime = System.currentTimeMillis() - evalStart
        evaluations.add(EvaluationMetric(tryApply.rule, tryApply.applicable, evalTime))
        if (tryApply.applicable) {
            return Pair(tryApply, evaluations)
        }
    }
    return Pair(ApplyResult(false, false, "", "", board), evaluations)
}


