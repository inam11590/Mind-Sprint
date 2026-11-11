package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.viewmodel.ActiveGameState

/**
 * Delegates directly to [QuizScreen] so all callers render the unified QuizScreen UI.
 */
@Composable
fun GameplayScreen(
    gameState: ActiveGameState,
    onSelectAnswer: (Int) -> Unit,
    onRequestExtraLife: () -> Unit,
    onDeclineExtraLife: () -> Unit,
    onQuitGame: () -> Unit
) {
    QuizScreen(
        gameState = gameState,
        onSelectAnswer = onSelectAnswer,
        onRequestExtraLife = onRequestExtraLife,
        onDeclineExtraLife = onDeclineExtraLife,
        onQuitGame = onQuitGame
    )
}
