package squarelinks.model

import java.util.concurrent.ConcurrentLinkedDeque
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import mu.KotlinLogging

import squarelinks.App
import squarelinks.App.Companion.NUM_Zs
import squarelinks.service.MsgManager
import java.util.concurrent.ConcurrentLinkedQueue

class SquareLinks : KoinComponent {
    private val msgMan: MsgManager by inject()

    private val links = ConcurrentLinkedDeque<Square>()
    private val messages = ConcurrentLinkedQueue<List<ByteArray>>()
    private val log = KotlinLogging.logger {}

    private fun getTailHash(): String {
        return links.peekLast()?.hash ?: "0"
    }

    internal fun isEmpty(): Boolean {
        return links.isEmpty()
    }

    internal fun reset() {
        links.clear()
        nextId.update { 1 }
    }

    internal fun offerSquare(magic: UInt, creator: String): Boolean {
        val sq = Square(magic, getTailHash(), creator)
        return squareIsSuccess(sq)
    }

    internal fun submitMessage(data: List<ByteArray>) {
        messages.add(data)
    }

    private fun captureMessages(): List<List<ByteArray>> {
        val data = messages.toList()
        messages.clear()
        return data
    }

    private fun squareIsSuccess(sq: Square): Boolean {
        val pHash = getTailHash()
        if ((pHash == sq.pHash) && (sq.hash.take(NUM_Zs.value) == "0".repeat(NUM_Zs.value))
            && (sq.id == getId())
//                && validateTransactions(sq)
        ) {
            log.debug { "Candidate ${sq.id} passes checks" }
            sq.data = captureMessages()
            println(sq)
            log.debug { "Incrementing nextId" }
            nextId.getAndIncrement()
            log.debug { "Adding to links" }
            return links.offerLast(sq)
        }
        return false
    }

//    private fun validateTransactions(sq: Square): Boolean {
//        try {
//            Ledger(links).process(sq)
//            return true
//        } catch (e: AssertionError) {
//            println("Invalid transactions, discarding")
//            return false
//        }
//    }

    internal fun validate(): Boolean {
        var valid = true
        var prevHash = "0"
        var counter = 1

        for (sq in links) {
            valid = sq.pHash == prevHash && sq.id == counter
            if (!valid) break
            prevHash = sq.hash
            msgMan.validateData(*sq.data.toTypedArray())
            counter++
        }

//        val ledger = Ledger(links)
//        println("Ledger: $ledger")

        log.info { "Is valid SquareLinks: $valid" }
        return valid
    }

    fun evaluateRuntime(seconds: Int) {
        log.info { "Block was generating for $seconds seconds" }
        if (App.FREEZE_N) {
            log.warn { "N frozen: Skipping adjust N" }
            return
        }
        return (when (seconds) {
            in 0..15 -> {
                if (NUM_Zs.value < App.N_CAP) {
                    NUM_Zs.incrementAndGet()
                    log.info { "N was increased to $NUM_Zs" }
                } else {
                    log.warn { "N is capped: stays at ${App.N_CAP}" }
                }
            }

            in 15..60 -> {
                log.info { "N stays the same at $NUM_Zs" }
            }

            else -> {
                NUM_Zs.decrementAndGet()
                log.info { "N was decreased to $NUM_Zs" }
            }
        })
    }

    companion object Properties {
        private var nextId: AtomicInt = atomic(1)
        internal fun getId(): Int {
            return nextId.value
        }

//        internal const val START_COINS = 100
//        internal const val REWARD = 100
    }
}
