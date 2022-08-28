package integration

import SolverConfiguration

class Dnd1_5 : BaseIntegrationTest() {
    override val file = "/dnd1-5"
    override val config = SolverConfiguration(false)
}