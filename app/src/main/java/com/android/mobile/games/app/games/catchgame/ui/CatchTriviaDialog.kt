package com.android.mobile.games.app.games.catchgame.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.games.catchgame.model.TriviaQuestion
import com.android.mobile.games.app.ui.theme.*

@Composable
fun CatchTriviaDialog(
    question: TriviaQuestion,
    timeLeftSeconds: Int,
    feedbackMessage: String?,
    isAnswerLocked: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = CuteCream,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp,
        title = {
            Text(
                text = "📝 ¡EXAMEN SORPRESA! 😱",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Categoría: ${question.category.displayName} 🏷️",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextDark.copy(alpha = 0.8f)
                )

                Text(
                    text = "⏱ Tiempo restante: ${timeLeftSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeftSeconds <= 3) CutePink else TextDark
                )

                Text(
                    text = question.question,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    fontWeight = FontWeight.Medium
                )

                question.options.forEachIndexed { index, option ->
                    Button(
                        onClick = { onAnswerSelected(index) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAnswerLocked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CuteLavender,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = option, fontWeight = FontWeight.Bold)
                    }
                }

                feedbackMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (message.contains("Correct", true) || message.contains("¡", true)) CuteMint else CutePink,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}
