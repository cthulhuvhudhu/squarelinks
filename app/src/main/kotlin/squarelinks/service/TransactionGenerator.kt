package squarelinks.service

import squarelinks.model.Transaction
import squarelinks.App.Companion.NUM_MINERS

class TransactionGenerator {
    private val users: MutableList<String>

    init {
        val miners = (1..NUM_MINERS).map { "miner$it" }.toTypedArray()
        val nonMiners = listOf(
            "Cthulhu",
            "Huisbaas",
            "Kapper",
            "Huisgenoot",
            "Fietsenmaker",
            "Kunstgalerie",
            "Hastur",
            "Azathoth",
            "Yog-Sothoth",
            "Groenteboer",
            "Slagerij"
        )
        users = mutableListOf(*miners)
        users.addAll(nonMiners)
    }

    internal fun unsafeGenerate(): Transaction {
        synchronized(this) {
            return Transaction(users.random(), users.random(), (1..100).random())
        }
    }

}
