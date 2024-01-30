package squarelinks

import kotlin.concurrent.thread
import kotlin.properties.Delegates
import kotlin.random.Random


class Operator {

    private val cut = SquareLinks()
    private var startTime by Delegates.notNull<Long>()

    fun mining() {
        startTime = System.currentTimeMillis()
        while (SquareLinks.getId().toInt() <= 5) {
            threadPool()
        }

        println("Is valid SquareLinks: ${cut.validate()}")
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
        val result = cut.offerSquare(Random.nextInt())
        if (result != null) {
            println("Miner $name")
            println(result)
            val seconds = (System.currentTimeMillis()-startTime)/1000
            cut.adjustN(seconds)
            startTime = System.currentTimeMillis()
        }
    }
}
