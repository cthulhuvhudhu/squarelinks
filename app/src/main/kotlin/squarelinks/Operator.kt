package squarelinks

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import squarelinks.model.SquareLinks
import squarelinks.service.MsgManager
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import kotlin.random.Random

class Operator : KoinComponent {

    private val msgMan : MsgManager by inject()
    private val sqLinks : SquareLinks by inject()

    private var startTime by Delegates.notNull<Long>()

    fun mining() {
        startTime = System.currentTimeMillis()
        while (SquareLinks.getId().toInt() <= 5) {
            threadPool()
        }
        println("Is valid SquareLinks: ${sqLinks.validate()}")
    }

    private fun threadPool() {
        val threads = mutableListOf<Thread>()
        for (i in 1..100) {
            val name = "$i"
            threads.add(thread(start = true, name = name, block = { exec(name) }))
        }
        threads.forEach { it.join() }
    }

    private fun exec(name: String) = run {
        val seconds = (System.currentTimeMillis() - startTime) / 1000
        val rInt = Random.nextInt()
        val result = sqLinks.offerSquare(rInt, msgMan.buildData())
        if (result != null) {
            println("Block:\nCreated by miner # $name\n${result}")
            sqLinks.adjustN(seconds)
            startTime = System.currentTimeMillis()
        }
    }
}
