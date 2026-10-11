package com.sriramanappindi.openwire.games

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Local, per-game day-streak tracking (Wordle-style): a streak counts
 * consecutive calendar days that game was solved. One file per game, since
 * the three daily games are otherwise completely independent of each other.
 */
class GameStreakStore(context: Context, gameKey: String) {
    private val file = File(context.filesDir, "streak_$gameKey.json")

    private data class Streak(val lastSolvedEpochDay: Long, val count: Int)

    private fun read(): Streak {
        return try {
            if (!file.exists()) return Streak(-1, 0)
            val o = JSONObject(file.readText())
            Streak(o.optLong("lastSolvedEpochDay", -1), o.optInt("count", 0))
        } catch (_: Exception) {
            Streak(-1, 0)
        }
    }

    private fun write(streak: Streak) {
        try {
            val o = JSONObject()
            o.put("lastSolvedEpochDay", streak.lastSolvedEpochDay)
            o.put("count", streak.count)
            file.writeText(o.toString())
        } catch (_: Exception) {
            // Best-effort; a failed save just means the streak doesn't survive a restart.
        }
    }

    /**
     * Call once a puzzle becomes solved. Safe to call again for the same
     * day (e.g. on every recomposition while solved, or reopening the
     * screen later the same day) — it's a no-op once today is recorded.
     */
    fun recordSolved(epochDay: Long) {
        val current = read()
        if (current.lastSolvedEpochDay == epochDay) return
        val newCount = if (current.lastSolvedEpochDay == epochDay - 1) current.count + 1 else 1
        write(Streak(epochDay, newCount))
    }

    /** The streak to show right now — 0 once a day's been skipped, until solved again. */
    fun currentStreak(epochDay: Long): Int {
        val streak = read()
        return if (streak.lastSolvedEpochDay >= epochDay - 1) streak.count else 0
    }

    companion object {
        const val SUDOKU = "sudoku"
        const val CHESS = "chess"
        const val GK_QUIZ = "gk_quiz"
    }
}
