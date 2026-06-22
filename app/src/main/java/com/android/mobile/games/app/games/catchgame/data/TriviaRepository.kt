package com.android.mobile.games.app.games.catchgame.data

import com.android.mobile.games.app.games.catchgame.model.TriviaQuestion
import kotlin.random.Random

class TriviaRepository(
    private val questions: List<TriviaQuestion>
) {

    private val usedQuestionIds = mutableSetOf<Int>()

    fun resetSession() {
        usedQuestionIds.clear()
    }

    fun getRandomQuestion(): TriviaQuestion {
        val availableQuestions = questions.filterNot { question ->
            question.id in usedQuestionIds
        }

        val selectedQuestion = if (availableQuestions.isNotEmpty()) {
            availableQuestions.random(Random(System.currentTimeMillis()))
        } else {
            usedQuestionIds.clear()
            questions.random(Random(System.currentTimeMillis()))
        }

        usedQuestionIds += selectedQuestion.id
        return selectedQuestion
    }
}
