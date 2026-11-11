package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileState
import com.example.data.StreakDaySlot
import com.example.domain.AvatarPreset
import com.example.domain.BadgeInfo
import com.example.domain.QuizCategory
import com.example.ui.components.AvatarSvgBadge
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.WrongRed
import com.example.ui.theme.XpCyan

@Composable
fun ProgressAndShopScreen(
    profile: PlayerProfileState,
    streakDays: List<StreakDaySlot>,
    onBuyOrSelectTheme: (AppThemePreset) -> Unit,
    onBuyOrSelectAvatar: (AvatarPreset) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profile/Badges", "Leaderboard", "Themes (7)", "Avatars (8)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Coins balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("progress_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Profile, Badges & Shop",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Level ${profile.level} • ${profile.xp} Total XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = XpCyan
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldCoin.copy(alpha = 0.2f))
                    .border(1.dp, GoldCoin, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = GoldCoin,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${profile.coins}",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldCoin
                )
            }
        }

        // Visual 7-Day Streak Calendar (Highlights active days with a Gold Circle)
        SevenDayStreakProgressCard(
            currentStreak = profile.currentStreak,
            longestStreak = profile.longestStreak,
            streakDays = streakDays
        )

        // Tab Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { idx, label ->
                val active = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else Color.White.copy(alpha = 0.12f)
                        )
                        .clickable { selectedTab = idx }
                        .padding(vertical = 10.dp)
                        .testTag("progress_tab_$idx"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                0 -> ProfileAndBadgesSection(profile)
                1 -> LeaderboardAndBestsSection(profile)
                2 -> ThemesShopSection(profile, onBuyOrSelectTheme)
                3 -> AvatarsShopSection(profile, onBuyOrSelectAvatar)
            }
        }
    }
}

/**
 * Visual 7-Day Streak Calendar displayed on the Progress screen.
 * Highlights active days with a glowing Gold Circle and updates from localStorage daily activity.
 */
