package integration

import SolverConfiguration

class Dnd2_5 : BaseIntegrationTest() {
    override val file = "/dnd2-5"
    override val config = SolverConfiguration(false)
}