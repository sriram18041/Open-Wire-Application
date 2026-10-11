package com.sriramanappindi.openwire.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessBoardTest {

    @Test
    fun `parseFen places pieces on the right squares`() {
        val board = ChessBoard.parseFen("k7/pp6/8/8/8/8/7K/2R5 w - - 0 1")
        assertEquals('k', board[ChessBoard.squareIndex("a8")])
        assertEquals('p', board[ChessBoard.squareIndex("a7")])
        assertEquals('p', board[ChessBoard.squareIndex("b7")])
        assertEquals('K', board[ChessBoard.squareIndex("h2")])
        assertEquals('R', board[ChessBoard.squareIndex("c1")])
        assertNull(board[ChessBoard.squareIndex("e4")])
    }

    @Test
    fun `square name and index round-trip for every square`() {
        for (i in 0 until 64) {
            assertEquals(i, ChessBoard.squareIndex(ChessBoard.squareName(i)))
        }
    }

    @Test
    fun `sideToMoveIsWhite reads the FEN's active color field`() {
        assertTrue(ChessBoard.sideToMoveIsWhite("8/8/8/8/8/8/8/8 w - - 0 1"))
        assertTrue(!ChessBoard.sideToMoveIsWhite("8/8/8/8/8/8/8/8 b - - 0 1"))
    }

    @Test
    fun `parseUci reads a plain move and a promotion`() {
        val move = ChessBoard.parseUci("e2e4")
        assertEquals(ChessBoard.squareIndex("e2"), move.from)
        assertEquals(ChessBoard.squareIndex("e4"), move.to)
        assertNull(move.promotion)

        val promo = ChessBoard.parseUci("e7e8q")
        assertEquals('q', promo.promotion)
    }

    @Test
    fun `applyMove relocates the piece and clears the origin square`() {
        val board = ChessBoard.parseFen("k7/8/8/8/8/8/7K/2R5 w - - 0 1")
        val moved = ChessBoard.applyMove(board, ChessBoard.parseUci("c1c8"))
        assertNull(moved[ChessBoard.squareIndex("c1")])
        assertEquals('R', moved[ChessBoard.squareIndex("c8")])
        // The original board instance is untouched (applyMove returns a copy).
        assertEquals('R', board[ChessBoard.squareIndex("c1")])
    }

    @Test
    fun `glyph distinguishes white and black pieces`() {
        assertEquals("♔", ChessBoard.glyph('K'))
        assertEquals("♚", ChessBoard.glyph('k'))
        assertNull(ChessBoard.glyph(null))
    }
}
