package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.InterstitialAdModal
import com.example.ui.components.RewardedAdModal
import com.example.ui.screens.DailyChallengeHubScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModeSetupScreen
import com.example.ui.screens.ProgressAndShopScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.MindSprintTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MindSprintViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindSprintAppRoot()
        }
    }
}

/**
 * Top-level App Composable with a built-in Error Boundary and dynamic Gradient Theme wrapper
 * so the app never shows a blank screen.
 */
@Composable
fun MindSprintAppRoot(
    viewModel: MindSprintViewModel = viewModel()
) {
    var caughtFatalError by remember { mutableStateOf<String?>(null) }

    if (caughtFatalError != null) {
        MindSprintTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppThemePreset.COSMIC_INDIGO.backgroundBrush())
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Mind Sprint Recovery",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldCoin
                        )
                        Text(
                            text = caughtFatalError ?: "An unexpected error occurred.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                caughtFatalError = null
                                viewModel.navigateTo(AppScreen.HOME)
                            }
                        ) {
                            Text("Return to Home Screen")
                        }
                    }
                }
            }
        }
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTheme = AppThemePreset.fromId(uiState.profile.selectedThemeId)

    MindSprintTheme(themePreset = activeTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(activeTheme.backgroundBrush())
                    .padding(innerPadding)
            ) {
                when (uiState.currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            profile = uiState.profile,
                            calendarDays = viewModel.getLast7DaysCalendar(),
                            dailyCompletedToday = viewModel.isDailyChallengeCompletedToday(),
                            onOpenModeSetup = { mode -> viewModel.openModeSetup(mode) },
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    }

                    AppScreen.MODE_SETUP -> {
                        ModeSetupScreen(
                            mode = uiState.pendingMode,
                            selectedCategory = uiState.selectedCategory,
                            selectedDifficulty = uiState.selectedExamDifficulty,
                            bestScores = uiState.profile.bestScoresByCategory,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onSelectDifficulty = { viewModel.selectExamDifficulty(it) },
                            onStartGame = { viewModel.startSelectedGame() },
                            onBack = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.DAILY_CHALLENGE_HUB -> {
                        DailyChallengeHubScreen(
                            todayDateSeed = viewModel.todayDateSeed(),
                            profile = uiState.profile,
                            alreadyPlayedToday = viewModel.isDailyChallengeCompletedToday(),
                            onStartDaily = { viewModel.startDailyChallenge() },
                            onBack = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.PLAYING -> {
                        com.example.ui.screens.QuizScreen(
                            gameState = uiState.activeGame,
                            onSelectAnswer = { idx -> viewModel.submitAnswer(idx) },
                            onRequestExtraLife = { viewModel.requestExtraLifeWithRewardedAd() },
                            onDeclineExtraLife = { viewModel.declineExtraLifeAndFinish() },
                            onQuitGame = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.GAME_OVER -> {
                        val summary = uiState.lastGameSummary
                        if (summary != null) {
                            GameOverScreen(
                                summary = summary,
                                currentStreak = uiState.profile.currentStreak,
                                doubleCoinsUsed = uiState.activeGame.doubleCoinsUsed,
                                onDoubleCoinsAd = { viewModel.requestDoubleCoinsWithRewardedAd() },
                                onPlayAgain = { viewModel.startSelectedGame() },
                                onBackHome = { viewModel.navigateTo(AppScreen.HOME) }
                            )
                        } else {
                            viewModel.navigateTo(AppScreen.HOME)
                        }
                    }

                    AppScreen.PROGRESS_AND_SHOP -> {
                        ProgressAndShopScreen(
                            profile = uiState.profile,
                            streakDays = viewModel.getSevenDayStreakSlots(),
                            onBuyOrSelectTheme = { viewModel.buyOrSelectTheme(it) },
                            onBuyOrSelectAvatar = { viewModel.buyOrSelectAvatar(it) },
                            onBack = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            profile = uiState.profile,
                            onToggleSound = { viewModel.toggleSound(it) },
                            onToggleVibration = { viewModel.toggleVibration(it) },
                            onSelectTheme = { viewModel.buyOrSelectTheme(it) },
                            onResetProgress = { viewModel.resetAllProgress() },
                            onBack = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }
                }

                // Floating Status Toast Banner
                AnimatedVisibility(
                    visible = uiState.statusBannerMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text(
                            text = uiState.statusBannerMessage.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldCoin,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                // Interstitial Ad Modal Placeholder (after every 3 games)
                if (uiState.showInterstitialAd) {
                    InterstitialAdModal(
                        onDismiss = { viewModel.dismissInterstitialAd() }
                    )
                }

                // Rewarded Ad Modal Placeholder ("Extra Life" or "Double Coins")
                val rewardedReq = uiState.activeRewardedAdRequest
                if (rewardedReq != null) {
                    RewardedAdModal(
                        rewardTitle = rewardedReq.title,
                        rewardSubtitle = rewardedReq.subtitle,
                        onRewardEarned = rewardedReq.onRewardEarned,
                        onCancel = { viewModel.cancelRewardedAd() }
                    )
                }
            }
        }
    }
}
