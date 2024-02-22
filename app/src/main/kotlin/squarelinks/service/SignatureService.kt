package squarelinks.service

import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

class SignatureService {

    fun sign(msg: ByteArray, key: PrivateKey): ByteArray {
        val rsa = Signature.getInstance(ALGORITHM)
        rsa.initSign(key)
        rsa.update(msg)
        return rsa.sign()
    }

    fun verify(msg: ByteArray, signature: ByteArray, key: PublicKey) {
        val sig = Signature.getInstance(ALGORITHM)
        sig.initVerify(key)
        sig.update(msg)
        assert(sig.verify(signature)) { "Unable to validate signature" }
    }

    companion object {
        private const val ALGORITHM = "SHA1withRSA"
    }
}