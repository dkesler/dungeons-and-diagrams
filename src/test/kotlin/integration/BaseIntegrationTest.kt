package integration

import Loader
import SolverConfiguration
import game.Grid
import game.TypeRange
import metrics.Step
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
            solution.draw(solved.board.rowReqs, solved.board.colReqs, null)
            println("Diff:")
            solved.board.draw(solution)
            fail("Solutions did not match")
        }
    }

    private fun printSolveMetrics(steps: List<Step>) {
        data class Deduction(var timesEvaluated: Int, var timesApplied: Int, var timeSpentMs: Long){}
        val deductionsByName = mutableMapOf<String, Deduction>()
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
                if (eval.rule !in deductionsByName) {
                    deductionsByName[eval.rule] = Deduction(0, 0, 0)
                }
                deductionsByName[eval.rule]!!.timesEvaluated += 1
                deductionsByName[eval.rule]!!.timeSpentMs += eval.timeElapsedMs
                totalEvalTimeMs += eval.timeElapsedMs
                if (eval.applied) deductionsByName[eval.rule]!!.timesApplied += 1
            }
            if (step.bifurcation != null) {
                stepsToAccumulate.addAll(step.bifurcation!!.steps)
                totalBifurcations += 1
                timeWastedBifurcatingMs += step.bifurcation!!.wastedTimeMillis
                totalBifurcationProbes += step.bifurcation!!.probes
            }
        }

        val totalInferences = deductionsByName.values.sumOf { it.timesApplied }
        println("Total Deductions Applied: $totalInferences")
        println("Total Eval Time Millis: $totalEvalTimeMs")
        println("Deduction Stats:")
        val rowFormat = "|%-40s|%-10s|%-10s|%-15s|"
        println(rowFormat.format("Name", "Applied", "Evaluated", "Duration (ms)"))
        for (deduction in deductionsByName) {
            println(rowFormat.format(deduction.key, deduction.value.timesApplied, deduction.value.timesEvaluated, deduction.value.timeSpentMs))
        }

        println("")
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