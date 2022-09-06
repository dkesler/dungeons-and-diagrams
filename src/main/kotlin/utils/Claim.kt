package utils

class Claim(
    val minPossible: Int,
    val minRequired: Int,
    val maxRequired: Int,
    val maxPossible: Int,
) {

    fun canContain(idx: Int): Boolean {
        return idx in (minPossible..maxPossible)
    }

    fun doesContain(idx: Int): Boolean {
        return idx in (minRequired..maxRequired)
    }
}