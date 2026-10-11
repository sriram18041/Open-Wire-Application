package com.sriramanappindi.openwire.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GkQuizTest {

    @Test
    fun `every question's correct index is within its options`() {
        GkQuiz.ALL.forEach { q ->
            assertTrue("question ${q.id} has too few options", q.options.size >= 2)
            assertTrue(
                "question ${q.id} correctIndex ${q.correctIndex} out of bounds",
                q.correctIndex in q.options.indices
            )
        }
    }

    @Test
    fun `question ids are unique`() {
        val ids = GkQuiz.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `forDay returns the configured number of distinct questions`() {
        val questions = GkQuiz.forDay(777L)
        assertEquals(GkQuiz.QUESTIONS_PER_DAY, questions.size)
        assertEquals(questions.size, questions.map { it.id }.toSet().size)
    }

    @Test
    fun `same day always returns the same questions in the same order`() {
        assertEquals(GkQuiz.forDay(42L), GkQuiz.forDay(42L))
    }
}
