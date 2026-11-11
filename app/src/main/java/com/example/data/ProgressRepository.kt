package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.AvatarPreset
import com.example.domain.BadgeInfo
import com.example.domain.LeaderboardEntry
import com.example.domain.QuizCategory
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StreakDaySlot(
    val dayLabel: String,
    val dayOfMonth: String,
    val isoDate: String,
    val isActive: Boolean,
    val isToday: Boolean
)

data class PlayerProfileState(
    val xp: Int = 0,
    val coins: Int = 120,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val activeDatesIso: Set<String> = emptySet(),
    val lastDailyPlayedDate: String = "",
    val lastDailyScore: Int = 0,
    val lastDailyTimeSec: Int = 0,
    val lastDailyCorrect: Int = 0,
    val selectedThemeId: String = AppThemePreset.COSMIC_INDIGO.id,
    val unlockedThemeIds: Set<String> = setOf(AppThemePreset.COSMIC_INDIGO.id),
    val selectedAvatarId: String = AvatarPreset.ASTRO_OWL.id,
    val unlockedAvatarIds: Set<String> = setOf(AvatarPreset.ASTRO_OWL.id),
    val unlockedBadgeIds: Set<String> = emptySet(),
    val bestScoresByCategory: Map<String, Int> = emptyMap(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val totalGamesCompleted: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalMathCorrect: Int = 0,
    val totalVocabCorrect: Int = 0,
    val totalScienceCorrect: Int = 0,
    val totalLogicCorrect: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    /**
     * Player Level starts at Level 1. Every 150 XP advances a level.
     */
    val level: Int
        get() = 1 + (xp / 150)

    val xpIntoCurrentLevel: Int
        get() = xp % 150

    val xpNeededForNextLevel: Int
        get() = 150

    val levelProgressFraction: Float
        get() = (xpIntoCurrentLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)

    val lifetimeAccuracyPercent: Int
        get() = if (totalQuestionsAnswered <= 0) 0 else ((totalCorrectAnswers * 100f) / totalQuestionsAnswered).toInt()
}

/**
 * Local offline storage repository synchronized with [PlayerStatsDataStore].
 */
class ProgressRepository(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("mind_sprint_local_storage", Context.MODE_PRIVATE)
    val statsDataStore: PlayerStatsDataStore = PlayerStatsDataStore(appContext)
    private val ioScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )

    val profileFlow: kotlinx.coroutines.flow.Flow<PlayerProfileState>
        get() = statsDataStore.playerProfileFlow

    fun todayIsoString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun yesterdayIsoString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun getLast7DaysCalendar(): List<Pair<String, String>> {
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val list = mutableListOf<Pair<String, String>>()
        for (offset in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val label = if (offset == 0) "Today" else dayFormat.format(cal.time)
            list.add(label to isoFormat.format(cal.time))
        }
        return list
    }

    fun getSevenDayStreakSlots(activeDatesIso: Set<String>): List<StreakDaySlot> {
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val domFormat = SimpleDateFormat("d", Locale.US)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val slots = mutableListOf<StreakDaySlot>()
        for (offset in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val iso = isoFormat.format(cal.time)
            slots.add(
                StreakDaySlot(
                    dayLabel = if (offset == 0) "Today" else dayFormat.format(cal.time),
                    dayOfMonth = domFormat.format(cal.time),
                    isoDate = iso,
                    isActive = activeDatesIso.contains(iso),
                    isToday = offset == 0
                )
            )
        }
        return slots
    }

    fun recordDailyActivity(currentState: PlayerProfileState): PlayerProfileState {
        val today = todayIsoString()
        val updatedDates = currentState.activeDatesIso.toMutableSet().apply {
            add(today)
        }
        val recalculatedStreak = calculateConsecutiveStreakToToday(updatedDates)
        val newLongest = maxOf(currentState.longestStreak, recalculatedStreak)

        prefs.edit()
            .putString(KEY_LAST_ACTIVE_DATE, today)
            .putStringSet(KEY_ACTIVE_DATES, updatedDates)
            .putInt(KEY_CURRENT_STREAK, recalculatedStreak)
            .putInt(KEY_LONGEST_STREAK, newLongest)
            .apply()

        val updated = currentState.copy(
            currentStreak = recalculatedStreak,
            longestStreak = newLongest,
            activeDatesIso = updatedDates
        )
        ioScope.launch {
            statsDataStore.saveProfile(updated)
        }
        return updated
    }

    private fun calculateConsecutiveStreakToToday(activeDates: Set<String>): Int {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var streakCount = 0
        for (offset in 0..365) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val iso = isoFormat.format(cal.time)
            if (activeDates.contains(iso)) {
                streakCount++
            } else {
                break
            }
        }
        return streakCount.coerceAtLeast(1)
    }

    fun loadProfile(): PlayerProfileState {
        val today = todayIsoString()

        val savedDates = prefs.getStringSet(KEY_ACTIVE_DATES, null)?.toMutableSet()
            ?: mutableSetOf(today)
        savedDates.add(today)

        val currentStreak = calculateConsecutiveStreakToToday(savedDates)
        val savedLongest = prefs.getInt(KEY_LONGEST_STREAK, 1)
        val longestStreak = maxOf(savedLongest, currentStreak)

        prefs.edit()
            .putString(KEY_LAST_ACTIVE_DATE, today)
            .putStringSet(KEY_ACTIVE_DATES, savedDates)
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .apply()

        val unlockedThemes = prefs.getStringSet(
            KEY_UNLOCKED_THEMES,
            setOf(AppThemePreset.COSMIC_INDIGO.id)
        ) ?: setOf(AppThemePreset.COSMIC_INDIGO.id)

        val unlockedAvatars = prefs.getStringSet(
            KEY_UNLOCKED_AVATARS,
            setOf(AvatarPreset.ASTRO_OWL.id)
        ) ?: setOf(AvatarPreset.ASTRO_OWL.id)

        val unlockedBadges = prefs.getStringSet(KEY_UNLOCKED_BADGES, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        if (currentStreak >= 7) {
            unlockedBadges.add(BadgeInfo.STREAK_7_DAYS.id)
        }

        val bestScores = mutableMapOf<String, Int>()
        for (cat in QuizCategory.entries) {
            bestScores[cat.id] = prefs.getInt("best_score_${cat.id}", 0)
        }

        val leaderboardRaw = prefs.getString(KEY_LEADERBOARD, "") ?: ""
        val leaderboard = parseLeaderboard(leaderboardRaw)

        return PlayerProfileState(
            xp = prefs.getInt(KEY_XP, 0),
            coins = prefs.getInt(KEY_COINS, 120),
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            activeDatesIso = savedDates,
            lastDailyPlayedDate = prefs.getString(KEY_LAST_DAILY_DATE, "") ?: "",
            lastDailyScore = prefs.getInt(KEY_LAST_DAILY_SCORE, 0),
            lastDailyTimeSec = prefs.getInt(KEY_LAST_DAILY_TIME, 0),
            lastDailyCorrect = prefs.getInt(KEY_LAST_DAILY_CORRECT, 0),
            selectedThemeId = prefs.getString(KEY_SELECTED_THEME, AppThemePreset.COSMIC_INDIGO.id)
                ?: AppThemePreset.COSMIC_INDIGO.id,
            unlockedThemeIds = unlockedThemes,
            selectedAvatarId = prefs.getString(KEY_SELECTED_AVATAR, AvatarPreset.ASTRO_OWL.id)
                ?: AvatarPreset.ASTRO_OWL.id,
            unlockedAvatarIds = unlockedAvatars,
            unlockedBadgeIds = unlockedBadges,
            bestScoresByCategory = bestScores,
            leaderboard = leaderboard,
            totalGamesCompleted = prefs.getInt(KEY_TOTAL_GAMES, 0),
            totalQuestionsAnswered = prefs.getInt(KEY_TOTAL_QUESTIONS, 0),
            totalCorrectAnswers = prefs.getInt(KEY_TOTAL_CORRECT, 0),
            totalMathCorrect = prefs.getInt(KEY_TOTAL_MATH_CORRECT, 0),
            totalVocabCorrect = prefs.getInt(KEY_TOTAL_VOCAB_CORRECT, 0),
            totalScienceCorrect = prefs.getInt(KEY_TOTAL_SCIENCE_CORRECT, 0),
            totalLogicCorrect = prefs.getInt(KEY_TOTAL_LOGIC_CORRECT, 0),
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        )
    }

    fun saveProfile(state: PlayerProfileState) {
        val editor = prefs.edit()
            .putInt(KEY_XP, state.xp)
            .putInt(KEY_COINS, state.coins)
            .putInt(KEY_CURRENT_STREAK, state.currentStreak)
            .putInt(KEY_LONGEST_STREAK, state.longestStreak)
            .putStringSet(KEY_ACTIVE_DATES, state.activeDatesIso)
            .putString(KEY_LAST_DAILY_DATE, state.lastDailyPlayedDate)
            .putInt(KEY_LAST_DAILY_SCORE, state.lastDailyScore)
            .putInt(KEY_LAST_DAILY_TIME, state.lastDailyTimeSec)
            .putInt(KEY_LAST_DAILY_CORRECT, state.lastDailyCorrect)
            .putString(KEY_SELECTED_THEME, state.selectedThemeId)
            .putStringSet(KEY_UNLOCKED_THEMES, state.unlockedThemeIds)
            .putString(KEY_SELECTED_AVATAR, state.selectedAvatarId)
            .putStringSet(KEY_UNLOCKED_AVATARS, state.unlockedAvatarIds)
            .putStringSet(KEY_UNLOCKED_BADGES, state.unlockedBadgeIds)
            .putString(KEY_LEADERBOARD, serializeLeaderboard(state.leaderboard))
            .putInt(KEY_TOTAL_GAMES, state.totalGamesCompleted)
            .putInt(KEY_TOTAL_QUESTIONS, state.totalQuestionsAnswered)
            .putInt(KEY_TOTAL_CORRECT, state.totalCorrectAnswers)
            .putInt(KEY_TOTAL_MATH_CORRECT, state.totalMathCorrect)
            .putInt(KEY_TOTAL_VOCAB_CORRECT, state.totalVocabCorrect)
            .putInt(KEY_TOTAL_SCIENCE_CORRECT, state.totalScienceCorrect)
            .putInt(KEY_TOTAL_LOGIC_CORRECT, state.totalLogicCorrect)
            .putBoolean(KEY_SOUND_ENABLED, state.soundEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, state.vibrationEnabled)

        for ((catId, best) in state.bestScoresByCategory) {
            editor.putInt("best_score_$catId", best)
        }
        editor.apply()

        ioScope.launch {
            statsDataStore.saveProfile(state)
        }
    }

    fun resetAllProgress(): PlayerProfileState {
        prefs.edit().clear().apply()
        val fresh = loadProfile()
        ioScope.launch {
            statsDataStore.resetAllStats()
        }
        return fresh
    }

    private fun serializeLeaderboard(entries: List<LeaderboardEntry>): String {
        return entries.take(10).joinToString(";;") { e ->
            listOf(
                e.id,
                e.score.toString(),
                e.categoryId,
                e.modeName,
                e.accuracyPercent.toString(),
                e.dateLabel,
                e.avatarId
            ).joinToString("|")
        }
    }

    private fun parseLeaderboard(raw: String): List<LeaderboardEntry> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { row ->
            val parts = row.split("|")
            if (parts.size >= 7) {
                LeaderboardEntry(
                    id = parts[0],
                    score = parts[1].toIntOrNull() ?: 0,
                    categoryId = parts[2],
                    modeName = parts[3],
                    accuracyPercent = parts[4].toIntOrNull() ?: 0,
                    dateLabel = parts[5],
                    avatarId = parts[6]
                )
            } else null
        }
    }

    companion object {
        private const val KEY_XP = "xp"
        private const val KEY_COINS = "coins"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_ACTIVE_DATES = "active_dates_set"
        private const val KEY_LAST_DAILY_DATE = "last_daily_date"
        private const val KEY_LAST_DAILY_SCORE = "last_daily_score"
        private const val KEY_LAST_DAILY_TIME = "last_daily_time"
        private const val KEY_LAST_DAILY_CORRECT = "last_daily_correct"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_UNLOCKED_THEMES = "unlocked_themes"
        private const val KEY_SELECTED_AVATAR = "selected_avatar"
        private const val KEY_UNLOCKED_AVATARS = "unlocked_avatars"
        private const val KEY_UNLOCKED_BADGES = "unlocked_badges"
        private const val KEY_LEADERBOARD = "local_leaderboard_v1"
        private const val KEY_TOTAL_GAMES = "total_games_completed"
        private const val KEY_TOTAL_QUESTIONS = "total_questions_answered"
        private const val KEY_TOTAL_CORRECT = "total_correct_answers"
        private const val KEY_TOTAL_MATH_CORRECT = "total_math_correct"
        private const val KEY_TOTAL_VOCAB_CORRECT = "total_vocab_correct"
        private const val KEY_TOTAL_SCIENCE_CORRECT = "total_science_correct"
        private const val KEY_TOTAL_LOGIC_CORRECT = "total_logic_correct"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    }
}
