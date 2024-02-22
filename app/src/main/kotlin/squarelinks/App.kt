package squarelinks

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import mu.KotlinLogging
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import squarelinks.App.Companion.FREEZE_N
import squarelinks.App.Companion.NUM_QA_SQ
import squarelinks.App.Companion.NUM_Zs
import squarelinks.App.Companion.N_CAP
import squarelinks.App.Companion.NUM_MINERS
import squarelinks.model.SquareLinks
import squarelinks.model.Strategy
import squarelinks.service.CryptoService
import squarelinks.service.KeyService
import squarelinks.service.MsgManager
import squarelinks.service.SignatureService
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class App {

    companion object {
        lateinit var NUM_Zs: AtomicInt
        const val ALGORITHM = "RSA"
        var NUM_QA_SQ by Delegates.notNull<Int>()
        var NUM_MINERS by Delegates.notNull<Int>()
        var N_CAP by Delegates.notNull<Int>()
        var FREEZE_N: Boolean = false
    }
}

// TODO convert to annotations
val appModule = module {
    single<KeyService> { KeyService(1024) }
    singleOf(::CryptoService)
    singleOf(::MsgManager)
    singleOf(::Operator)
    singleOf(::SignatureService)
    singleOf(::SquareLinks)
//    singleOf(::TransactionGenerator)
}

// TODO Better way to do this, with get Logger
private val log = KotlinLogging.logger {}

fun main(args: Array<String>) {
    startKoin {
        modules(appModule)
    }
    val parser = ArgParser("squarelinks")
    val numSquares by parser.option(ArgType.Int, shortName = "sq", description = "Number of square to output")
        .default(7)
    val threads by parser.option(ArgType.Int, fullName = "threads", shortName = "t", description = "Number of threads")
        .default(50)
    val freezeN by parser.option(
        ArgType.Boolean,
        shortName = "f",
        description = "Freeze N to skip adjustments based on time to completion"
    ).default(false)
    val capN by parser.option(ArgType.Int, shortName = "cap", description = "Sets a cap of maximum value for N")
        .default(5)
    val verbose by parser.option(ArgType.Boolean, shortName = "v", description = "Turn on debug logging")
        .default(false)
    val startZ by parser.option(ArgType.Int, shortName = "z", description = "Sets initial value for N")
        .default(2)
    val reps by parser.option(ArgType.Int, shortName = "r", description = "How many times to repeat the runs")
        .default(1)
    val strategies by parser.option(
        ArgType.String,
        shortName = "s",
        description = "Which strategies to run of: thread(t), coroutine(c), synchronous(s)"
    ).multiple().default(listOf(Strategy.THREAD.title, Strategy.COROUTINE.title))
    parser.parse(args)

    if (verbose) {
        System.setProperty("logging.level.squarelinks", "DEBUG")
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "TRACE")
    } else {
        System.setProperty("logging.level.squarelinks", "INFO")
        System.setProperty("org.slf4j.Logger.defaultLogLevel", "INFO")
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO")
    }

    NUM_QA_SQ = numSquares
    NUM_MINERS = threads
    FREEZE_N = freezeN
    N_CAP = if (freezeN) {
        startZ
    } else {
        capN
    }
    NUM_Zs = atomic(startZ)
    val runStrategies = lookupStrategies(strategies)

    // Needed for coroutine custom naming
    System.setProperty("kotlinx.coroutines.debug", "on")

    log.info { "Starting test(s)... " }
    log.info { "Make $numSquares [_] x$reps with N: [$startZ${if (freezeN) "!" else "..$capN"}] ||$threads|| using ${runStrategies.map { it.title }}" }

    val startTime = System.currentTimeMillis()
    Operator().execute(runStrategies, reps)
    log.info { "Completed all testing in ${(System.currentTimeMillis() - startTime) / 1000} s" }
}

private fun lookupStrategies(strategies: List<String>): Array<Strategy> {
    try {
        return strategies.map { Strategy.lookup(it) }.toTypedArray()
    } catch (e: IllegalArgumentException) {
        log.error("Invalid value for strategies", e)
        exitProcess(1)
    }
}