package com.android.mobile.games.app.games.codemerge.model

data class CodeMergeGameState(
    val currentScore: Int = 0,
    val highScore: Int = 0,
    val elements: List<CodeElement> = emptyList(),
    val nextLevel: CodeLevel = CodeLevel.NULO,
    val currentElementX: Float = 500f,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isLoading: Boolean = false,
    val playerName: String = "Escarchito"
)

sealed class CodeMergeIntent {
    data object StartGame : CodeMergeIntent()
    data class MoveCurrentElement(val x: Float) : CodeMergeIntent()
    data object DropElement : CodeMergeIntent()
    data object Tick : CodeMergeIntent()
    data class SubmitScore(val name: String) : CodeMergeIntent()
}
