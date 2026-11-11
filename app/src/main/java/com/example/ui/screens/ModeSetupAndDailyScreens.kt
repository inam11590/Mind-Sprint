package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileState
import com.example.domain.ExamDifficulty
import com.example.domain.GameMode
import com.example.domain.QuizCategory
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldCoin

@Composable
fun ModeSetupScreen(
    mode: GameMode,
    selectedCategory: QuizCategory,
    selectedDifficulty: ExamDifficulty,
    bestScores: Map<String, Int>,
    onSelectCategory: (QuizCategory) -> Unit,
    onSelectDifficulty: (ExamDifficulty) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("setup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = if (mode == GameMode.SPRINT) {
                            "Pick a category • 60s clock (+2s right, -3s wrong)"
                        } else {
                            "20 Questions • Untimed • Diagnostic Weak Topics Report"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }

            Text(
                text = "1. Choose Category",
                style = MaterialTheme.typography.titleLarge,
                color = GoldCoin
            )

            QuizCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                val best = bestScores[category.id] ?: 0
                CategoryOptionCard(
                    category = category,
                    bestScore = best,
                    isSelected = isSelected,
                    onClick = { onSelectCategory(category) }
                )
            }

            if (mode == GameMode.EXAM_MODE) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2. Choose Exam Difficulty",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldCoin
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExamDifficulty.entries.forEach { diff ->
                        val isSelected = diff == selectedDifficulty
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldCoin else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectDifficulty(diff) }
                                .testTag("diff_${diff.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = diff.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "XP x${diff.xpMultiplier}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldCoin,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("launch_game_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (mode == GameMode.SPRINT) "START 60s SPRINT" else "BEGIN 20-QUESTION EXAM",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CategoryOptionCard(
    category: QuizCategory,
    bestScore: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (category) {
        QuizCategory.MENTAL_MATH -> Icons.Default.Calculate
        QuizCategory.ENGLISH_VOCAB -> Icons.Default.MenuBook
        QuizCategory.SCIENCE -> Icons.Default.Science
        QuizCategory.LOGIC_PATTERNS -> Icons.Default.Extension
        QuizCategory.MIXED -> Icons.Default.AutoAwesome
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                category.badgeColor.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) category.badgeColor else Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("cat_${category.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(category.badgeColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.title,
                    tint = category.badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp
                )
            }
            if (bestScore > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldCoin,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$bestScore",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DailyChallengeHubScreen(
    todayDateSeed: String,
    profile: PlayerProfileState,
    alreadyPlayedToday: Boolean,
    onStartDaily: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("daily_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "Daily Challenge",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "Seed: $todayDateSeed • Same 10 Questions Worldwide",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldCoin
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldCoin.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (alreadyPlayedToday) Icons.Default.CheckCircle else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (alreadyPlayedToday) CorrectGreen else GoldCoin,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = if (alreadyPlayedToday) {
                        "Today's Challenge Completed!"
                    } else {
                        "Ready for Today's 10-Question Brain Sprint?"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (alreadyPlayedToday) {
                        "Only one official attempt is allowed per day. Share your result card below with friends and come back tomorrow for a brand-new seeded set!"
                    } else {
                        "• 10 curated questions across Math, Vocab, Science & Logic\n• Identical question seed ($todayDateSeed) for all students today\n• Only 1 attempt allowed per day!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                if (alreadyPlayedToday) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CorrectGreen, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MIND SPRINT DAILY • $todayDateSeed",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldCoin
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Score: ${profile.lastDailyScore} pts (${profile.lastDailyCorrect}/10)",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Time: ${profile.lastDailyTimeSec}s • Streak: 🔥 ${profile.currentStreak} Days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CorrectGreen
                            )
                        }
                    }

                    Button(
                        onClick = {
                            shareDailyChallengeCard(
                                context = context,
                                dateSeed = todayDateSeed,
                                score = profile.lastDailyScore,
                                correct = profile.lastDailyCorrect,
                                timeSec = profile.lastDailyTimeSec,
                                streak = profile.currentStreak
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("share_daily_hub_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Result Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (!alreadyPlayedToday) {
            Button(
                onClick = onStartDaily,
                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("start_daily_challenge_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START TODAY'S CHALLENGE",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.16f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Back to Home", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

fun shareDailyChallengeCard(
    context: Context,
    dateSeed: String,
    score: Int,
    correct: Int,
    timeSec: Int,
    streak: Int
) {
    val blocks = (1..10).joinToString("") { idx -> if (idx <= correct) "🟩" else "⬜" }
    val shareText = buildString {
        appendLine("⚡ Mind Sprint Daily Challenge ($dateSeed)")
        appendLine("🏆 Score: $score pts ($correct/10)")
        appendLine("⏱️ Time: ${timeSec}s | 🔥 Streak: $streak Days")
        appendLine(blocks)
        append("Can you beat my brain sprint today?")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    val chooser = Intent.createChooser(sendIntent, "Share Mind Sprint Result").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
