package squarelinks.service

import squarelinks.App.Companion.ALGORITHM
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import java.util.Base64

class CryptoService {

    private val cipher: Cipher = Cipher.getInstance(ALGORITHM)

    fun encrypt(msg: String, publicKey: PublicKey): String {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(msg.toByteArray())
        val encodedBytes = Base64.getEncoder().encode(encryptedBytes)
        return String(encodedBytes)
    }

    fun decrypt(msg: String, privateKey: PrivateKey): String {
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = cipher.doFinal(msg.toByteArray())
        val encodedBytes = Base64.getDecoder().decode(encryptedBytes)
        return String(encodedBytes)
    }
}