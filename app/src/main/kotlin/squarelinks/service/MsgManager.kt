package squarelinks.service

import com.google.common.primitives.Ints
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class MsgManager : KoinComponent {

    private val keySvc : KeyService by inject()
    private val sigSvc : SignatureService by inject()


    fun buildData(): List<ByteArray> {
        val msg = trivia().toByteArray()
        val sign = sigSvc.sign(msg, keySvc.getPrivateKey())
        return listOf(msg, sign)
    }

    fun validateData(data : List<ByteArray>) {
        sigSvc.verify(data[0], data[1], keySvc.getPublicKey())
    }

    private fun trivia(): String {
        val num = Random.nextInt()
        val trivia = khttp.get("$TRIVIA_URL$num?json").jsonObject.getString("text")
        return trivia.slice(0..Ints.min(CARRIAGE_RETURN_LEN, trivia.length - 1))
    }

    companion object {
        const val CARRIAGE_RETURN_LEN = 64
        const val TRIVIA_URL = "http://numbersapi.com/"
    }
}