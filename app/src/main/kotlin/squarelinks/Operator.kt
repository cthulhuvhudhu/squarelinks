package squarelinks

class Operator {

    private lateinit var cut: SquareLinks

    // Client should have retry logic
    fun buildFive() {
        cut = SquareLinks()
        repeat(NUM_SQ) {
            cut.newSquare()
        }
        println("Is valid: ${cut.validate()}")
    }

    companion object Properties {
        private const val NUM_SQ = 5
    }
}
