package metrics

data class Bifurcation(val probes: Int, val wastedTimeMillis: Long, val steps: List<Step>)
