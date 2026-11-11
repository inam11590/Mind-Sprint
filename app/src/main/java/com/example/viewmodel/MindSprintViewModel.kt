package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PlayerProfileState
import com.example.data.ProgressRepository
import com.example.data.StreakDaySlot
import com.example.domain.AvatarPreset
import com.example.domain.BadgeInfo
import com.example.domain.DynamicDifficultyConfig
import com.example.domain.ExamDifficulty
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.GameSummary
import com.example.domain.LeaderboardEntry
import com.example.domain.QuizCategory
import com.example.domain.QuizQuestion
import com.example.domain.TopicPerformance
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.ThemeManager
import com.example.util.SoundAndHapticsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppScreen {
    HOME,
    MODE_SETUP,
    PLAYING,
    GAME_OVER,
    DAILY_CHALLENGE_HUB,
    PROGRESS_AND_SHOP,
    SETTINGS
}

enum class AnswerFeedbackState {
    NONE,
    CORRECT_FLASH,
    WRONG_SHAKE
}

data class ActiveGameState(
    val mode: GameMode = GameMode.SPRINT,
    val category: QuizCategory = QuizCategory.MIXED,
    val difficulty: ExamDifficulty = ExamDifficulty.EASY,
    val currentQuestion: QuizQuestion? = null,
    val questionIndex: Int = 1,
    val totalQuestionsLimit: Int? = null,
    val timeRemainingSeconds: Int = 60,
    val elapsedSeconds: Int = 0,
    val score: Int = 0,
    val comboMultiplier: Int = 1,
    val maxComboReached: Int = 1,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val selectedOptionIndex: Int? = null,
    val feedbackState: AnswerFeedbackState = AnswerFeedbackState.NONE,
    val extraLifeUsed: Boolean = false,
    val showExtraLifePrompt: Boolean = false,
    val doubleCoinsUsed: Boolean = false,
    val dynamicDifficultyLabel: String = "Lv.1 • Starter"
)

data class MindSprintUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val pendingMode: GameMode = GameMode.SPRINT,
    val selectedCategory: QuizCategory = QuizCategory.MENTAL_MATH,
    val selectedExamDifficulty: ExamDifficulty = ExamDifficulty.EASY,
    val profile: PlayerProfileState = PlayerProfileState(),
    val activeGame: ActiveGameState = ActiveGameState(),
    val lastGameSummary: GameSummary? = null,
    val showInterstitialAd: Boolean = false,
    val activeRewardedAdRequest: RewardedAdRequest? = null,
    val statusBannerMessage: String? = null
)

data class RewardedAdRequest(
    val title: String,
    val subtitle: String,
    val onRewardEarned: () -> Unit
)

class MindSprintViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProgressRepository(application.applicationContext)
    val themeManager = ThemeManager(repository.statsDataStore)
    private val soundManager = SoundAndHapticsManager(application.applicationContext)
    private val questionGenerator = GameEngine()

    private val _uiState = MutableStateFlow(MindSprintUiState())
    val uiState: StateFlow<MindSprintUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var answerTransitionJob: Job? = null
    private var dailyQuestionsQueue: List<QuizQuestion> = emptyList()
    private val sessionTopicStats = mutableMapOf<String, Pair<Int, Int>>()
    private val sessionCategoryCorrect = mutableMapOf<QuizCategory, Int>()

    init {
        val loadedProfile = repository.loadProfile()
        soundManager.soundEnabled = loadedProfile.soundEnabled
        soundManager.vibrationEnabled = loadedProfile.vibrationEnabled
        _uiState.update { it.copy(profile = loadedProfile) }

        viewModelScope.launch {
            // Seed DataStore with initial profile if empty, then observe DataStore flow
            repository.statsDataStore.saveProfile(loadedProfile)
            repository.profileFlow.collect { dsProfile ->
                soundManager.soundEnabled = dsProfile.soundEnabled
                soundManager.vibrationEnabled = dsProfile.vibrationEnabled
                _uiState.update { it.copy(profile = dsProfile) }
            }
        }
    }

    fun getLast7DaysCalendar(): List<Pair<String, String>> = repository.getLast7DaysCalendar()

    fun getSevenDayStreakSlots(): List<StreakDaySlot> =
        repository.getSevenDayStreakSlots(_uiState.value.profile.activeDatesIso)

    fun calculateDynamicDifficulty(playerLevel: Int, currentScore: Int): DynamicDifficultyConfig =
        questionGenerator.calculateDynamicDifficulty(playerLevel, currentScore)

    fun isDailyChallengeCompletedToday(): Boolean {
        val today = repository.todayIsoString()
        return _uiState.value.profile.lastDailyPlayedDate == today
    }

    fun todayDateSeed(): String = repository.todayIsoString()

    // ------------------------------------------------------------------------
    // NAVIGATION
    // ------------------------------------------------------------------------
    fun navigateTo(screen: AppScreen) {
        soundManager.playTapFeedback()
        if (screen != AppScreen.PLAYING) {
            timerJob?.cancel()
            answerTransitionJob?.cancel()
        }
        _uiState.update { it.copy(currentScreen = screen, statusBannerMessage = null) }
    }

    fun openModeSetup(mode: GameMode) {
        soundManager.playTapFeedback()
        if (mode == GameMode.DAILY_CHALLENGE) {
            _uiState.update {
                it.copy(
                    pendingMode = GameMode.DAILY_CHALLENGE,
                    currentScreen = AppScreen.DAILY_CHALLENGE_HUB,
                    statusBannerMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    pendingMode = mode,
                    currentScreen = AppScreen.MODE_SETUP,
                    statusBannerMessage = null
                )
            }
        }
    }

    fun selectCategory(category: QuizCategory) {
        soundManager.playTapFeedback()
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectExamDifficulty(difficulty: ExamDifficulty) {
        soundManager.playTapFeedback()
        _uiState.update { it.copy(selectedExamDifficulty = difficulty) }
    }

    // ------------------------------------------------------------------------
    // START GAME MODES (SPRINT, DAILY CHALLENGE, EXAM MODE)
    // ------------------------------------------------------------------------
    fun startSelectedGame() {
        val state = _uiState.value
        startGameSession(
            mode = state.pendingMode,
            category = state.selectedCategory,
            difficulty = state.selectedExamDifficulty
        )
    }

    fun startDailyChallenge() {
        if (isDailyChallengeCompletedToday()) {
            _uiState.update {
                it.copy(statusBannerMessage = "You already completed today's Daily Challenge! Come back tomorrow.")
            }
            return
        }
        startGameSession(
            mode = GameMode.DAILY_CHALLENGE,
            category = QuizCategory.MIXED,
            difficulty = ExamDifficulty.MEDIUM
        )
    }

    private fun startGameSession(
        mode: GameMode,
        category: QuizCategory,
        difficulty: ExamDifficulty
    ) {
        soundManager.playTapFeedback()
        timerJob?.cancel()
        answerTransitionJob?.cancel()
        questionGenerator.resetSession()
        sessionTopicStats.clear()
        sessionCategoryCorrect.clear()

        val playerLevel = _uiState.value.profile.level
        val startBaseLevel = if (mode == GameMode.EXAM_MODE) {
            maxOf(playerLevel, difficulty.baseLevel)
        } else {
            playerLevel
        }
        val initialDiffConfig = questionGenerator.calculateDynamicDifficulty(startBaseLevel, 0)
        val initialQuestion: QuizQuestion
        val totalLimit: Int?

        when (mode) {
            GameMode.SPRINT -> {
                totalLimit = null
                initialQuestion = questionGenerator.nextQuestion(
                    category = category,
                    playerLevel = startBaseLevel,
                    currentScore = 0
                )
            }
            GameMode.DAILY_CHALLENGE -> {
                totalLimit = 10
                val todaySeed = repository.todayIsoString()
                dailyQuestionsQueue = questionGenerator.generateDailyChallengeSet(todaySeed)
                initialQuestion = dailyQuestionsQueue.first()
            }
            GameMode.EXAM_MODE -> {
                totalLimit = 20
                initialQuestion = questionGenerator.nextQuestion(
                    category = category,
                    playerLevel = startBaseLevel,
                    currentScore = 0
                )
            }
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.PLAYING,
                activeGame = ActiveGameState(
                    mode = mode,
                    category = category,
                    difficulty = difficulty,
                    currentQuestion = initialQuestion,
                    questionIndex = 1,
                    totalQuestionsLimit = totalLimit,
                    timeRemainingSeconds = 60,
                    elapsedSeconds = 0,
                    score = 0,
                    comboMultiplier = 1,
                    maxComboReached = 1,
                    correctCount = 0,
                    wrongCount = 0,
                    selectedOptionIndex = null,
                    feedbackState = AnswerFeedbackState.NONE,
                    extraLifeUsed = false,
                    showExtraLifePrompt = false,
                    doubleCoinsUsed = false,
                    dynamicDifficultyLabel = initialDiffConfig.difficultyLabel
                ),
                lastGameSummary = null,
                statusBannerMessage = null
            )
        }

        startSessionTimer(mode)
    }

    private fun startSessionTimer(mode: GameMode) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val active = _uiState.value.activeGame
                if (_uiState.value.currentScreen != AppScreen.PLAYING || active.showExtraLifePrompt) {
                    continue
                }

                if (mode == GameMode.SPRINT) {
                    val nextRemaining = (active.timeRemainingSeconds - 1).coerceAtLeast(0)
                    _uiState.update {
                        it.copy(
                            activeGame = it.activeGame.copy(
                                timeRemainingSeconds = nextRemaining,
                                elapsedSeconds = it.activeGame.elapsedSeconds + 1
                            )
                        )
                    }
                    if (nextRemaining <= 0) {
                        handleSprintTimeUp()
                        break
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            activeGame = it.activeGame.copy(
                                elapsedSeconds = it.activeGame.elapsedSeconds + 1
                            )
                        )
                    }
                }
            }
        }
    }

    private fun handleSprintTimeUp() {
        val active = _uiState.value.activeGame
        if (!active.extraLifeUsed) {
            _uiState.update {
                it.copy(
                    activeGame = it.activeGame.copy(showExtraLifePrompt = true)
                )
            }
        } else {
            finishGameSession()
        }
    }

    // ------------------------------------------------------------------------
    // ANSWER SUBMISSION & DYNAMIC DIFFICULTY PROGRESSION
    // ------------------------------------------------------------------------
    fun submitAnswer(optionIndex: Int) {
        val active = _uiState.value.activeGame
        val question = active.currentQuestion ?: return
        if (active.selectedOptionIndex != null || active.showExtraLifePrompt) return

        val isCorrect = (optionIndex == question.correctIndex)

        val prevTopic = sessionTopicStats[question.subTopic] ?: (0 to 0)
        sessionTopicStats[question.subTopic] =
            (prevTopic.first + if (isCorrect) 1 else 0) to (prevTopic.second + 1)

        if (isCorrect) {
            sessionCategoryCorrect[question.category] =
                (sessionCategoryCorrect[question.category] ?: 0) + 1
        }

        val newCombo = if (isCorrect) {
            (active.comboMultiplier + 1).coerceAtMost(5)
        } else {
            1
        }
        val newMaxCombo = maxOf(active.maxComboReached, newCombo)

        val pointsEarned = if (isCorrect) {
            when (active.mode) {
                GameMode.SPRINT -> 10 * active.comboMultiplier
                GameMode.DAILY_CHALLENGE -> 15
                GameMode.EXAM_MODE -> (10 * active.difficulty.xpMultiplier).toInt()
            }
        } else 0

        val updatedScore = active.score + pointsEarned

        val updatedTimeRemaining = if (active.mode == GameMode.SPRINT) {
            if (isCorrect) {
                (active.timeRemainingSeconds + 2).coerceAtMost(99)
            } else {
                (active.timeRemainingSeconds - 3).coerceAtLeast(0)
            }
        } else {
            active.timeRemainingSeconds
        }

        if (isCorrect) {
            soundManager.playCorrectFeedback(newCombo)
        } else {
            soundManager.playWrongFeedback()
        }

        _uiState.update {
            it.copy(
                activeGame = it.activeGame.copy(
                    selectedOptionIndex = optionIndex,
                    feedbackState = if (isCorrect) AnswerFeedbackState.CORRECT_FLASH else AnswerFeedbackState.WRONG_SHAKE,
                    score = updatedScore,
                    comboMultiplier = newCombo,
                    maxComboReached = newMaxCombo,
                    correctCount = active.correctCount + (if (isCorrect) 1 else 0),
                    wrongCount = active.wrongCount + (if (isCorrect) 0 else 1),
                    timeRemainingSeconds = updatedTimeRemaining
                )
            )
        }

        answerTransitionJob?.cancel()
        answerTransitionJob = viewModelScope.launch {
            delay(380L)
            val latestGame = _uiState.value.activeGame
            if (latestGame.mode == GameMode.SPRINT && latestGame.timeRemainingSeconds <= 0) {
                handleSprintTimeUp()
                return@launch
            }

            val nextIndex = latestGame.questionIndex + 1
            val limit = latestGame.totalQuestionsLimit
            if (limit != null && nextIndex > limit) {
                finishGameSession()
                return@launch
            }

            // Dynamically calculate difficulty from player level + live session score
            val baseLevel = if (latestGame.mode == GameMode.EXAM_MODE) {
                maxOf(_uiState.value.profile.level, latestGame.difficulty.baseLevel)
            } else {
                _uiState.value.profile.level
            }
            val dynamicConfig = questionGenerator.calculateDynamicDifficulty(
                playerLevel = baseLevel,
                currentScore = latestGame.score
            )

            val nextQuestion = if (latestGame.mode == GameMode.DAILY_CHALLENGE) {
                dailyQuestionsQueue.getOrElse(nextIndex - 1) {
                    questionGenerator.nextQuestion(
                        category = QuizCategory.MIXED,
                        playerLevel = baseLevel,
                        currentScore = latestGame.score
                    )
                }
            } else {
                questionGenerator.nextQuestion(
                    category = latestGame.category,
                    playerLevel = baseLevel,
                    currentScore = latestGame.score
                )
            }

            _uiState.update {
                it.copy(
                    activeGame = it.activeGame.copy(
                        currentQuestion = nextQuestion,
                        questionIndex = nextIndex,
                        selectedOptionIndex = null,
                        feedbackState = AnswerFeedbackState.NONE,
                        dynamicDifficultyLabel = dynamicConfig.difficultyLabel
                    )
                )
            }
        }
    }

    // ------------------------------------------------------------------------
    // PLACEHOLDER AD FUNCTIONS: showInterstitialAd() & showRewardedAd(callback)
    // ------------------------------------------------------------------------
    fun showInterstitialAd() {
        _uiState.update { it.copy(showInterstitialAd = true) }
    }

    fun dismissInterstitialAd() {
        soundManager.playTapFeedback()
        _uiState.update { it.copy(showInterstitialAd = false) }
    }

    fun showRewardedAd(
        title: String,
        subtitle: String,
        callback: () -> Unit
    ) {
        soundManager.playTapFeedback()
        _uiState.update {
            it.copy(
                activeRewardedAdRequest = RewardedAdRequest(
                    title = title,
                    subtitle = subtitle,
                    onRewardEarned = {
                        _uiState.update { s -> s.copy(activeRewardedAdRequest = null) }
                        callback()
                    }
                )
            )
        }
    }

    fun cancelRewardedAd() {
        soundManager.playTapFeedback()
        val wasExtraLifePrompt = _uiState.value.activeGame.showExtraLifePrompt
        _uiState.update { it.copy(activeRewardedAdRequest = null) }
        if (wasExtraLifePrompt) {
            declineExtraLifeAndFinish()
        }
    }

    fun requestExtraLifeWithRewardedAd() {
        showRewardedAd(
            title = "Extra Life (+15 Seconds)",
            subtitle = "Watch a short placeholder ad to continue your Sprint with +15s on the clock!"
        ) {
            _uiState.update {
                it.copy(
                    activeGame = it.activeGame.copy(
                        timeRemainingSeconds = 15,
                        extraLifeUsed = true,
                        showExtraLifePrompt = false,
                        selectedOptionIndex = null,
                        feedbackState = AnswerFeedbackState.NONE
                    )
                )
            }
            startSessionTimer(GameMode.SPRINT)
        }
    }

    fun declineExtraLifeAndFinish() {
        _uiState.update {
            it.copy(activeGame = it.activeGame.copy(showExtraLifePrompt = false))
        }
        finishGameSession()
    }

    fun requestDoubleCoinsWithRewardedAd() {
        val summary = _uiState.value.lastGameSummary ?: return
        if (_uiState.value.activeGame.doubleCoinsUsed || summary.coinsEarned <= 0) return

        showRewardedAd(
            title = "Double Coins (2x)",
            subtitle = "Earn +${summary.coinsEarned} bonus coins for this round!"
        ) {
            val bonusCoins = summary.coinsEarned
            val updatedProfile = _uiState.value.profile.copy(
                coins = _uiState.value.profile.coins + bonusCoins
            )
            repository.saveProfile(updatedProfile)
            soundManager.playVictoryFanfare()
            _uiState.update {
                it.copy(
                    profile = updatedProfile,
                    activeGame = it.activeGame.copy(doubleCoinsUsed = true),
                    lastGameSummary = summary.copy(coinsEarned = summary.coinsEarned * 2),
                    statusBannerMessage = "Doubled! +$bonusCoins bonus coins added!"
                )
            }
        }
    }

    // ------------------------------------------------------------------------
    // FINISH GAME & UPDATE PROGRESSION, BADGES, STREAK & LEADERBOARD
    // ------------------------------------------------------------------------
    private fun finishGameSession() {
        timerJob?.cancel()
        answerTransitionJob?.cancel()
        soundManager.playVictoryFanfare()

        val active = _uiState.value.activeGame
        // Record daily activity in local storage so the 7-day streak calendar stays updated
        val profile = repository.recordDailyActivity(_uiState.value.profile)

        val xpEarned = maxOf(10, active.correctCount * 8 + (active.score / 5))
        val coinsEarned = maxOf(5, active.correctCount * 4 + (active.maxComboReached * 3))

        val prevCategoryBest = profile.bestScoresByCategory[active.category.id] ?: 0
        val isNewBest = active.score > prevCategoryBest && active.score > 0
        val updatedBests = profile.bestScoresByCategory.toMutableMap().apply {
            if (active.score > prevCategoryBest) {
                put(active.category.id, active.score)
            }
        }

        val sessionTotalQuestions = active.correctCount + active.wrongCount
        val newTotalGames = profile.totalGamesCompleted + 1
        val todayStr = repository.todayIsoString()

        val profileWithUpdatedMilestones = profile.copy(
            totalGamesCompleted = newTotalGames,
            totalQuestionsAnswered = profile.totalQuestionsAnswered + sessionTotalQuestions,
            totalCorrectAnswers = profile.totalCorrectAnswers + active.correctCount,
            totalMathCorrect = profile.totalMathCorrect + (sessionCategoryCorrect[QuizCategory.MENTAL_MATH] ?: 0),
            totalVocabCorrect = profile.totalVocabCorrect + (sessionCategoryCorrect[QuizCategory.ENGLISH_VOCAB] ?: 0),
            totalScienceCorrect = profile.totalScienceCorrect + (sessionCategoryCorrect[QuizCategory.SCIENCE] ?: 0),
            totalLogicCorrect = profile.totalLogicCorrect + (sessionCategoryCorrect[QuizCategory.LOGIC_PATTERNS] ?: 0),
            lastDailyPlayedDate = if (active.mode == GameMode.DAILY_CHALLENGE) todayStr else profile.lastDailyPlayedDate
        )

        val badgeEval = com.example.domain.BadgeAchievementManager.evaluateAchievements(
            profile = profileWithUpdatedMilestones,
            sessionMode = active.mode,
            sessionMaxCombo = active.maxComboReached,
            sessionExamCorrect = active.correctCount,
            sessionCategoryCorrect = sessionCategoryCorrect
        )
        val updatedBadges = badgeEval.updatedUnlockedBadgeIds
        val newlyUnlocked = badgeEval.newlyUnlockedBadges

        val breakdown = sessionTopicStats.map { (subTopic, pair) ->
            TopicPerformance(subTopic = subTopic, correct = pair.first, total = pair.second)
        }.sortedBy { it.accuracyPercent }

        val weakTopics = breakdown
            .filter { it.total > 0 && it.accuracyPercent < 75 }
            .map { "${it.subTopic} (${it.accuracyPercent}%)" }

        val totalAnswered = sessionTotalQuestions.coerceAtLeast(1)
        val accuracyPct = ((active.correctCount * 100f) / totalAnswered).toInt()

        val dateLabel = SimpleDateFormat("MMM d", Locale.US).format(Date())
        val newEntry = LeaderboardEntry(
            id = UUID.randomUUID().toString().take(8),
            score = active.score,
            categoryId = active.category.id,
            modeName = active.mode.title,
            accuracyPercent = accuracyPct,
            dateLabel = dateLabel,
            avatarId = profile.selectedAvatarId
        )

        val updatedLeaderboard = (profile.leaderboard + newEntry)
            .sortedByDescending { it.score }
            .take(10)

        val updatedProfile = profileWithUpdatedMilestones.copy(
            xp = profile.xp + xpEarned,
            coins = profile.coins + coinsEarned,
            unlockedBadgeIds = updatedBadges,
            bestScoresByCategory = updatedBests,
            leaderboard = updatedLeaderboard,
            lastDailyScore = if (active.mode == GameMode.DAILY_CHALLENGE) active.score else profile.lastDailyScore,
            lastDailyTimeSec = if (active.mode == GameMode.DAILY_CHALLENGE) active.elapsedSeconds else profile.lastDailyTimeSec,
            lastDailyCorrect = if (active.mode == GameMode.DAILY_CHALLENGE) active.correctCount else profile.lastDailyCorrect
        )

        repository.saveProfile(updatedProfile)

        val summary = GameSummary(
            mode = active.mode,
            category = active.category,
            difficulty = active.difficulty,
            finalScore = active.score,
            correctCount = active.correctCount,
            wrongCount = active.wrongCount,
            maxCombo = active.maxComboReached,
            elapsedSeconds = active.elapsedSeconds,
            xpEarned = xpEarned,
            coinsEarned = coinsEarned,
            isNewPersonalBest = isNewBest,
            newlyUnlockedBadges = newlyUnlocked,
            topicBreakdown = breakdown,
            weakTopics = weakTopics,
            dailyDateSeed = todayStr
        )

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME_OVER,
                profile = updatedProfile,
                lastGameSummary = summary
            )
        }

        if (newTotalGames % 3 == 0) {
            showInterstitialAd()
        }
    }

    // ------------------------------------------------------------------------
    // SHOP (THEMES & AVATARS) & SETTINGS
    // ------------------------------------------------------------------------
    fun buyOrSelectTheme(theme: AppThemePreset) {
        val profile = _uiState.value.profile
        if (profile.unlockedThemeIds.contains(theme.id)) {
            soundManager.playTapFeedback()
            val updated = profile.copy(selectedThemeId = theme.id)
            repository.saveProfile(updated)
            viewModelScope.launch { themeManager.setSelectedTheme(theme) }
            _uiState.update {
                it.copy(profile = updated, statusBannerMessage = "Theme '${theme.displayName}' equipped!")
            }
        } else if (profile.coins >= theme.cost) {
            soundManager.playVictoryFanfare()
            val updated = profile.copy(
                coins = profile.coins - theme.cost,
                unlockedThemeIds = profile.unlockedThemeIds + theme.id,
                selectedThemeId = theme.id
            )
            repository.saveProfile(updated)
            viewModelScope.launch { themeManager.unlockOrSelectTheme(theme) }
            _uiState.update {
                it.copy(profile = updated, statusBannerMessage = "Unlocked & equipped '${theme.displayName}'!")
            }
        } else {
            soundManager.playWrongFeedback()
            _uiState.update {
                it.copy(statusBannerMessage = "Need ${theme.cost - profile.coins} more coins for '${theme.displayName}'.")
            }
        }
    }

    fun buyOrSelectAvatar(avatar: AvatarPreset) {
        val profile = _uiState.value.profile
        if (profile.unlockedAvatarIds.contains(avatar.id)) {
            soundManager.playTapFeedback()
            val updated = profile.copy(selectedAvatarId = avatar.id)
            repository.saveProfile(updated)
            _uiState.update {
                it.copy(profile = updated, statusBannerMessage = "Avatar '${avatar.displayName}' equipped!")
            }
        } else if (profile.coins >= avatar.cost) {
            soundManager.playVictoryFanfare()
            val updated = profile.copy(
                coins = profile.coins - avatar.cost,
                unlockedAvatarIds = profile.unlockedAvatarIds + avatar.id,
                selectedAvatarId = avatar.id
            )
            repository.saveProfile(updated)
            _uiState.update {
                it.copy(profile = updated, statusBannerMessage = "Unlocked '${avatar.displayName}'!")
            }
        } else {
            soundManager.playWrongFeedback()
            _uiState.update {
                it.copy(statusBannerMessage = "Need ${avatar.cost - profile.coins} more coins for '${avatar.displayName}'.")
            }
        }
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.soundEnabled = enabled
        if (enabled) soundManager.playTapFeedback()
        val updated = _uiState.value.profile.copy(soundEnabled = enabled)
        repository.saveProfile(updated)
        _uiState.update { it.copy(profile = updated) }
    }

    fun toggleVibration(enabled: Boolean) {
        soundManager.vibrationEnabled = enabled
        if (enabled) soundManager.playTapFeedback()
        val updated = _uiState.value.profile.copy(vibrationEnabled = enabled)
        repository.saveProfile(updated)
        _uiState.update { it.copy(profile = updated) }
    }

    fun resetAllProgress() {
        soundManager.playWrongFeedback()
        val freshProfile = repository.resetAllProgress()
        soundManager.soundEnabled = freshProfile.soundEnabled
        soundManager.vibrationEnabled = freshProfile.vibrationEnabled
        _uiState.update {
            it.copy(
                profile = freshProfile,
                statusBannerMessage = "All progress has been reset to Level 1."
            )
        }
    }
}
