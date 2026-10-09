package com.sriramanappindi.openwire.games

/**
 * Minimal, pure-Kotlin chess support — just enough to render a position from
 * FEN and apply a known-correct move from a curated puzzle's solution. This
 * is deliberately NOT a rules engine: it doesn't validate legality, check,
 * or checkmate itself. Every puzzle in [ChessPuzzles] was verified offline
 * with a real chess engine (python-chess) before being hand-copied in here,
 * so the app only ever needs to apply moves it already knows are correct —
 * it just has to compare what the player tapped against that known-correct
 * sequence.
 *
 * Board indices run 0-63, index = rank * 8 + file, where rank 0 is rank "1"
 * (White's back rank) and file 0 is the 'a' file — the usual convention.
 * Pieces are single chars: uppercase = white, lowercase = black
 * (K/Q/R/B/N/P), null = empty square.
 */
object ChessBoard {

    fun parseFen(fen: String): Array<Char?> {
        val board = arrayOfNulls<Char>(64)
        val placement = fen.trim().split(" ").first().split("/")
        for (fenRow in 0 until 8) {
            val rank = 7 - fenRow // FEN's first row is rank 8
            var file = 0
            for (ch in placement[fenRow]) {
                if (ch.isDigit()) {
                    file += ch.digitToInt()
                } else {
                    board[rank * 8 + file] = ch
                    file++
                }
            }
        }
        return board
    }

    fun sideToMoveIsWhite(fen: String): Boolean =
        fen.trim().split(" ").getOrElse(1) { "w" } == "w"

    fun squareIndex(square: String): Int {
        val file = square[0] - 'a'
        val rank = square[1] - '1'
        return rank * 8 + file
    }

    fun squareName(index: Int): String {
        val file = index % 8
        val rank = index / 8
        return "${('a' + file)}${rank + 1}"
    }

    /** A move in UCI form, e.g. "e2e4" or "e7e8q" for a queen promotion. */
    data class Move(val from: Int, val to: Int, val promotion: Char?)

    fun parseUci(uci: String): Move {
        val from = squareIndex(uci.substring(0, 2))
        val to = squareIndex(uci.substring(2, 4))
        val promotion = uci.getOrNull(4)
        return Move(from, to, promotion)
    }

    /**
     * Applies a move to a board, returning a new board. Handles a normal
     * move/capture and simple promotion. Does NOT handle castling or en
     * passant — no puzzle in [ChessPuzzles] needs either, so this is a
     * known, deliberate gap rather than an oversight.
     */
    fun applyMove(board: Array<Char?>, move: Move): Array<Char?> {
        val next = board.copyOf()
        val piece = next[move.from]
        next[move.from] = null
        next[move.to] = if (move.promotion != null && piece != null) {
            if (piece.isUpperCase()) move.promotion.uppercaseChar() else move.promotion.lowercaseChar()
        } else {
            piece
        }
        return next
    }

    /** Unicode glyph for a piece char, or null for an empty square. */
    fun glyph(piece: Char?): String? = when (piece) {
        'K' -> "♔"; 'Q' -> "♕"; 'R' -> "♖"; 'B' -> "♗"; 'N' -> "♘"; 'P' -> "♙"
        'k' -> "♚"; 'q' -> "♛"; 'r' -> "♜"; 'b' -> "♝"; 'n' -> "♞"; 'p' -> "♟"
        else -> null
    }
}
