package squarelinks.model

import java.security.MessageDigest

internal data class Square(
    val magic: Int,
    val pHash: String,
    val data: List<ByteArray>) {

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
            appendLine("\tMessage: ${String(data[0])}")
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
