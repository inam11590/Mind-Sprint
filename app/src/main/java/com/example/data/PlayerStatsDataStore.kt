package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.AvatarPreset
import com.example.domain.BadgeAchievementManager
import com.example.domain.BadgeEvaluationResult
import com.example.domain.BadgeInfo
import com.example.domain.GameMode
import com.example.domain.LeaderboardEntry
import com.example.domain.QuizCategory
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Context.playerStatsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mind_sprint_player_stats"
)

/**
 * Jetpack DataStore<Preferences> wrapper for persisting cross-session player statistics:
 * - Current XP & derived Player Level
 * - Coins balance
 * - Unlocked & selected Avatars and Themes
 * - 7-Day Streak Counter & active calendar dates
 * - Cumulative Question Milestones & Earned Achievement Badges ('100 Questions Answered', 'Math Master', etc.)
 * - Category Personal Bests, Top 10 Local Leaderboard, and Settings
 */
class PlayerStatsDataStore(private val context: Context) {

    private val dataStore: DataStore<Preferences>
        get() = context.playerStatsDataStore

    /**
     * Reactive stream of the player's persisted profile state.
     */
    val playerProfileFlow: Flow<PlayerProfileState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> mapPreferencesToProfile(prefs) }

    /**
     * Reactive stream of the currently selected theme ID in DataStore.
     */
    val selectedThemeIdFlow: Flow<String> = playerProfileFlow.map { it.selectedThemeId }

    /**
     *Reactive stream of earned badge IDs stored in DataStore.
     */
    val unlockedBadgesFlow: Flow<Set<String>> = playerProfileFlow.map { it.unlockedBadgeIds }

    /**
     * Reactive stream of the user's toggleable haptic vibration preference.
     */
    val vibrationEnabledFlow: Flow<Boolean> = playerProfileFlow.map { it.vibrationEnabled }

    /**
     * Reads the current snapshot of [PlayerProfileState] from DataStore,
     * automatically recording today's login toward the 7-day streak counter.
     */
    suspend fun loadAndSyncDailyStreak(): PlayerProfileState {
        val current = playerProfileFlow.first()
        return recordDailyStreakActivity(current)
    }

    /**
     * Persists a complete [PlayerProfileState] snapshot into DataStore.
     */
    suspend fun saveProfile(state: PlayerProfileState) {
        dataStore.edit { prefs ->
            prefs[Keys.XP] = state.xp
            prefs[Keys.COINS] = state.coins
            prefs[Keys.CURRENT_STREAK] = state.currentStreak
            prefs[Keys.LONGEST_STREAK] = state.longestStreak
            prefs[Keys.ACTIVE_DATES] = state.activeDatesIso
            prefs[Keys.LAST_ACTIVE_DATE] = todayIsoString()
            prefs[Keys.LAST_DAILY_DATE] = state.lastDailyPlayedDate
            prefs[Keys.LAST_DAILY_SCORE] = state.lastDailyScore
            prefs[Keys.LAST_DAILY_TIME] = state.lastDailyTimeSec
            prefs[Keys.LAST_DAILY_CORRECT] = state.lastDailyCorrect
            prefs[Keys.SELECTED_THEME] = state.selectedThemeId
            prefs[Keys.UNLOCKED_THEMES] = state.unlockedThemeIds
            prefs[Keys.SELECTED_AVATAR] = state.selectedAvatarId
            prefs[Keys.UNLOCKED_AVATARS] = state.unlockedAvatarIds
            prefs[Keys.UNLOCKED_BADGES] = state.unlockedBadgeIds
            prefs[Keys.LEADERBOARD] = serializeLeaderboard(state.leaderboard)
            prefs[Keys.TOTAL_GAMES] = state.totalGamesCompleted
            prefs[Keys.TOTAL_QUESTIONS] = state.totalQuestionsAnswered
            prefs[Keys.TOTAL_CORRECT] = state.totalCorrectAnswers
            prefs[Keys.TOTAL_MATH_CORRECT] = state.totalMathCorrect
            prefs[Keys.TOTAL_VOCAB_CORRECT] = state.totalVocabCorrect
            prefs[Keys.TOTAL_SCIENCE_CORRECT] = state.totalScienceCorrect
            prefs[Keys.TOTAL_LOGIC_CORRECT] = state.totalLogicCorrect
            prefs[Keys.SOUND_ENABLED] = state.soundEnabled
            prefs[Keys.VIBRATION_ENABLED] = state.vibrationEnabled

            for (cat in QuizCategory.entries) {
                val best = state.bestScoresByCategory[cat.id] ?: 0
                prefs[bestScoreKey(cat.id)] = best
            }
        }
    }

    /**
     * Records answered question milestones and evaluates badge achievements
     * (such as '100 Questions Answered' and 'Math Master'), storing newly earned badges in DataStore.
     */
    suspend fun recordMilestonesAndEvaluateBadges(
        questionsAnsweredDelta: Int,
        correctAnswersDelta: Int = questionsAnsweredDelta,
        categoryCorrectDeltas: Map<QuizCategory, Int> = emptyMap(),
        sessionMode: GameMode? = null,
        sessionMaxCombo: Int = 0
    ): BadgeEvaluationResult {
        val current = playerProfileFlow.first()
        val updatedTotals = current.copy(
            totalGamesCompleted = current.totalGamesCompleted + (if (sessionMode != null) 1 else 0),
            totalQuestionsAnswered = current.totalQuestionsAnswered + questionsAnsweredDelta.coerceAtLeast(0),
            totalCorrectAnswers = current.totalCorrectAnswers + correctAnswersDelta.coerceAtLeast(0),
            totalMathCorrect = current.totalMathCorrect + (categoryCorrectDeltas[QuizCategory.MENTAL_MATH] ?: 0),
            totalVocabCorrect = current.totalVocabCorrect + (categoryCorrectDeltas[QuizCategory.ENGLISH_VOCAB] ?: 0),
            totalScienceCorrect = current.totalScienceCorrect + (categoryCorrectDeltas[QuizCategory.SCIENCE] ?: 0),
            totalLogicCorrect = current.totalLogicCorrect + (categoryCorrectDeltas[QuizCategory.LOGIC_PATTERNS] ?: 0)
        )

        val evaluation = BadgeAchievementManager.evaluateAchievements(
            profile = updatedTotals,
            sessionMode = sessionMode,
            sessionMaxCombo = sessionMaxCombo,
            sessionExamCorrect = correctAnswersDelta,
            sessionCategoryCorrect = categoryCorrectDeltas
        )

        saveProfile(updatedTotals.copy(unlockedBadgeIds = evaluation.updatedUnlockedBadgeIds))
        return evaluation
    }

    /**
     * Atomically increments XP and Coins in DataStore.
     */
    suspend fun addXpAndCoins(xpDelta: Int, coinsDelta: Int) {
        dataStore.edit { prefs ->
            val currentXp = prefs[Keys.XP] ?: 0
            val currentCoins = prefs[Keys.COINS] ?: DEFAULT_STARTER_COINS
            prefs[Keys.XP] = (currentXp + xpDelta).coerceAtLeast(0)
            prefs[Keys.COINS] = (currentCoins + coinsDelta).coerceAtLeast(0)
        }
    }

    /**
     * Directly persists the selected theme ID in DataStore and ensures it is in the unlocked set.
     */
    suspend fun setSelectedTheme(themeId: String) {
        dataStore.edit { prefs ->
            val unlocked = (prefs[Keys.UNLOCKED_THEMES] ?: setOf(AppThemePreset.COSMIC_INDIGO.id)).toMutableSet()
            unlocked.add(themeId)
            prefs[Keys.UNLOCKED_THEMES] = unlocked
            prefs[Keys.SELECTED_THEME] = themeId
        }
    }

    /**
     * Unlocks and equips a theme palette in DataStore, deducting [cost] coins if not yet unlocked.
     */
    suspend fun unlockAndSelectTheme(themeId: String, cost: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val unlocked = (prefs[Keys.UNLOCKED_THEMES] ?: setOf(AppThemePreset.COSMIC_INDIGO.id)).toMutableSet()
            val currentCoins = prefs[Keys.COINS] ?: DEFAULT_STARTER_COINS
            if (unlocked.contains(themeId)) {
                prefs[Keys.SELECTED_THEME] = themeId
                success = true
            } else if (currentCoins >= cost) {
                unlocked.add(themeId)
                prefs[Keys.COINS] = currentCoins - cost
                prefs[Keys.UNLOCKED_THEMES] = unlocked
                prefs[Keys.SELECTED_THEME] = themeId
                success = true
            }
        }
        return success
    }

    /**
     * Unlocks and equips an avatar in DataStore, deducting [cost] coins if not yet unlocked.
     */
    suspend fun unlockAndSelectAvatar(avatarId: String, cost: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val unlocked = (prefs[Keys.UNLOCKED_AVATARS] ?: setOf(AvatarPreset.ASTRO_OWL.id)).toMutableSet()
            val currentCoins = prefs[Keys.COINS] ?: DEFAULT_STARTER_COINS
            if (unlocked.contains(avatarId)) {
                prefs[Keys.SELECTED_AVATAR] = avatarId
                success = true
            } else if (currentCoins >= cost) {
                unlocked.add(avatarId)
                prefs[Keys.COINS] = currentCoins - cost
                prefs[Keys.UNLOCKED_AVATARS] = unlocked
                prefs[Keys.SELECTED_AVATAR] = avatarId
                success = true
            }
        }
        return success
    }

    /**
     * Updates the 7-day streak counter and active dates set in DataStore based on today's date.
     */
    suspend fun recordDailyStreakActivity(baseState: PlayerProfileState? = null): PlayerProfileState {
        val state = baseState ?: playerProfileFlow.first()
        val today = todayIsoString()
        val updatedDates = state.activeDatesIso.toMutableSet().apply { add(today) }
        val newStreak = calculateConsecutiveStreakToToday(updatedDates)
        val newLongest = maxOf(state.longestStreak, newStreak)
        val updatedBadges = state.unlockedBadgeIds.toMutableSet().apply {
            if (newStreak >= 7) add(BadgeInfo.STREAK_7_DAYS.id)
        }

        val updatedState = state.copy(
            currentStreak = newStreak,
            longestStreak = newLongest,
            activeDatesIso = updatedDates,
            unlockedBadgeIds = updatedBadges
        )
        saveProfile(updatedState)
        return updatedState
    }

    /**
     * Updates the toggleable haptic vibration setting in user preferences.
     */
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    /**
     * Resets all stored DataStore preferences back to initial defaults.
     */
    suspend fun resetAllStats(): PlayerProfileState {
        dataStore.edit { it.clear() }
        return loadAndSyncDailyStreak()
    }

    private fun mapPreferencesToProfile(prefs: Preferences): PlayerProfileState {
        val today = todayIsoString()
        val activeDates = (prefs[Keys.ACTIVE_DATES] ?: setOf(today)).toMutableSet().apply {
            add(today)
        }
        val currentStreak = calculateConsecutiveStreakToToday(activeDates)
        val savedLongest = prefs[Keys.LONGEST_STREAK] ?: 1
        val longestStreak = maxOf(savedLongest, currentStreak)

        val totalQuestions = prefs[Keys.TOTAL_QUESTIONS] ?: 0
        val totalMath = prefs[Keys.TOTAL_MATH_CORRECT] ?: 0
        val totalVocab = prefs[Keys.TOTAL_VOCAB_CORRECT] ?: 0
        val totalScience = prefs[Keys.TOTAL_SCIENCE_CORRECT] ?: 0
        val totalLogic = prefs[Keys.TOTAL_LOGIC_CORRECT] ?: 0

        val unlockedBadges = (prefs[Keys.UNLOCKED_BADGES] ?: emptySet()).toMutableSet().apply {
            if (currentStreak >= 7) add(BadgeInfo.STREAK_7_DAYS.id)
            if (totalQuestions >= BadgeInfo.CENTURY_SCHOLAR.targetMilestone) add(BadgeInfo.CENTURY_SCHOLAR.id)
            if (totalMath >= BadgeInfo.MATH_MASTER.targetMilestone) add(BadgeInfo.MATH_MASTER.id)
            if (totalVocab >= BadgeInfo.VOCAB_VIRTUOSO.targetMilestone) add(BadgeInfo.VOCAB_VIRTUOSO.id)
            if (totalScience >= BadgeInfo.SCIENCE_WHIZ.targetMilestone) add(BadgeInfo.SCIENCE_WHIZ.id)
            if (totalLogic >= BadgeInfo.LOGIC_PROPHET.targetMilestone) add(BadgeInfo.LOGIC_PROPHET.id)
        }

        val bestScores = mutableMapOf<String, Int>()
        for (cat in QuizCategory.entries) {
            bestScores[cat.id] = prefs[bestScoreKey(cat.id)] ?: 0
        }

        return PlayerProfileState(
            xp = prefs[Keys.XP] ?: 0,
            coins = prefs[Keys.COINS] ?: DEFAULT_STARTER_COINS,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            activeDatesIso = activeDates,
            lastDailyPlayedDate = prefs[Keys.LAST_DAILY_DATE] ?: "",
            lastDailyScore = prefs[Keys.LAST_DAILY_SCORE] ?: 0,
            lastDailyTimeSec = prefs[Keys.LAST_DAILY_TIME] ?: 0,
            lastDailyCorrect = prefs[Keys.LAST_DAILY_CORRECT] ?: 0,
            selectedThemeId = prefs[Keys.SELECTED_THEME] ?: AppThemePreset.COSMIC_INDIGO.id,
            unlockedThemeIds = prefs[Keys.UNLOCKED_THEMES] ?: setOf(AppThemePreset.COSMIC_INDIGO.id),
            selectedAvatarId = prefs[Keys.SELECTED_AVATAR] ?: AvatarPreset.ASTRO_OWL.id,
            unlockedAvatarIds = prefs[Keys.UNLOCKED_AVATARS] ?: setOf(AvatarPreset.ASTRO_OWL.id),
            unlockedBadgeIds = unlockedBadges,
            bestScoresByCategory = bestScores,
            leaderboard = parseLeaderboard(prefs[Keys.LEADERBOARD] ?: ""),
            totalGamesCompleted = prefs[Keys.TOTAL_GAMES] ?: 0,
            totalQuestionsAnswered = totalQuestions,
            totalCorrectAnswers = prefs[Keys.TOTAL_CORRECT] ?: 0,
            totalMathCorrect = totalMath,
            totalVocabCorrect = totalVocab,
            totalScienceCorrect = totalScience,
            totalLogicCorrect = totalLogic,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true
        )
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

    private fun todayIsoString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun bestScoreKey(categoryId: String): Preferences.Key<Int> =
        intPreferencesKey("best_score_$categoryId")

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

    private object Keys {
        val XP = intPreferencesKey("xp")
        val COINS = intPreferencesKey("coins")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val ACTIVE_DATES = stringSetPreferencesKey("active_dates_set")
        val LAST_DAILY_DATE = stringPreferencesKey("last_daily_date")
        val LAST_DAILY_SCORE = intPreferencesKey("last_daily_score")
        val LAST_DAILY_TIME = intPreferencesKey("last_daily_time")
        val LAST_DAILY_CORRECT = intPreferencesKey("last_daily_correct")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
        val SELECTED_AVATAR = stringPreferencesKey("selected_avatar")
        val UNLOCKED_AVATARS = stringSetPreferencesKey("unlocked_avatars")
        val UNLOCKED_BADGES = stringSetPreferencesKey("unlocked_badges")
        val LEADERBOARD = stringPreferencesKey("local_leaderboard_v1")
        val TOTAL_GAMES = intPreferencesKey("total_games_completed")
        val TOTAL_QUESTIONS = intPreferencesKey("total_questions_answered")
        val TOTAL_CORRECT = intPreferencesKey("total_correct_answers")
        val TOTAL_MATH_CORRECT = intPreferencesKey("total_math_correct")
        val TOTAL_VOCAB_CORRECT = intPreferencesKey("total_vocab_correct")
        val TOTAL_SCIENCE_CORRECT = intPreferencesKey("total_science_correct")
        val TOTAL_LOGIC_CORRECT = intPreferencesKey("total_logic_correct")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    companion object {
        const val DEFAULT_STARTER_COINS = 120
    }
}
