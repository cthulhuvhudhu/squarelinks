package squarelinks

import java.security.MessageDigest
import kotlin.random.Random

data class Square(
    internal val id: Int = 1,
    internal val pHash: String = "0") {

    private var magic: Int = Random.nextInt()
    private val createdAt: Long = System.currentTimeMillis()
    var hash = applyMagicHash()

    override fun toString(): String {
        return buildString {
            appendLine("Block:")
            appendLine("Id: $id")
            appendLine("Timestamp: $createdAt")
            appendLine("Magic number: $magic")
            appendLine("Hash of the previous block:")
            appendLine(pHash)
            appendLine("Hash of the block:")
            append(hash)
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

    private fun input() = "$id$pHash$createdAt$magic"

    private fun applyMagicHash(): String {
        var h = applySha256(input())

        if (App.Zs > 0) {
            while (h.take(App.Zs) != "0".repeat(App.Zs)) {
                // Try new numbers
                magic = Random.nextInt()
                h = applySha256(input())
            }
        }
        return h
    }
}

class SquareLinks {
    private val links = ArrayDeque<Square>()

    internal fun newSquare() {
        val pHash = links.lastOrNull()?.hash ?: "0"
        val sq = measureTimeS { Square(getId(), pHash) }
        links.add(sq)
    }

    fun validate(): Boolean {
        var valid = true
        var i = 0
        while (valid && i < links.size) {
            val s = links[i]
            valid = if (i == 0) {
                s.id == 1 && s.pHash == "0"
            } else {
                s.pHash == links[i-1].hash
            }
            i++
        }
        return valid
    }
    private inline fun measureTimeS(block: () -> Square): Square {
        val startTime = System.currentTimeMillis()
        val s = block()
        val t = (System.currentTimeMillis()-startTime)/1000
        println(s)
        println("Block was generating for $t seconds")
        adjustN(t)
        return s
    }

    private fun adjustN(time: Long) {
        if (time < 15) {
            App.Zs++
            println("N was increased to ${App.Zs}")
        } else if (time > 45) {
            App.Zs--
            println("N was decreased to ${App.Zs}")
        } else {
            println("N stays the same")
        }
    }

    companion object Properties {
        private var nextId = 1
        fun getId(): Int {
            val i = nextId
            nextId++
            return i
        }
    }
}