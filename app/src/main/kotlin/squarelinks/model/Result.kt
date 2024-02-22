package squarelinks.model

import kotlin.math.pow
import kotlin.math.sqrt

class Result(
    internal val strategy: Strategy,
    internal val runTimeS: Double,
    internal val distro: Array<List<Double>>,
    internal val isValid: Boolean
) {

    override fun toString(): String {
        return buildString {
            appendLine("Run Time [${strategy.title}]: $runTimeS s")
            appendLine("Performance: ")
            appendLine(distroToString(distro))
            appendLine("isValid: $isValid")
        }
    }

    companion object {
        internal fun distroToString(distro: Array<List<Double>>): String {
            return buildString {
                appendLine("NUM_Zs | Avg\t| Num\t| StDev")
                distro.forEachIndexed { index, e ->
                    val mean = e.average()
                    val stdev = sqrt(e.map { (it - mean).pow(2) }.average())
                    appendLine("$index | %.2f\t| ${e.size}\t| %.2f".format(mean, stdev))
                }
            }
        }
    }
}

enum class Strategy(val title: String, val short: Char) {
    SINGLE("synchronous", 's'),
    THREAD("thread", 't'),
    COROUTINE("coroutine", 'c');

    companion object {
        fun lookup(s: String): Strategy {
            return when (s) {
                "synchronous", 's'.toString() -> SINGLE
                "thread", 't'.toString() -> THREAD
                "coroutine", 'c'.toString() -> COROUTINE
                else -> {
                    valueOf(s.uppercase())
                }
            }
        }
    }
}