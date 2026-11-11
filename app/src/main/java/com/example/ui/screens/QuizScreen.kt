package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.GameMode
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.WrongRed
import com.example.ui.theme.XpCyan
import com.example.viewmodel.ActiveGameState
import com.example.viewmodel.AnswerFeedbackState
import kotlin.math.roundToInt

/**
 * Primary QuizScreen Composable for Mind Sprint.
 * Displays:
 * - A circular countdown timer with smooth arc & linear progress bar animations
 * - The current question card with smooth horizontal slide transitions, category/sub-topic tags,
 *   live dynamic difficulty badge, green flash on correct, and horizontal red shake on wrong
 * - Four big tactile answer buttons (A, B, C, D) in a fixed, zero-scroll portrait layout
 */
@Composable
fun QuizScreen(
    gameState: ActiveGameState,
    onSelectAnswer: (Int) -> Unit,
    onRequestExtraLife: () -> Unit,
    onDeclineExtraLife: () -> Unit,
    onQuitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onQuitGame)

    val question = gameState.currentQuestion ?: return

    // Horizontal shake animation on wrong answer
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(gameState.feedbackState, gameState.questionIndex) {
        if (gameState.feedbackState == AnswerFeedbackState.WRONG_SHAKE) {
            val keyframes = listOf(-22f, 22f, -16f, 16f, -8f, 8f, 0f)
            for (target in keyframes) {
                shakeOffset.animateTo(target, animationSpec = tween(durationMillis = 42))
            }
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    // Target progress fraction for smooth progress animation
    val targetProgress = if (gameState.mode == GameMode.SPRINT) {
        (gameState.timeRemainingSeconds / 60f).coerceIn(0f, 1f)
    } else {
        val total = (gameState.totalQuestionsLimit ?: 10).toFloat()
        (gameState.questionIndex / total).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "QuizTimerSmoothProgress"
    )

    val isLowTime = gameState.mode == GameMode.SPRINT && gameState.timeRemainingSeconds <= 10
    val animatedTimerColor by animateColorAsState(
        targetValue = when {
            gameState.feedbackState == AnswerFeedbackState.CORRECT_FLASH -> CorrectGreen
            gameState.feedbackState == AnswerFeedbackState.WRONG_SHAKE -> WrongRed
            isLowTime -> WrongRed
            gameState.mode == GameMode.SPRINT -> GoldCoin
            else -> XpCyan
        },
        animationSpec = tween(durationMillis = 280),
        label = "QuizTimerColor"
    )

    val flashBorderColor = when (gameState.feedbackState) {
        AnswerFeedbackState.CORRECT_FLASH -> CorrectGreen
        AnswerFeedbackState.WRONG_SHAKE -> WrongRed
        AnswerFeedbackState.NONE -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (flashBorderColor != Color.Transparent) {
                    Modifier
                        .background(flashBorderColor.copy(alpha = 0.14f))
                        .border(4.dp, flashBorderColor)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .testTag("quiz_screen_root")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HUD BAR: Quit Button, Circular Countdown Timer, Combo Multiplier, Live Score & Smooth Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onQuitGame,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .testTag("quit_gameplay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quit Game",
                            tint = Color.White
                        )
                    }

                    // Circular Countdown Timer with Smooth Animated Sweep
                    CircularCountdownTimer(
                        progress = animatedProgress,
                        ringColor = animatedTimerColor,
                        centerPrimaryText = if (gameState.mode == GameMode.SPRINT) {
                            "${gameState.timeRemainingSeconds}s"
                        } else {
                            "${gameState.questionIndex}/${gameState.totalQuestionsLimit ?: 10}"
                        },
                        deltaBadgeText = if (gameState.mode == GameMode.SPRINT) {
                            when (gameState.feedbackState) {
                                AnswerFeedbackState.CORRECT_FLASH -> "+2s"
                                AnswerFeedbackState.WRONG_SHAKE -> "-3s"
                                AnswerFeedbackState.NONE -> null
                            }
                        } else null,
                        deltaBadgeColor = if (gameState.feedbackState == AnswerFeedbackState.CORRECT_FLASH) {
                            CorrectGreen
                        } else {
                            WrongRed
                        }
                    )

                    // Combo Multiplier Pill (x1 up to x5)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (gameState.comboMultiplier >= 3) StreakFlame.copy(alpha = 0.28f)
                                else Color.White.copy(alpha = 0.14f)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (gameState.comboMultiplier >= 3) StreakFlame else Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("combo_multiplier_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Combo Multiplier",
                            tint = GoldCoin,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "x${gameState.comboMultiplier}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    // Live Score Counter
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${gameState.score}",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldCoin,
                            modifier = Modifier.testTag("gameplay_score_text")
                        )
                    }
                }

                // Smooth Animated Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("smooth_timer_progress_bar"),
                    color = animatedTimerColor,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. CURRENT QUESTION CARD (Smooth Horizontal Slide Animation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = question,
                    transitionSpec = {
                        (slideInHorizontally(tween(260)) { fullWidth -> fullWidth } + fadeIn(tween(220)))
                            .togetherWith(
                                slideOutHorizontally(tween(240)) { fullWidth -> -fullWidth } + fadeOut(tween(180))
                            )
                    },
                    label = "QuestionSlideAnimation"
                ) { targetQuestion ->
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = when (gameState.feedbackState) {
                                    AnswerFeedbackState.CORRECT_FLASH -> CorrectGreen
                                    AnswerFeedbackState.WRONG_SHAKE -> WrongRed
                                    AnswerFeedbackState.NONE -> targetQuestion.category.badgeColor.copy(alpha = 0.6f)
                                },
                                shape = RoundedCornerShape(26.dp)
                            )
                            .testTag("quiz_question_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(targetQuestion.category.badgeColor.copy(alpha = 0.22f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${targetQuestion.category.title} • ${targetQuestion.subTopic}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = targetQuestion.category.badgeColor,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = "${gameState.dynamicDifficultyLabel} • #${gameState.questionIndex}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldCoin.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.testTag("dynamic_difficulty_badge")
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = targetQuestion.questionText,
                                    style = if (targetQuestion.questionText.length > 55) {
                                        MaterialTheme.typography.headlineMedium
                                    } else {
                                        MaterialTheme.typography.headlineLarge
                                    },
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("question_prompt_text")
                                )
                            }

                            val statusText = when (gameState.feedbackState) {
                                AnswerFeedbackState.CORRECT_FLASH -> "✓ CORRECT! Combo x${gameState.comboMultiplier}"
                                AnswerFeedbackState.WRONG_SHAKE -> "✗ WRONG! Answer: ${targetQuestion.options[targetQuestion.correctIndex]}"
                                AnswerFeedbackState.NONE -> "Tap the correct option below"
                            }
                            val statusColor = when (gameState.feedbackState) {
                                AnswerFeedbackState.CORRECT_FLASH -> CorrectGreen
                                AnswerFeedbackState.WRONG_SHAKE -> WrongRed
                                AnswerFeedbackState.NONE -> Color.White.copy(alpha = 0.55f)
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelLarge,
                                color = statusColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. FOUR BIG ANSWER BUTTONS (Zero-scroll proportional layout)
            val optionLabels = listOf("A", "B", "C", "D")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.50f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.forEachIndexed { idx, optionText ->
                    val isSelected = gameState.selectedOptionIndex == idx
                    val isCorrectOption = idx == question.correctIndex
                    val showResult = gameState.selectedOptionIndex != null

                    val containerColor = when {
                        showResult && isCorrectOption -> CorrectGreen
                        showResult && isSelected && !isCorrectOption -> WrongRed
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                    }

                    val borderColor = when {
                        showResult && isCorrectOption -> Color.White
                        showResult && isSelected && !isCorrectOption -> WrongRed
                        else -> Color.White.copy(alpha = 0.22f)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                            .clickable(enabled = !showResult) { onSelectAnswer(idx) }
                            .testTag("answer_option_$idx")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLabels.getOrElse(idx) { "${idx + 1}" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Extra Life Prompt Dialog (Once per Sprint game when timer reaches 0)
    if (gameState.showExtraLifePrompt) {
        Dialog(
            onDismissRequest = onDeclineExtraLife,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GoldCoin, RoundedCornerShape(26.dp))
                    .testTag("extra_life_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Extra Life",
                        tint = WrongRed,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Time's Up!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Current Score: ${gameState.score} pts (Combo x${gameState.comboMultiplier})\nUse your 1 Extra Life to get +15 seconds and keep your streak alive?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onRequestExtraLife,
                        colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_watch_ad_extra_life")
                    ) {
                        Text(
                            text = "Watch Ad for Extra Life (+15s)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDeclineExtraLife,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.14f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_decline_extra_life")
                    ) {
                        Text("End Game & Claim Rewards", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Circular Countdown Timer with a smooth animated progress ring and optional +2s / -3s time delta badge.
 */
@Composable
fun CircularCountdownTimer(
    progress: Float,
    ringColor: Color,
    centerPrimaryText: String,
    deltaBadgeText: String? = null,
    deltaBadgeColor: Color = CorrectGreen,
    ringSize: Dp = 56.dp,
    strokeWidth: Dp = 5.5.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.testTag("sprint_timer_pill")
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .testTag("circular_countdown_timer"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                // Background track circle
                drawArc(
                    color = Color.White.copy(alpha = 0.16f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                // Smooth animated foreground progress arc starting from 12 o'clock (-90f)
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
            Text(
                text = centerPrimaryText,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (deltaBadgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(deltaBadgeColor.copy(alpha = 0.24f))
                    .border(1.dp, deltaBadgeColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = deltaBadgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = deltaBadgeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
