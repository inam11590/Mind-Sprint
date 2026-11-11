package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PlayerStatsDataStore
import com.example.data.ProgressRepository
import com.example.domain.AvatarPreset
import com.example.domain.BadgeAchievementManager
import com.example.domain.BadgeInfo
import com.example.domain.GameMode
import com.example.domain.QuizCategory
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.ThemeManager
import com.example.util.HapticFeedbackManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and verify DataStore, Haptics, Badges, and ThemeManager`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mind Sprint", appName)

        val dataStore = PlayerStatsDataStore(context)
        dataStore.resetAllStats()

        // Verify XP & Coins update
        dataStore.addXpAndCoins(xpDelta = 300, coinsDelta = 280)
        val afterXp = dataStore.playerProfileFlow.first()
        assertEquals(300, afterXp.xp)
        assertEquals(3, afterXp.level)
        assertEquals(400, afterXp.coins)

        // Verify unlocking and equipping an avatar persists
        val unlockedFox = dataStore.unlockAndSelectAvatar(
            avatarId = AvatarPreset.VOLT_FOX.id,
            cost = AvatarPreset.VOLT_FOX.cost
        )
        assertTrue(unlockedFox)

        // Verify ThemeManager palettes (>= 6 unique linear-gradient palettes) and DataStore persistence
        val themeManager = ThemeManager(dataStore)
        assertTrue(themeManager.availablePalettes.size >= 6)
        assertEquals(AppThemePreset.COSMIC_INDIGO, themeManager.getCurrentTheme())

        val unlockedSunset = themeManager.unlockOrSelectTheme(AppThemePreset.SUNSET_BLAZE)
        assertTrue(unlockedSunset)
        assertEquals(AppThemePreset.SUNSET_BLAZE, themeManager.getCurrentTheme())
        assertEquals(AppThemePreset.SUNSET_BLAZE, themeManager.selectedThemeFlow.first())

        themeManager.setSelectedTheme(AppThemePreset.AURORA_MYSTIC)
        assertEquals(AppThemePreset.AURORA_MYSTIC, themeManager.getCurrentTheme())

        // Verify Badge Achievement System: '100 Questions Answered' and 'Math Master'
        val badgeEval = dataStore.recordMilestonesAndEvaluateBadges(
            questionsAnsweredDelta = 100,
            correctAnswersDelta = 92,
            categoryCorrectDeltas = mapOf(QuizCategory.MENTAL_MATH to 16),
            sessionMode = GameMode.SPRINT,
            sessionMaxCombo = 5
        )
        assertTrue(badgeEval.updatedUnlockedBadgeIds.contains(BadgeInfo.CENTURY_SCHOLAR.id))
        assertTrue(badgeEval.updatedUnlockedBadgeIds.contains(BadgeInfo.MATH_MASTER.id))
        assertTrue(badgeEval.updatedUnlockedBadgeIds.contains(BadgeInfo.SPEED_DEMON.id))

        val savedProfile = dataStore.playerProfileFlow.first()
        assertEquals(100, savedProfile.totalQuestionsAnswered)
        assertTrue(savedProfile.unlockedBadgeIds.contains(BadgeInfo.CENTURY_SCHOLAR.id))
        assertTrue(savedProfile.unlockedBadgeIds.contains(BadgeInfo.MATH_MASTER.id))

        val progressList = BadgeAchievementManager.buildBadgeProgressList(savedProfile)
        val centuryBadgeProgress = progressList.first { it.badge == BadgeInfo.CENTURY_SCHOLAR }
        assertTrue(centuryBadgeProgress.isEarned)
        assertEquals(1f, centuryBadgeProgress.progressFraction, 0.001f)

        // Verify HapticFeedbackManager and toggleable vibration preference
        val hapticManager = HapticFeedbackManager(context)
        assertTrue(hapticManager.isVibrationEnabled)
        hapticManager.vibrateCorrectAnswer(comboMultiplier = 4)
        hapticManager.vibrateIncorrectAnswer()

        dataStore.setVibrationEnabled(false)
        val vibrationPref = dataStore.vibrationEnabledFlow.first()
        assertFalse(vibrationPref)
        hapticManager.updatePreference(vibrationPref)
        assertFalse(hapticManager.isVibrationEnabled)

        val repo = ProgressRepository(context)
        val slots = repo.getSevenDayStreakSlots(savedProfile.activeDatesIso)
        assertEquals(7, slots.size)
        assertTrue(slots.last().isActive)
    }
}
