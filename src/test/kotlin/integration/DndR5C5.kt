package integration

import SolverConfiguration

class DndR5C5 : BaseIntegrationTest() {
    override val file = "/dnd5-5"
    override val config = SolverConfiguration(false)
}