package squarelinks.service

import squarelinks.App.Companion.ALGORITHM
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class KeyService @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class) constructor(keyLength: Int) {

    private var keyGen: KeyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
    private val pair: KeyPair

    init {
        keyGen.initialize(keyLength)
        pair = keyGen.generateKeyPair()
    }

    fun getPrivateKey(): PrivateKey {
        return pair.private
    }

    fun getPublicKey(): PublicKey {
        return pair.public
    }

    private fun readPrivateKey(filename: String?): PrivateKey {
        val path = filename ?: "$DEFAULT_DIR$PRIVKEY_NAME"
        val keyBytes = Files.readAllBytes(File(path).toPath())
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance(ALGORITHM)
        return kf.generatePrivate(spec)
    }

    private fun readPublicKey(filename: String?): PublicKey {
        val path = filename ?: "$DEFAULT_DIR$PUBKEY_NAME"
        val keyBytes = Files.readAllBytes(File(path).toPath())
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance(ALGORITHM)
        return kf.generatePublic(spec)
    }

    @Throws(IOException::class)
    private fun writeKeys(path: String, key: ByteArray) {
        val f = File(path)
        if (f.exists()) return

        f.parentFile.mkdirs()
        val fos = FileOutputStream(f)
        fos.write(key)
        fos.flush()
        fos.close()
    }

    fun writeToDir(dir: String?) {
        val parent = dir ?: DEFAULT_DIR
        writeKeys("${parent}${PUBKEY_NAME}", pair.public.encoded)
        writeKeys("${parent}${PRIVKEY_NAME}", pair.private.encoded)
    }

    companion object {
        const val DEFAULT_DIR = "KeyPair/"
        const val PUBKEY_NAME = "key.pub"
        const val PRIVKEY_NAME = "key"
    }
}