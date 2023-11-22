package squarelinks

import java.security.MessageDigest

data class Square(
    internal val id: Int = 1,
    internal val pHash: String = "0") {

    private val createdAt: Long = System.currentTimeMillis()
    internal val hash = applySha256("$id$pHash$createdAt")

    override fun toString(): String {
        return buildString {
            append("Block:\n")
            append("Id: $id\n")
            append("Timestamp: $createdAt\n")
            append("Hash of the previous block:\n")
            append("$pHash\n")
            append("Hash of the block:\n")
            append("$hash\n")
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
    val links = ArrayDeque<Square>()

    fun newSquare() {
        val pHash = links.lastOrNull()?.hash ?: "0"
        val sq = Square(getId(), pHash)
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

    companion object Properties {
        var nextId = 1
        fun getId(): Int {
            nextId++
            return --nextId
        }
    }

}