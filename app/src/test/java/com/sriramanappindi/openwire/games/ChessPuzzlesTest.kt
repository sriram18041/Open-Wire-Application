package com.sriramanappindi.openwire.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessPuzzlesTest {

    @Test
    fun `every puzzle has a non-empty solution and a parseable board`() {
        ChessPuzzles.ALL.forEach { puzzle ->
            assertTrue("puzzle ${puzzle.id} has no solution moves", puzzle.solutionUci.isNotEmpty())
            val board = ChessBoard.parseFen(puzzle.fen)
            assertEquals(64, board.size)
            assertTrue("puzzle ${puzzle.id} isn't White to move", ChessBoard.sideToMoveIsWhite(puzzle.fen))
        }
    }

    @Test
    fun `puzzle ids are unique`() {
        val ids = ChessPuzzles.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `forDay picks a valid puzzle for any day, including negative epoch days`() {
        listOf(-10_000L, -1L, 0L, 1L, 20_000L).forEach { epochDay ->
            val puzzle = ChessPuzzles.forDay(epochDay)
            assertTrue(ChessPuzzles.ALL.contains(puzzle))
        }
    }

    @Test
    fun `same day always picks the same puzzle`() {
        assertEquals(ChessPuzzles.forDay(12_345L), ChessPuzzles.forDay(12_345L))
    }
}
