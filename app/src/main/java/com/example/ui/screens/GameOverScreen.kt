package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.GameMode
import com.example.domain.GameSummary
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.WrongRed
import com.example.ui.theme.XpCyan

@Composable
fun GameOverScreen(
    summary: GameSummary,
    currentStreak: Int,
    doubleCoinsUsed: Boolean,
    onDoubleCoinsAd: () -> Unit,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    BackHandler(onBack = onBackHome)
    val context = LocalContext.current

    val totalQuestions = (summary.correctCount + summary.wrongCount).coerceAtLeast(1)
    val accuracyPercent = ((summary.correctCount * 100f) / totalQuestions).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Main Score Summary Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GoldCoin.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (summary.isNewPersonalBest) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldCoin.copy(alpha = 0.25f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "★ NEW PERSONAL BEST! ★",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldCoin
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = summary.mode.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = XpCyan
                    )
                    Text(
                        text = "${summary.finalScore}",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        modifier = Modifier.testTag("final_score_value")
                    )
                    Text(
                        text = "POINTS • ${summary.category.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid: Accuracy, Correct, Wrong, Max Combo / Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMiniPill("Accuracy", "$accuracyPercent%", GoldCoin)
                        StatMiniPill("Correct", "${summary.correctCount}", CorrectGreen)
                        StatMiniPill("Wrong", "${summary.wrongCount}", WrongRed)
                        StatMiniPill(
                            label = if (summary.mode == GameMode.SPRINT) "Max Combo" else "Time",
                            value = if (summary.mode == GameMode.SPRINT) "x${summary.maxCombo}" else "${summary.elapsedSeconds}s",
                            color = XpCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // XP & Coins Earned Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+${summary.xpEarned} XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = XpCyan
                        )
                        Text(
                            text = "+${summary.coinsEarned} Coins",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldCoin
                        )
                    }
                }
            }

            // 2. Rewarded Ad Button for "Double Coins"
            if (!doubleCoinsUsed && summary.coinsEarned > 0) {
                Button(
                    onClick = onDoubleCoinsAd,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_double_coins_ad")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Watch Ad to Double Coins (+${summary.coinsEarned})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 3. Daily Challenge Shareable Result Card
            if (summary.mode == GameMode.DAILY_CHALLENGE) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CorrectGreen, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SHAREABLE DAILY CARD • ${summary.dailyDateSeed}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldCoin
                        )
                        Text(
                            text = "Score: ${summary.finalScore} | Time: ${summary.elapsedSeconds}s | 🔥 Streak: $currentStreak",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                shareDailyChallengeCard(
                                    context = context,
                                    dateSeed = summary.dailyDateSeed,
                                    score = summary.finalScore,
                                    correct = summary.correctCount,
                                    timeSec = summary.elapsedSeconds,
                                    streak = currentStreak
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_daily_result_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Daily Result", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Exam Mode Score Report & Weak Topics Diagnostics
            if (summary.mode == GameMode.EXAM_MODE || summary.topicBreakdown.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = GoldCoin,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weak Topics & Topic Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }

                        if (summary.weakTopics.isEmpty()) {
                            Text(
                                text = "Outstanding! You scored 75%+ across all tested topics.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CorrectGreen
                            )
                        } else {
                            Text(
                                text = "Recommended to practice: ${summary.weakTopics.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WrongRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        summary.topicBreakdown.forEach { topic ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = topic.subTopic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${topic.correct}/${topic.total} (${topic.accuracyPercent}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (topic.accuracyPercent >= 75) CorrectGreen else GoldCoin
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { topic.accuracyPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (topic.accuracyPercent >= 75) CorrectGreen else WrongRed,
                                    trackColor = Color.White.copy(alpha = 0.14f)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Newly Unlocked Badges Banner
            if (summary.newlyUnlockedBadges.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CorrectGreen.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CorrectGreen, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = GoldCoin
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Badge Unlocked!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        summary.newlyUnlockedBadges.forEach { badge ->
                            Text(
                                text = "🏅 ${badge.title}: ${badge.description}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.16f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("btn_results_home")
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Home", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (summary.mode != GameMode.DAILY_CHALLENGE) {
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp)
                        .testTag("btn_play_again")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatMiniPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}
