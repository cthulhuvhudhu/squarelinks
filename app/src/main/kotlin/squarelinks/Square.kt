package squarelinks

import java.security.MessageDigest
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

internal data class Square(
    private val magic: Int,
    val pHash: String,
    private val message: String) {

    val id: Int = SquareLinks.getId().toInt()
    private val createdAt: Long = System.currentTimeMillis()
    private val input = "$id$pHash$createdAt$magic"
    val hash = applySha256(input)

    override fun toString(): String {
        return buildString {
            appendLine("Block: $id")
            appendLine("\tHash of the previous block: $pHash")
            appendLine("\tMagic number: $magic")
            appendLine("\tTimestamp: $createdAt")
            appendLine("\tMessage:\n$message")
            append("\tHash of the block: $hash")
        }
    }

    private fun applySha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            /* Applies sha256 to our input */
            val hash = digest.digest(input.toByteArray(charset("UTF-8")))
            val hexString = StringBuilder()
            for (elem in hash) {
                val hex = Integer.toHexString(0xff and elem.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

class SquareLinks {
    private val links = ConcurrentLinkedDeque<Square>()

    private fun getTailHash(): String {
        return links.peekLast()?.hash ?: "0"
    }

    internal fun offerSquare(magic: Int, message: String): Square? {
        val sq = Square(magic, getTailHash(), message)
        if (squareIsSuccess(sq)) { return sq }
        return null
    }

    private fun squareIsSuccess(sq: Square): Boolean {
        val pHash = getTailHash()
        if (pHash == sq.pHash && sq.hash.take(App.Zs) == "0".repeat(App.Zs)) {
            nextId.incrementAndGet()
            return links.offerLast(sq)
        }
        return false
    }

    fun validate(): Boolean {
        var valid = true
        var prevHash = "0"
        var counter = 1

        for (sq in links) {
            valid = sq.pHash == prevHash && sq.id == counter
            if (!valid) break
            prevHash = sq.hash
            counter++
        }
        return valid
    }

    fun adjustN(seconds: Long) {
        print("Block was generating for $seconds seconds - ")
        if (seconds < 15) {
            App.Zs++
            println("N was increased to ${App.Zs}")
        } else if (seconds > 60) {
            App.Zs--
            println("N was decreased to ${App.Zs}")
        } else {
            println("N stays the same")
        }
    }

    companion object Properties {
        private var nextId: AtomicInteger = AtomicInteger(1)
        internal fun getId(): AtomicInteger {
            return nextId
        }
    }
}