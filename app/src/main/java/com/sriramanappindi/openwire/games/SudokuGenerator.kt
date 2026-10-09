package com.sriramanappindi.openwire.games

import kotlin.random.Random

/**
 * A daily Sudoku puzzle. Both arrays are row-major, length 81, values 1-9
 * ([givens] uses 0 for an empty/fillable cell).
 */
data class SudokuPuzzle(val givens: IntArray, val solution: IntArray)

/**
 * Generates "the daily Sudoku" the same way Wordle generates "the daily
 * word": everyone who opens the app on the same calendar day gets the same
 * puzzle, because it's derived from a seed built out of the date. Fully
 * offline, no puzzle bank, no backend.
 */
object SudokuGenerator {
    private const val GIVEN_COUNT = 34 // casual difficulty: roughly a third of cells filled in

    fun forDay(epochDay: Long): SudokuPuzzle {
        val rng = Random(epochDay)
        val solution = buildSolvedGrid(rng)
        val givens = digGivens(solution, rng)
        return SudokuPuzzle(givens, solution)
    }

    /**
     * Starts from a known-valid solved grid and applies a sequence of
     * transformations that are guaranteed to preserve validity (digit
     * relabeling, permuting rows/columns within their band of three,
     * permuting the bands themselves, and an optional transpose). Far
     * cheaper than generating a random grid via backtracking, and still
     * gives a different-looking grid for every seed.
     */
    private fun buildSolvedGrid(rng: Random): IntArray {
        var grid = IntArray(81) { i ->
            val row = i / 9
            val col = i % 9
            ((row * 3 + row / 3 + col) % 9) + 1
        }

        val digitMap = (1..9).shuffled(rng)
        grid = IntArray(81) { digitMap[grid[it] - 1] }

        grid = permuteLines(grid, rng, isRow = true)
        grid = permuteLines(grid, rng, isRow = false)

        if (rng.nextBoolean()) {
            grid = IntArray(81) { i -> grid[(i % 9) * 9 + (i / 9)] }
        }

        return grid
    }

    /** Shuffles the three bands (of rows or columns) and, within each band, the three lines inside it. */
    private fun permuteLines(grid: IntArray, rng: Random, isRow: Boolean): IntArray {
        val bandOrder = (0..2).shuffled(rng)
        val withinBandOrders = List(3) { (0..2).shuffled(rng) }
        val newOrder = bandOrder.mapIndexed { position, band ->
            withinBandOrders[position].map { band * 3 + it }
        }.flatten()

        val result = IntArray(81)
        for (newLine in 0 until 9) {
            val oldLine = newOrder[newLine]
            for (other in 0 until 9) {
                val newIndex = if (isRow) newLine * 9 + other else other * 9 + newLine
                val oldIndex = if (isRow) oldLine * 9 + other else other * 9 + oldLine
                result[newIndex] = grid[oldIndex]
            }
        }
        return result
    }

    /**
     * Blanks out cells to turn the solved grid into a puzzle. This doesn't
     * verify the remaining givens still pin down a *unique* solution —
     * a full uniqueness-preserving digger is considerably more expensive —
     * which is a fine trade-off for a casual daily puzzle but worth knowing
     * if this ever needs a "hard, no-ambiguity" mode later.
     */
    private fun digGivens(solution: IntArray, rng: Random): IntArray {
        val givens = solution.copyOf()
        (0 until 81).shuffled(rng).take(81 - GIVEN_COUNT).forEach { givens[it] = 0 }
        return givens
    }
}
