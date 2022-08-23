package integration

import Space
import fromChar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import solve

abstract class BaseIntegrationTest {
    abstract val file: String

    @Test
    fun doTest() {
        val board = Loader.load(file)
        val solution = loadSolution("/integration$file.sol")

        val solved = solve(board)
        if (solved.isEmpty) {
            fail("Did not find a solution")
        }

        assertEquals(solution, solved.get().grid)
    }

    private fun loadSolution(file: String): List<List<Space>> {
        val content = this::class.java.getResource(file)!!.readText()
        val lines = content.split('\n').map { it.trim() }.filter { it != "" }
        return lines.map {
            it.map { fromChar(it) }
        }
    }
}