package squarelinks.service

import com.google.common.primitives.Ints
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import squarelinks.App.Companion.NUM_MINERS
import kotlin.random.Random

class MsgManager : KoinComponent {

    private val keySvc: KeyService by inject()
    private val sigSvc: SignatureService by inject()
//    private val txGen: TransactionGenerator by inject()

    private val triviaList: List<String> = (1..NUM_TRIVIA).map { it.toString() }
        .map { khttp.get("$TRIVIA_URL$it?json").jsonObject.getString("text") }
        .map { it.slice(0..Ints.min(CARRIAGE_RETURN_LEN, it.length - 1)) + "..." }

    fun buildMessageData(): List<ByteArray> {
        val msg = triviaList[Random.nextInt(NUM_TRIVIA)].toByteArray()
        val sign = sigSvc.sign(msg, keySvc.getPrivateKey())
        return listOf(msg, sign)
    }

//    private fun buildTxData(msg: List<Transaction>): List<ByteArray> {
//        val data = Json.encodeToString(msg).toByteArray()
//        val sign = sigSvc.sign(data, keySvc.getPrivateKey())
//        return listOf(data, sign)
//    }

//    fun buildTxData(): List<ByteArray> {
//        return buildTxData(listOf(txGen.unsafeGenerate()))
//    }
    fun validateData(vararg data: List<ByteArray>) {
        data.forEach { sigSvc.verify(it.first(), it.last(), keySvc.getPublicKey()) }
    }

    companion object {
        const val CARRIAGE_RETURN_LEN = 80
        const val TRIVIA_URL = "http://numbersapi.com/"
        val NUM_TRIVIA = NUM_MINERS
    }
}