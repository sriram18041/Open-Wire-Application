package com.sriramanappindi.openwire.games

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Local-only record of today's GK quiz: which option (if any) was picked for
 * each of today's questions, so the Games hub can show a score and a
 * checkmark once every question has been answered, and reopening mid-quiz
 * doesn't lose progress.
 */
class GkQuizStore(context: Context) {
    private val file = File(context.filesDir, "gk_quiz_progress.json")

    /** -1 for a question not yet answered; the chosen option index otherwise. */
    fun load(epochDay: Long, questionCount: Int): IntArray {
        return try {
            if (!file.exists()) return IntArray(questionCount) { -1 }
            val o = JSONObject(file.readText())
            if (o.optLong("epochDay") != epochDay) return IntArray(questionCount) { -1 }
            val arr = o.optJSONArray("answers") ?: return IntArray(questionCount) { -1 }
            IntArray(questionCount) { i -> if (i < arr.length()) arr.optInt(i, -1) else -1 }
        } catch (_: Exception) {
            IntArray(questionCount) { -1 }
        }
    }

    fun save(epochDay: Long, answers: IntArray) {
        try {
            val o = JSONObject()
            o.put("epochDay", epochDay)
            o.put("answers", JSONArray(answers.toList()))
            file.writeText(o.toString())
        } catch (_: Exception) {
            // Best-effort; a failed save just means progress doesn't survive a restart.
        }
    }

    companion object {
        fun isSolvedToday(context: Context): Boolean {
            val epochDay = localEpochDay()
            val questions = GkQuiz.forDay(epochDay)
            val answers = GkQuizStore(context).load(epochDay, questions.size)
            return answers.none { it == -1 }
        }

        /** Only meaningful once [isSolvedToday] is true. */
        fun scoreToday(context: Context): Int {
            val epochDay = localEpochDay()
            val questions = GkQuiz.forDay(epochDay)
            val answers = GkQuizStore(context).load(epochDay, questions.size)
            return questions.indices.count { answers[it] == questions[it].correctIndex }
        }
    }
}
