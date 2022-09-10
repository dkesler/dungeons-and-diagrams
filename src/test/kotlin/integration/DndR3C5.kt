package integration

import SolverConfiguration

class DndR3C5 : BaseIntegrationTest() {
    override val file = "/dnd3-5"
    override val config = SolverConfiguration(false)
}