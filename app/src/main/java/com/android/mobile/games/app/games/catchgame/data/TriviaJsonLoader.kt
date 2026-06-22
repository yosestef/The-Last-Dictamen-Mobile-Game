package com.android.mobile.games.app.games.catchgame.data

import android.content.Context
import com.android.mobile.games.app.games.catchgame.model.TriviaCategory
import com.android.mobile.games.app.games.catchgame.model.TriviaQuestion
import org.json.JSONArray

object TriviaJsonLoader {

    fun loadQuestions(context: Context): List<TriviaQuestion> {
        val jsonString = context.assets
            .open("trivia_questions.json")
            .bufferedReader()
            .use { reader -> reader.readText() }

        val jsonArray = JSONArray(jsonString)

        return buildList {
            for (index in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(index)
                val optionsArray = jsonObject.getJSONArray("options")
                val options = buildList {
                    for (optionIndex in 0 until optionsArray.length()) {
                        add(optionsArray.getString(optionIndex))
                    }
                }

                add(
                    TriviaQuestion(
                        id = jsonObject.getInt("id"),
                        category = TriviaCategory.fromValue(
                            value = jsonObject.getString("category")
                        ),
                        question = jsonObject.getString("question"),
                        options = options,
                        correctAnswerIndex = jsonObject.getInt("correctAnswerIndex")
                    )
                )
            }
        }
    }
}
