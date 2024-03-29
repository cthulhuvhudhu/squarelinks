package squarelinks.model

import squarelinks.model.SquareLinks.Properties.REWARD
import java.security.MessageDigest

internal data class Square(
    val magic: UInt,
    val pHash: String,
    internal val creator: String,
) {

    val id: Int = SquareLinks.getId()
    private val createdAt: Long = System.currentTimeMillis()
    internal val hash = applySha256("$id$pHash$createdAt$magic")
    internal var data: List<List<ByteArray>> = emptyList()

    override fun toString(): String {
        return buildString {
            appendLine("Block:")
            appendLine("Created by: $creator")
            appendLine("$creator gets $REWARD VC")
            appendLine("Id: $id")
            appendLine("\tTimestamp: $createdAt")
            appendLine("\tMagic number: $magic")
            appendLine("\tHash of the previous block: \n$pHash")
            appendLine("\tHash of the block: \n$hash")
            val output = if (data.isEmpty()) {
                "No transactions/messages"
            } else {
                "\n${
                    data.joinToString("\n") { e ->
                        Transaction.decode(e.first()).toString()
                    }
                }"
            }
            append("\tBlock data: $output")
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

    internal fun getTransactions(): Array<Transaction> {
        return data.map { e -> e.first() }
            .map { Transaction.decode(it) }
            .toTypedArray()
    }
}
