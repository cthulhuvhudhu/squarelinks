package squarelinks.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import squarelinks.model.SquareLinks.Properties.REWARD
import squarelinks.model.SquareLinks.Properties.START_COINS

@Serializable
class Transaction(val payer: String, val payee: String, val amount: Int) {
    override fun toString(): String {
        return "$payer sent $amount to $payee"
    }

    companion object {
        fun decode(b: ByteArray): Transaction {
            return Json.decodeFromString<Transaction>(String(b))
        }

        fun encode(t: Transaction): () -> String = { Json.encodeToString(t) }
    }
}

class Ledger internal constructor(squares: Collection<Square>) : KoinComponent {

    private val userBalances = mutableMapOf<String, Int>()

    init {
        squares.forEach { process(it) }
    }

    internal fun process(sq: Square) {
        transact(*sq.getTransactions())
        reward(sq.creator)
    }

    private fun transact(vararg t: Transaction) {
        t.forEach {
            val outBal = userBalances.getOrPut(it.payer) { START_COINS }
            val inBal = userBalances.getOrPut(it.payee) { START_COINS }
            assert(outBal >= it.amount) { "Error in Transaction: $t.payer has insufficient coins" }
            if (outBal >= it.amount) {
                userBalances[it.payer] = outBal - it.amount
                userBalances[it.payee] = inBal + it.amount
            }
        }
    }

    private fun reward(user: String) {
        val bal = userBalances.getOrPut(user) { START_COINS }
        userBalances[user] = bal + REWARD
    }

    override fun toString(): String {
        return userBalances.toString()
    }
}