import java.io.File

object Loader {
    fun load(file: String): Board {
        val content = this::class.java.getResource(file)!!.readText()
        val lines = content.split('\n').map{it.trim()}

        val rowReqs = lines[0].split(',').map{it.toInt()}
        val colReqs = lines[1].split(',').map{it.toInt()}

        val monsters = if (lines.size > 2 && lines[2].isNotEmpty()) {
            lines[2].split("|").map { toPoint(it) }.toSet()
        } else {
            setOf()
        }

        val treasures = if (lines.size > 3 && lines[3].isNotEmpty()) {
            lines[3].split("|").map { toPoint(it) }.toSet()
        } else {
            setOf()
        }


        return createBoard(rowReqs, colReqs, monsters, treasures)
    }
    private fun toPoint(s: String): Pair<Int, Int> {
        val split = s.split(',').map { it.toInt() }
        return Pair(split[0], split[1]);
    }
}