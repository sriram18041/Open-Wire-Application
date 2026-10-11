package com.sriramanappindi.openwire.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGeneratorTest {

    @Test
    fun `same day always produces the same puzzle`() {
        val a = SudokuGenerator.forDay(20_000L)
        val b = SudokuGenerator.forDay(20_000L)
        assertEquals(a.solution.toList(), b.solution.toList())
        assertEquals(a.givens.toList(), b.givens.toList())
    }

    @Test
    fun `different days usually produce different puzzles`() {
        val a = SudokuGenerator.forDay(20_000L)
        val b = SudokuGenerator.forDay(20_001L)
        assertTrue(!a.solution.contentEquals(b.solution))
    }

    @Test
    fun `solution is a valid completed sudoku grid`() {
        repeat(5) { offset ->
            val solution = SudokuGenerator.forDay(100L + offset).solution
            assertEquals(81, solution.size)

            // Every row, column and 3x3 box contains each digit 1-9 exactly once.
            for (row in 0 until 9) {
                val line = (0 until 9).map { col -> solution[row * 9 + col] }
                assertEquals((1..9).toSet(), line.toSet())
            }
            for (col in 0 until 9) {
                val line = (0 until 9).map { row -> solution[row * 9 + col] }
                assertEquals((1..9).toSet(), line.toSet())
            }
            for (boxRow in 0 until 3) {
                for (boxCol in 0 until 3) {
                    val box = (0 until 3).flatMap { r ->
                        (0 until 3).map { c -> solution[(boxRow * 3 + r) * 9 + (boxCol * 3 + c)] }
                    }
                    assertEquals((1..9).toSet(), box.toSet())
                }
            }
        }
    }

    @Test
    fun `givens are a consistent subset of the solution`() {
        val puzzle = SudokuGenerator.forDay(555L)
        assertEquals(81, puzzle.givens.size)
        val filledCount = puzzle.givens.count { it != 0 }
        assertEquals(34, filledCount) // GIVEN_COUNT

        for (i in 0 until 81) {
            if (puzzle.givens[i] != 0) {
                assertEquals(puzzle.solution[i], puzzle.givens[i])
            }
        }
    }
}
