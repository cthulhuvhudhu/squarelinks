package squarelinks.service

import squarelinks.App.Companion.ALGORITHM
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class CryptoService {

    private var cipher: Cipher = Cipher.getInstance(ALGORITHM)

    fun encrypt(msg: String, privateKey: PrivateKey): String {
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        return Base64.encodeToByteArray(cipher.doFinal(msg.toByteArray())).toString()
    }

    fun decrypt(msg: String, publicKey: PublicKey): String {
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        return cipher.doFinal(Base64.decode(msg.toByteArray())).toString()
    }
}