package squarelinks

class Operator {

    private lateinit var cut: SquareLinks

    fun buildFive() {
        cut = SquareLinks()
        repeat(NUM_SQ) {
            println("Block was generating for ${measureTimeS(cut::newSquare)} seconds")
        }
        println("Is valid: ${cut.validate()}")
    }

    private inline fun measureTimeS(block: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        block()
        return (System.currentTimeMillis()-startTime)/1000
    }

    companion object Properties {
        private const val NUM_SQ = 5
    }
}
