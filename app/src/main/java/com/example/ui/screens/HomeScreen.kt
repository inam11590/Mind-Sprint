package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerProfileState
import com.example.domain.AvatarPreset
import com.example.domain.GameMode
import com.example.ui.components.AvatarSvgBadge
import com.example.ui.components.HomeBannerAdPlaceholder
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.XpCyan
import com.example.viewmodel.AppScreen

@Composable
fun HomeScreen(
    profile: PlayerProfileState,
    calendarDays: List<Pair<String, String>>,
    dailyCompletedToday: Boolean,
    onOpenModeSetup: (GameMode) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val avatar = AvatarPreset.fromId(profile.selectedAvatarId)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Player Header (Avatar, Level & XP Bar, Coins)
            PlayerHeaderCard(
                profile = profile,
                avatar = avatar,
                onAvatarClick = { onNavigate(AppScreen.PROGRESS_AND_SHOP) }
            )

            // 2. 7-Day Streak Counter & Simple Calendar Strip
            StreakCalendarCard(
                currentStreak = profile.currentStreak,
                activeDates = profile.activeDatesIso,
                calendarDays = calendarDays
            )

            // 3. Visual Hero Banner with App Title
            HeroBannerCard()

            // 4. Primary Action Buttons: Play Sprint, Daily Challenge, Exam Mode, Progress, Settings
            HeroSprintButton(
                onClick = { onOpenModeSetup(GameMode.SPRINT) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryModeCard(
                    title = stringResource(R.string.btn_daily_challenge),
                    subtitle = if (dailyCompletedToday) "Completed Today ✓" else "10 Seeded Qs • 1/Day",
                    icon = Icons.Default.CalendarToday,
                    accentColor = if (dailyCompletedToday) CorrectGreen else Color(0xFF38BDF8),
                    testTag = "btn_daily_challenge",
                    onClick = { onOpenModeSetup(GameMode.DAILY_CHALLENGE) },
                    modifier = Modifier.weight(1f)
                )

                SecondaryModeCard(
                    title = stringResource(R.string.btn_exam_mode),
                    subtitle = "20 Qs • Weak Topics",
                    icon = Icons.Default.School,
                    accentColor = Color(0xFFA78BFA),
                    testTag = "btn_exam_mode",
                    onClick = { onOpenModeSetup(GameMode.EXAM_MODE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryModeCard(
                    title = stringResource(R.string.btn_progress),
                    subtitle = "Shop, Badges & Top 10",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = GoldCoin,
                    testTag = "btn_progress",
                    onClick = { onNavigate(AppScreen.PROGRESS_AND_SHOP) },
                    modifier = Modifier.weight(1f)
                )

                SecondaryModeCard(
                    title = stringResource(R.string.btn_settings),
                    subtitle = "Audio, Haptics & Theme",
                    icon = Icons.Default.Settings,
                    accentColor = Color(0xFFF472B6),
                    testTag = "btn_settings",
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // 5. Banner Ad Placeholder (Strictly at bottom of Home Screen only, never during gameplay)
        HomeBannerAdPlaceholder(
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun PlayerHeaderCard(
    profile: PlayerProfileState,
    avatar: AvatarPreset,
    onAvatarClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarSvgBadge(
                avatar = avatar,
                size = 54.dp,
                modifier = Modifier
                    .clickable(onClick = onAvatarClick)
                    .testTag("home_avatar_badge")
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Level ${profile.level}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(XpCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${profile.xpIntoCurrentLevel}/${profile.xpNeededForNextLevel} XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = XpCyan,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { profile.levelProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = XpCyan,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Coins Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldCoin.copy(alpha = 0.18f))
                    .border(1.dp, GoldCoin.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onAvatarClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("home_coins_pill"),
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
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldCoin
                )
            }
        }
    }
}

@Composable
private fun StreakCalendarCard(
    currentStreak: Int,
    activeDates: Set<String>,
    calendarDays: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = StreakFlame,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currentStreak-Day Study Streak",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "7-Day Calendar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                calendarDays.forEach { (dayLabel, isoDate) ->
                    val isDone = activeDates.contains(isoDate)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) StreakFlame else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isDone) GoldCoin else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (dayLabel == "Today") GoldCoin else Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = if (dayLabel == "Today") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBannerCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_mindsprint_1794314658944),
                contentDescription = stringResource(R.string.hero_banner_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0F172A).copy(alpha = 0.90f),
                                Color(0xFF1E1B4B).copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = GoldCoin,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INFINITE BRAIN ARENA",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldCoin,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "MIND SPRINT",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Math • Vocab • Science • Logic",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSprintButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("btn_play_sprint")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.btn_play_sprint),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "60s Timer • +2s Correct • Combo up to x5!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Play Sprint",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun SecondaryModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        modifier = modifier
            .border(1.5.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp
            )
        }
    }
}