@Composable
private fun SevenDayStreakProgressCard(
    currentStreak: Int,
    longestStreak: Int,
    streakDays: List<StreakDaySlot>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GoldCoin.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .testTag("progress_seven_day_streak_calendar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "7-Day Streak",
                        tint = GoldCoin,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currentStreak-Day Streak",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldCoin.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Best: $longestStreak Days",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldCoin,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                streakDays.forEach { slot ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.testTag("streak_day_${slot.isoDate}")
                    ) {
                        Text(
                            text = slot.dayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (slot.isToday) GoldCoin else Color.White.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            fontWeight = if (slot.isToday) FontWeight.Bold else FontWeight.Normal
                        )

                        // Gold Circle highlight for active days
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (slot.isActive) {
                                        Brush.radialGradient(
                                            colors = listOf(GoldCoin, StreakFlame)
                                        )
                                    } else {
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.10f),
                                                Color.White.copy(alpha = 0.06f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = if (slot.isActive) 2.dp else 1.dp,
                                    color = if (slot.isActive) GoldCoin else Color.White.copy(alpha = 0.22f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (slot.isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active Day",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = slot.dayOfMonth,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Text(
                            text = if (slot.isActive) "Active" else slot.dayOfMonth,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (slot.isActive) GoldCoin else Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardAndBestsSection(profile: PlayerProfileState) {
    // Personal Best per Category
    Text(
        text = "Personal Best Score per Category",
        style = MaterialTheme.typography.titleLarge,
        color = GoldCoin
    )
    QuizCategory.entries.forEach { cat ->
        val best = profile.bestScoresByCategory[cat.id] ?: 0
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cat.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = if (best > 0) "$best pts" else "No score yet",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (best > 0) GoldCoin else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Local Top 10 Leaderboard (On-Device)",
        style = MaterialTheme.typography.titleLarge,
        color = GoldCoin
    )

    if (profile.leaderboard.isEmpty()) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = GoldCoin,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Play your first Sprint, Daily Challenge, or Exam to record your Top 10 local high scores!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    } else {
        profile.leaderboard.forEachIndexed { index, entry ->
            val cat = QuizCategory.fromId(entry.categoryId)
            val avatar = AvatarPreset.fromId(entry.avatarId)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (index == 0) GoldCoin else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (index < 3) GoldCoin else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.width(36.dp)
                    )
                    AvatarSvgBadge(avatar = avatar, size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cat.title} • ${entry.modeName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Accuracy ${entry.accuracyPercent}% • ${entry.dateLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "${entry.score}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldCoin
                    )
                }
            }
        }
    }
}

/**
 * Dedicated 'Profile/Badges' section showing the student's profile milestone overview
 * and all 10 achievement badges ('100 Questions Answered', 'Math Master', etc.)
 * with live DataStore-backed progress bars.
 */
@Composable
private fun ProfileAndBadgesSection(profile: PlayerProfileState) {
    val avatar = AvatarPreset.fromId(profile.selectedAvatarId)
    val badgeItems = remember(profile) {
        com.example.domain.BadgeAchievementManager.buildBadgeProgressList(profile)
    }

    // Student Profile Overview Card
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            .testTag("profile_badges_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarSvgBadge(avatar = avatar, size = 56.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${avatar.displayName} • Level ${profile.level}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "${avatar.titleTag} • ${profile.xpIntoCurrentLevel}/${profile.xpNeededForNextLevel} XP to Lv.${profile.level + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = XpCyan,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { profile.levelProgressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = XpCyan,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatColumn("Answered", "${profile.totalQuestionsAnswered}", GoldCoin)
                ProfileStatColumn("Accuracy", "${profile.lifetimeAccuracyPercent}%", CorrectGreen)
                ProfileStatColumn("Math Right", "${profile.totalMathCorrect}", XpCyan)
                ProfileStatColumn("Badges", "${profile.unlockedBadgeIds.size}/${BadgeInfo.entries.size}", GoldCoin)
            }
        }
    }

    Text(
        text = "Profile / Badges (${profile.unlockedBadgeIds.size}/${BadgeInfo.entries.size} Earned)",
        style = MaterialTheme.typography.titleLarge,
        color = GoldCoin
    )

    badgeItems.forEach { item ->
        val badge = item.badge
        val isUnlocked = item.isEarned
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) {
                    badge.accentColor.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isUnlocked) 1.5.dp else 1.dp,
                    color = if (isUnlocked) badge.accentColor else Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(18.dp)
                )
                .testTag("badge_card_${badge.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) badge.accentColor.copy(alpha = 0.32f)
                                else Color.White.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                            contentDescription = badge.title,
                            tint = if (isUnlocked) badge.accentColor else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isUnlocked) CorrectGreen.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.10f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.progressLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isUnlocked) CorrectGreen else GoldCoin,
                            fontSize = 11.sp
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isUnlocked) CorrectGreen else badge.accentColor,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ThemesShopSection(
    profile: PlayerProfileState,
    onBuyOrSelectTheme: (AppThemePreset) -> Unit
) {
    Text(
        text = "Unlockable Gradient Themes (7)",
        style = MaterialTheme.typography.titleLarge,
        color = GoldCoin
    )
    AppThemePreset.entries.forEach { theme ->
        val isUnlocked = profile.unlockedThemeIds.contains(theme.id)
        val isEquipped = profile.selectedThemeId == theme.id

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isEquipped) 2.5.dp else 1.dp,
                    color = if (isEquipped) GoldCoin else theme.buttonBorderColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onBuyOrSelectTheme(theme) }
                .testTag("shop_theme_${theme.id}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.backgroundBrush())
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.complementaryButtonBrush())
                            .border(2.dp, theme.buttonBorderColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = when {
                                isEquipped -> "Equipped ✓"
                                isUnlocked -> "Tap to Equip"
                                else -> "Cost: ${theme.cost} Coins"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isEquipped) CorrectGreen else GoldCoin,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = { onBuyOrSelectTheme(theme) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isEquipped -> CorrectGreen
                            isUnlocked -> theme.buttonContainerColor
                            else -> GoldCoin
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            isEquipped -> "Active"
                            isUnlocked -> "Use"
                            else -> "${theme.cost} 🪙"
                        },
                        color = when {
                            !isUnlocked -> Color(0xFF0F172A)
                            isUnlocked && !isEquipped -> theme.buttonContentColor
                            else -> Color.White
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarsShopSection(
    profile: PlayerProfileState,
    onBuyOrSelectAvatar: (AvatarPreset) -> Unit
) {
    Text(
        text = "Unlockable Student SVG Avatars (8)",
        style = MaterialTheme.typography.titleLarge,
        color = GoldCoin
    )
    AvatarPreset.entries.forEach { avatar ->
        val isUnlocked = profile.unlockedAvatarIds.contains(avatar.id)
        val isEquipped = profile.selectedAvatarId == avatar.id

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isEquipped) 2.5.dp else 1.dp,
                    color = if (isEquipped) GoldCoin else Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onBuyOrSelectAvatar(avatar) }
                .testTag("shop_avatar_${avatar.id}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarSvgBadge(avatar = avatar, size = 52.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = avatar.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = avatar.titleTag,
                            style = MaterialTheme.typography.bodyMedium,
                            color = avatar.accentColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = { onBuyOrSelectAvatar(avatar) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isEquipped -> CorrectGreen
                            isUnlocked -> Color.White.copy(alpha = 0.2f)
                            else -> GoldCoin
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            isEquipped -> "Active"
                            isUnlocked -> "Equip"
                            else -> "${avatar.cost} 🪙"
                        },
                        color = if (!isUnlocked) Color(0xFF0F172A) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    profile: PlayerProfileState,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onSelectTheme: (AppThemePreset) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var showResetConfirmation by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            // Sound Effects Toggle
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Sound",
                            tint = GoldCoin
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Synthesized Sound Effects",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Chimes, combo tones & buzz feedback",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Switch(
                        checked = profile.soundEnabled,
                        onCheckedChange = onToggleSound,
                        colors = SwitchDefaults.colors(checkedThumbColor = CorrectGreen),
                        modifier = Modifier.testTag("toggle_sound_switch")
                    )
                }
            }

            // Vibration Toggle
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Vibration",
                            tint = XpCyan
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Haptic Vibration",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Tactile pulse on answers and combos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Switch(
                        checked = profile.vibrationEnabled,
                        onCheckedChange = onToggleVibration,
                        colors = SwitchDefaults.colors(checkedThumbColor = CorrectGreen),
                        modifier = Modifier.testTag("toggle_vibration_switch")
                    )
                }
            }

            // Theme Picker Section
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme",
                            tint = GoldCoin
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Active Theme (${profile.unlockedThemeIds.size} Unlocked)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    AppThemePreset.entries.forEach { preset ->
                        val unlocked = profile.unlockedThemeIds.contains(preset.id)
                        val active = profile.selectedThemeId == preset.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (active) preset.primaryColor.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.06f)
                                )
                                .clickable { onSelectTheme(preset) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    active -> "Active ✓"
                                    unlocked -> "Switch"
                                    else -> "${preset.cost} Coins"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) CorrectGreen else GoldCoin
                            )
                        }
                    }
                }
            }

            // Reset All Progress
            Button(
                onClick = { showResetConfirmation = true },
                colors = ButtonDefaults.buttonColors(containerColor = WrongRed.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, WrongRed, RoundedCornerShape(16.dp))
                    .testTag("btn_reset_progress")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = WrongRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset All Progress & Local Storage",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset All Progress?") },
            text = {
                Text("This will reset your XP, Level, Coins, 7-Day Streak, Personal Bests, Leaderboard, and Unlocked Items back to the default starter state.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation = false
                        onResetProgress()
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset Everything", color = WrongRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
