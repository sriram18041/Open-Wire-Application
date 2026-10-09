package com.sriramanappindi.openwire.games

/**
 * One daily chess puzzle: a starting position plus its forced solution.
 * [solutionUci] is the full move sequence in UCI notation (e.g. "h6f7") —
 * every puzzle here is a mate-in-1, so it's always a single move, but the
 * list shape leaves room for a multi-move puzzle later without a data
 * model change.
 *
 * Every position below was generated and checkmate-verified offline with a
 * real chess engine (the python-chess library) before being copied in here
 * — not hand-derived from memory — specifically because a wrong FEN or
 * move in a shipped puzzle is a dead end for whoever plays it with no way
 * to recover. See the generation script referenced in the commit for how
 * these were produced if the set ever needs expanding.
 */
data class ChessPuzzle(
    val id: String,
    val fen: String,
    val solutionUci: List<String>,
    val theme: String,
    val hint: String,
    val rating: Int
)

object ChessPuzzles {
    val ALL: List<ChessPuzzle> = listOf(
    ChessPuzzle(
        id = "back-rank-01",
        fen = "k7/pp6/8/8/8/8/7K/2R5 w - - 0 1",
        solutionUci = listOf("c1c8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 500
    ),
    ChessPuzzle(
        id = "back-rank-02",
        fen = "1k6/ppp5/8/8/8/8/K7/4Q3 w - - 0 1",
        solutionUci = listOf("e1e8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 510
    ),
    ChessPuzzle(
        id = "back-rank-03",
        fen = "2k5/1ppp4/8/8/8/8/K7/5R2 w - - 0 1",
        solutionUci = listOf("f1f8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 520
    ),
    ChessPuzzle(
        id = "back-rank-04",
        fen = "3k4/2ppp3/8/8/8/8/K7/6Q1 w - - 0 1",
        solutionUci = listOf("g1g8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 530
    ),
    ChessPuzzle(
        id = "back-rank-05",
        fen = "4k3/3ppp2/8/8/8/8/7K/1R6 w - - 0 1",
        solutionUci = listOf("b1b8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 540
    ),
    ChessPuzzle(
        id = "back-rank-06",
        fen = "5k2/4ppp1/8/8/8/8/K7/7Q w - - 0 1",
        solutionUci = listOf("h1h8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 550
    ),
    ChessPuzzle(
        id = "back-rank-07",
        fen = "6k1/5ppp/8/8/8/8/7K/R7 w - - 0 1",
        solutionUci = listOf("a1a8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 560
    ),
    ChessPuzzle(
        id = "back-rank-08",
        fen = "7k/6pp/8/8/8/8/7K/3Q4 w - - 0 1",
        solutionUci = listOf("d1d8"),
        theme = "back-rank",
        hint = "The king has nowhere to run on the back rank.",
        rating = 570
    ),
    ChessPuzzle(
        id = "queen-mate-09",
        fen = "k7/8/K7/8/8/8/8/Q7 w - - 0 1",
        solutionUci = listOf("a1h8"),
        theme = "queen-mate",
        hint = "Your king shields the queen from capture.",
        rating = 520
    ),
    ChessPuzzle(
        id = "queen-mate-10",
        fen = "7k/8/7K/8/8/8/8/7Q w - - 0 1",
        solutionUci = listOf("h1a8"),
        theme = "queen-mate",
        hint = "Your king shields the queen from capture.",
        rating = 520
    ),
    ChessPuzzle(
        id = "queen-mate-11",
        fen = "Q7/8/8/8/8/K7/8/k7 w - - 0 1",
        solutionUci = listOf("a8h1"),
        theme = "queen-mate",
        hint = "Your king shields the queen from capture.",
        rating = 520
    ),
    ChessPuzzle(
        id = "queen-mate-12",
        fen = "7Q/8/8/8/8/7K/8/7k w - - 0 1",
        solutionUci = listOf("h8a1"),
        theme = "queen-mate",
        hint = "Your king shields the queen from capture.",
        rating = 520
    ),
    ChessPuzzle(
        id = "smothered-mate-13",
        fen = "6rk/6pp/7N/8/4K3/8/8/8 w - - 0 1",
        solutionUci = listOf("h6f7"),
        theme = "smothered-mate",
        hint = "Its own pieces box the king in — the knight can't be captured or blocked.",
        rating = 700
    ),
    ChessPuzzle(
        id = "smothered-mate-14",
        fen = "kr6/pp6/8/1N6/3K4/8/8/8 w - - 0 1",
        solutionUci = listOf("b5c7"),
        theme = "smothered-mate",
        hint = "Its own pieces box the king in — the knight can't be captured or blocked.",
        rating = 700
    ),
    ChessPuzzle(
        id = "smothered-mate-15",
        fen = "8/8/8/4K3/6N1/8/6pp/6rk w - - 0 1",
        solutionUci = listOf("g4f2"),
        theme = "smothered-mate",
        hint = "Its own pieces box the king in — the knight can't be captured or blocked.",
        rating = 700
    ),
    ChessPuzzle(
        id = "smothered-mate-16",
        fen = "8/8/8/3K4/1N6/8/pp6/kr6 w - - 0 1",
        solutionUci = listOf("b4c2"),
        theme = "smothered-mate",
        hint = "Its own pieces box the king in — the knight can't be captured or blocked.",
        rating = 700
    ),
    )

    /** Same puzzle for everyone on a given day, like the other daily games. */
    fun forDay(epochDay: Long): ChessPuzzle = ALL[(epochDay % ALL.size).toInt().let { if (it < 0) it + ALL.size else it }]
}
