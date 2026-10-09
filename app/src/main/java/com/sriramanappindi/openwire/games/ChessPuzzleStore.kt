package com.sriramanappindi.openwire.games

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Local-only record of whether today's chess puzzle has been solved yet,
 * so the Games hub can show a checkmark and reopening mid-puzzle doesn't
 * lose progress on how many of the solution's moves have already landed.
 */
class ChessPuzzleStore(context: Context) {
    private val file = File(context.filesDir, "chess_puzzle_progress.json")

    /** How many of the solution's moves the player has already played correctly today. */
    fun load(epochDay: Long): Int {
        return try {
            if (!file.exists()) return 0
            val o = JSONObject(file.readText())
            if (o.optLong("epochDay") != epochDay) return 0
            o.optInt("movesPlayed", 0)
        } catch (_: Exception) {
            0
        }
    }

    fun save(epochDay: Long, movesPlayed: Int) {
        try {
            val o = JSONObject()
            o.put("epochDay", epochDay)
            o.put("movesPlayed", movesPlayed)
            file.writeText(o.toString())
        } catch (_: Exception) {
            // Best-effort; a failed save just means progress doesn't survive a restart.
        }
    }

    companion object {
        fun isSolvedToday(context: Context): Boolean {
            val epochDay = localEpochDay()
            val puzzle = ChessPuzzles.forDay(epochDay)
            return ChessPuzzleStore(context).load(epochDay) >= puzzle.solutionUci.size
        }
    }
}
