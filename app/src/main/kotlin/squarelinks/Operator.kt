package squarelinks

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlinx.atomicfu.update
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import squarelinks.model.SquareLinks
import squarelinks.service.MsgManager
import squarelinks.App.Companion.NUM_QA_SQ
import squarelinks.App.Companion.NUM_MINERS
import squarelinks.App.Companion.NUM_Zs
import squarelinks.model.Result
import squarelinks.model.Strategy

class Operator : KoinComponent {

    private val msgMan: MsgManager by inject()
    private val sqLinks: SquareLinks by inject()

    private val log = KotlinLogging.logger {}

    private var sqGenStartTime = System.currentTimeMillis()
    private var histogram = Array<List<Double>>(9) { emptyList() }
    private var results = mutableListOf<Result>()
    private var initNumZs = App.NUM_Zs.value

    fun execute(strategies: Array<Strategy>, reps: Int) {
        strategies.forEach { s ->
            log.debug { "Running ${s.title} strategy" }
            val executable = when (s) {
                Strategy.SINGLE -> ::straightMining
                Strategy.THREAD -> ::threadMining
                Strategy.COROUTINE -> ::coMining
            }
            repeat(reps) {
                log.debug { "Starting rep ${it + 1} ..." }
                val runTimeS = measureTimeS(executable)
                log.info { "Completed ${s.title} run in $runTimeS s" }
                finish(s, runTimeS)
                log.debug { "... completed rep ${it + 1}" }
            }
            log.debug { "Completed ${s.title} strategy" }
        }
        printResults()
    }

    private fun finish(strategy: Strategy, runTimeS: Double) {
        val isValid = sqLinks.validate()
        val result = Result(strategy, runTimeS, histogram, isValid)
        log.info { result }
        results.add(result)
        resetPerStrategy()
    }

    private inline fun measureTimeS(block: () -> Unit): Double {
        val startTime = System.currentTimeMillis()
        block()
        return ((System.currentTimeMillis() - startTime) / 1000).toDouble()
    }

    private fun straightMining() {
        while (SquareLinks.getId() <= NUM_QA_SQ) {
            mine()
        }
    }

    private fun threadMining() {
        val executor =
            Executors.newFixedThreadPool(NUM_MINERS, ThreadFactoryBuilder().setNameFormat("miner#%d").build())
        while (SquareLinks.getId() <= NUM_QA_SQ) {
            repeat(NUM_MINERS) {
                executor.submit(thread(start = true, block = { mine() }))
            }
        }
        executor.shutdown()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun coMining() = runBlocking {
        while (SquareLinks.getId() <= NUM_QA_SQ) {
            val miners = List(NUM_MINERS) {
                GlobalScope.launch(CoroutineName("miner")) {
                    mine()
                }
            }
            miners.forEach { it.join() }
        }
    }

    private fun mine() = run {
        val name = "miner${Random.nextInt(NUM_MINERS) + 1}"
        Thread.currentThread().name = name
        val seconds = ((System.currentTimeMillis() - sqGenStartTime) / 1000).toDouble()
        val rUInt = Random.nextUInt()
        synchronized(this) {
            val isSuccess = sqLinks.offerSquare(rUInt, name)
            if (isSuccess) {
                sqReset(seconds)
                sqLinks.evaluateRuntime(seconds.toInt())
            }
        }
        if (Random.nextInt(NUM_Zs.value.scale()) == 0 && !sqLinks.isEmpty()) {
            sqLinks.submitMessage(msgMan.buildTxData())
        }
    }

    private fun Int.scale(): Int {
        return this.toDouble().pow(this * 1.5).toInt()
    }

    private fun resetPerStrategy() {
        sqLinks.reset()
        histogram = Array(9) { emptyList() }
        NUM_Zs.update { initNumZs }
    }

    private fun sqReset(timeS: Double) {
        val index = NUM_Zs.value
        val list = histogram.getOrElse(index) { emptyList() }
        histogram[index] = list.plus(timeS)
        sqGenStartTime = System.currentTimeMillis()
    }

    private fun getHistogram(results: List<Result>): Array<List<Double>> {
        var runningHistro: Array<List<Double>> = emptyArray()
        results.forEachIndexed { index, r ->
            if (index == 0) {
                runningHistro = r.distro
            } else {
                runningHistro.forEachIndexed { i, list ->
                    runningHistro[i] = list + r.distro[i]
                }
            }
        }
        return runningHistro
    }

    private fun printResults() {
        Strategy.entries.toTypedArray().forEach { s ->
            val sResults = results.filter { it.strategy == s }
            val runs = sResults.size
            if (runs > 0) {
                if (sResults.any { !it.isValid }) {
                    log.error { "Contains invalid result!" }
                }
                val distro = getHistogram(sResults)
                val blocks = distro.sumOf { it.size }
                log.info { "Results for [$s]: Runs: $runs Blocks: $blocks" }
                println(Result.distroToString(distro))

                val avg = sResults.map { it.runTimeS }.average()
                val stdev = sqrt(sResults.map { (it.runTimeS - avg).pow(2) }.average())
                log.info { "Average Runtime: $avg stdev: $stdev" }
            }
        }
    }
}
