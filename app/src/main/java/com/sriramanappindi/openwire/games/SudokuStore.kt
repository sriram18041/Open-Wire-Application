package com.sriramanappindi.openwire.games

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Local-only save of today's Sudoku progress, so leaving and reopening the
 * app doesn't lose fill-ins. Keyed by epoch day: a new calendar day means a
 * new puzzle, and yesterday's leftover state is simply ignored.
 */
class SudokuStore(context: Context) {
    private val file = File(context.filesDir, "sudoku_progress.json")

    fun load(epochDay: Long): IntArray? {
        return try {
            if (!file.exists()) return null
            val o = JSONObject(file.readText())
            if (o.optLong("epochDay") != epochDay) return null
            val arr = o.getJSONArray("cells")
            IntArray(81) { arr.optInt(it, 0) }
        } catch (_: Exception) {
            null
        }
    }

    fun save(epochDay: Long, cells: IntArray) {
        try {
            val o = JSONObject()
            o.put("epochDay", epochDay)
            o.put("cells", JSONArray(cells.toList()))
            file.writeText(o.toString())
        } catch (_: Exception) {
            // Best-effort; a failed save just means today's fill-ins don't survive a restart.
        }
    }

    companion object {
        /** Used by the Games hub to show a checkmark without opening the full board. */
        fun isSolvedToday(context: Context): Boolean {
            val epochDay = localEpochDay()
            val cells = SudokuStore(context).load(epochDay) ?: return false
            return cells.contentEquals(SudokuGenerator.forDay(epochDay).solution)
        }
    }
}
