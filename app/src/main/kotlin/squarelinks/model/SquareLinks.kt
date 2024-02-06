package squarelinks.model

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import squarelinks.App.Companion.NUM_Zs
import squarelinks.service.MsgManager
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger


class SquareLinks : KoinComponent {
    private val msgMan : MsgManager by inject()

    private val links = ConcurrentLinkedDeque<Square>()

    private fun getTailHash(): String {
        return links.peekLast()?.hash ?: "0"
    }

    internal fun offerSquare(magic: Int, data: List<ByteArray>): Square? {
        val sq = Square(magic, getTailHash(), data)
        if (squareIsSuccess(sq)) { return sq }
        return null
    }

    private fun squareIsSuccess(sq: Square): Boolean {
        val pHash = getTailHash()
        synchronized(this) {
            if (pHash == sq.pHash && sq.hash.take(NUM_Zs) == "0".repeat(NUM_Zs)
                && sq.id == getId().toInt()) {
                nextId.incrementAndGet()
                return links.offerLast(sq)
            }
            return false
        }
    }

    fun validate(): Boolean {
        var valid = true
        var prevHash = "0"
        var counter = 1

        for (sq in links) {
            valid = sq.pHash == prevHash && sq.id == counter
            if (!valid) break
            prevHash = sq.hash
            msgMan.validateData(sq.data)
            counter++
        }
        return valid
    }

    fun adjustN(seconds: Long) {
        println("Block was generating for $seconds seconds")
        if (seconds < 15) {
            NUM_Zs++
            println("N was increased to $NUM_Zs")
        } else if (seconds > 60) {
            NUM_Zs--
            println("N was decreased to $NUM_Zs")
        } else {
            println("N stays the same\n")
        }
    }

    companion object Properties {
        private var nextId: AtomicInteger = AtomicInteger(1)
        internal fun getId(): AtomicInteger {
            return nextId
        }
    }
}
