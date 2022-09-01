package integration

import Loader
import SolverConfiguration
import game.Board
import game.Grid
import game.TypeRange
import game.draw
import metrics.Step
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import solve

abstract class BaseIntegrationTest {
    abstract val file: String
    open val config: SolverConfiguration = SolverConfiguration(true)

    @Test
    fun doTest() {
        val board = Loader.load(file)
        val solution = Grid(loadSolution("/integration$file.sol"))

        val solved = solve(board, config)
        printSolveMetrics(solved.steps)

        if (!solved.successful) {
            fail("Did not find a solution")
        }

        if (solution.cells != solved.board.grid.cells) {
            println("Solutions do not match.  Final solution:")
            solved.board.draw()
            println("Expected solution:")
            draw(solved.board.rowReqs, solved.board.colReqs, solution, null)
            println("Diff:")
            solved.board.draw(solution)
            fail("Solutions did not match")
        }
    }

    private fun printSolveMetrics(steps: List<Step>) {
        data class Inference(var timesEvaluated: Int, var timesApplied: Int, var timeSpentMs: Long){}
        val inferencesByName = mutableMapOf<String, Inference>()
        val stepsToAccumulate = mutableListOf<Step>()
        stepsToAccumulate.addAll(steps)
        var totalBifurcations = 0
        var timeWastedBifurcatingMs = 0L
        var totalBifurcationProbes = 0
        var totalEvalTimeMs = 0L

        while (stepsToAccumulate.isNotEmpty()) {
            val step = stepsToAccumulate.first()
            stepsToAccumulate.remove(step)
            for (eval in step.evaluations) {
                if (eval.rule !in inferencesByName) {
                    inferencesByName[eval.rule] = Inference(0, 0, 0)
                }
                inferencesByName[eval.rule]!!.timesEvaluated += 1
                inferencesByName[eval.rule]!!.timeSpentMs += eval.timeElapsedMs
                totalEvalTimeMs += eval.timeElapsedMs
                if (eval.applied) inferencesByName[eval.rule]!!.timesApplied += 1
            }
            if (step.bifurcation != null) {
                stepsToAccumulate.addAll(step.bifurcation!!.steps)
                totalBifurcations += 1
                timeWastedBifurcatingMs += step.bifurcation!!.wastedTimeMillis
                totalBifurcationProbes += step.bifurcation!!.probes
            }
        }

        val totalInferences = inferencesByName.values.sumOf { it.timesApplied }
        println("Total Inferences Applied: $totalInferences")
        println("Total Eval Time Millis: $totalEvalTimeMs")
        println("Inferences: (name, evaluated, applied, duration)")
        for (inference in inferencesByName) {
            println("${inference.key}, ${inference.value.timesEvaluated}, ${inference.value.timesApplied}, ${inference.value.timeSpentMs}")
        }
        println("Total Bifurcations: $totalBifurcations")
        println("Total Bifurcation Probes: $totalBifurcationProbes")
        println("Time Wasted Bifurcating: $timeWastedBifurcatingMs")
    }

    private fun loadSolution(file: String): List<List<TypeRange>> {
        val content = this::class.java.getResource(file)!!.readText()
        val lines = content.split('\n').map { it.trim() }.filter { it != "" }
        return lines.map {
            it.map { TypeRange.fromChar(it) }
        }
    }
}