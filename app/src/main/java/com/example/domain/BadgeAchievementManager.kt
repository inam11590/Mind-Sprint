package com.example.domain

import com.example.data.PlayerProfileState

/**
 * UI-ready progress snapshot for a single achievement badge in the Profile/Badges section.
 */
data class BadgeProgressItem(
    val badge: BadgeInfo,
    val isEarned: Boolean,
    val currentProgress: Int,
    val targetMilestone: Int,
    val progressLabel: String
) {
    val progressFraction: Float
        get() = if (isEarned) {
            1f
        } else {
            (currentProgress.toFloat() / targetMilestone.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        }
}

/**
 * Result of evaluating badge achievements after a game session or milestone update.
 */
data class BadgeEvaluationResult(
    val updatedUnlockedBadgeIds: Set<String>,
    val newlyUnlockedBadges: List<BadgeInfo>
)

/**
 * Achievement Engine that tracks cumulative and single-game milestones such as
 * '100 Questions Answered' (CENTURY_SCHOLAR), 'Math Master' (MATH_MASTER),
 * 'Speed Demon', and '7-Day Streak'.
 */
object BadgeAchievementManager {

    /**
     * Evaluates all badge unlock criteria against the updated [profile] and optional
     * session statistics, returning the full set of unlocked badge IDs and any newly earned badges.
     */
    fun evaluateAchievements(
        profile: PlayerProfileState,
        sessionMode: GameMode? = null,
        sessionMaxCombo: Int = 0,
        sessionExamCorrect: Int = 0,
        sessionCategoryCorrect: Map<QuizCategory, Int> = emptyMap()
    ): BadgeEvaluationResult {
        val unlockedIds = profile.unlockedBadgeIds.toMutableSet()
        val newlyEarned = mutableListOf<BadgeInfo>()

        fun unlock(badge: BadgeInfo) {
            if (unlockedIds.add(badge.id)) {
                newlyEarned.add(badge)
            }
        }

        // 1. First Steps (1+ games completed)
        if (profile.totalGamesCompleted >= 1 || sessionMode != null) {
            unlock(BadgeInfo.FIRST_SPRINT)
        }

        // 2. 100 Questions Answered milestone
        if (profile.totalQuestionsAnswered >= BadgeInfo.CENTURY_SCHOLAR.targetMilestone) {
            unlock(BadgeInfo.CENTURY_SCHOLAR)
        }

        // 3. Math Master (15+ in one session OR 50+ lifetime math correct)
        val sessionMath = sessionCategoryCorrect[QuizCategory.MENTAL_MATH] ?: 0
        if (sessionMath >= 15 || profile.totalMathCorrect >= BadgeInfo.MATH_MASTER.targetMilestone) {
            unlock(BadgeInfo.MATH_MASTER)
        }

        // 4. Speed Demon (x5 Combo reached)
        if (sessionMaxCombo >= 5) {
            unlock(BadgeInfo.SPEED_DEMON)
        }

        // 5. Word Wizard (15+ in one session OR 50+ lifetime vocab correct)
        val sessionVocab = sessionCategoryCorrect[QuizCategory.ENGLISH_VOCAB] ?: 0
        if (sessionVocab >= 15 || profile.totalVocabCorrect >= BadgeInfo.VOCAB_VIRTUOSO.targetMilestone) {
            unlock(BadgeInfo.VOCAB_VIRTUOSO)
        }

        // 6. Science Whiz (15+ in one session OR 50+ lifetime science correct)
        val sessionScience = sessionCategoryCorrect[QuizCategory.SCIENCE] ?: 0
        if (sessionScience >= 15 || profile.totalScienceCorrect >= BadgeInfo.SCIENCE_WHIZ.targetMilestone) {
            unlock(BadgeInfo.SCIENCE_WHIZ)
        }

        // 7. Pattern Prophet (15+ in one session OR 50+ lifetime logic correct)
        val sessionLogic = sessionCategoryCorrect[QuizCategory.LOGIC_PATTERNS] ?: 0
        if (sessionLogic >= 15 || profile.totalLogicCorrect >= BadgeInfo.LOGIC_PROPHET.targetMilestone) {
            unlock(BadgeInfo.LOGIC_PROPHET)
        }

        // 8. 7-Day Streak
        if (profile.currentStreak >= 7 || profile.longestStreak >= 7) {
            unlock(BadgeInfo.STREAK_7_DAYS)
        }

        // 9. Exam Ace (18/20+ in Exam Mode)
        if (sessionMode == GameMode.EXAM_MODE && sessionExamCorrect >= 18) {
            unlock(BadgeInfo.EXAM_ACE)
        }

        // 10. Daily Challenger
        if (sessionMode == GameMode.DAILY_CHALLENGE || profile.lastDailyPlayedDate.isNotBlank()) {
            unlock(BadgeInfo.DAILY_CHAMPION)
        }

        return BadgeEvaluationResult(
            updatedUnlockedBadgeIds = unlockedIds,
            newlyUnlockedBadges = newlyEarned
        )
    }

    /**
     * Builds the list of [BadgeProgressItem] entries for the dedicated 'Profile/Badges' screen.
     */
    fun buildBadgeProgressList(profile: PlayerProfileState): List<BadgeProgressItem> {
        return BadgeInfo.entries.map { badge ->
            val isEarned = profile.unlockedBadgeIds.contains(badge.id)
            val currentVal = when (badge) {
                BadgeInfo.FIRST_SPRINT -> profile.totalGamesCompleted.coerceAtMost(1)
                BadgeInfo.CENTURY_SCHOLAR -> profile.totalQuestionsAnswered.coerceAtMost(badge.targetMilestone)
                BadgeInfo.MATH_MASTER -> if (isEarned) badge.targetMilestone else profile.totalMathCorrect.coerceAtMost(badge.targetMilestone)
                BadgeInfo.SPEED_DEMON -> if (isEarned) 5 else 1
                BadgeInfo.VOCAB_VIRTUOSO -> if (isEarned) badge.targetMilestone else profile.totalVocabCorrect.coerceAtMost(badge.targetMilestone)
                BadgeInfo.SCIENCE_WHIZ -> if (isEarned) badge.targetMilestone else profile.totalScienceCorrect.coerceAtMost(badge.targetMilestone)
                BadgeInfo.LOGIC_PROPHET -> if (isEarned) badge.targetMilestone else profile.totalLogicCorrect.coerceAtMost(badge.targetMilestone)
                BadgeInfo.STREAK_7_DAYS -> maxOf(profile.currentStreak, profile.longestStreak).coerceAtMost(7)
                BadgeInfo.EXAM_ACE -> if (isEarned) 18 else 0
                BadgeInfo.DAILY_CHAMPION -> if (isEarned || profile.lastDailyPlayedDate.isNotBlank()) 1 else 0
            }
            val label = if (isEarned) {
                "Earned ✓"
            } else {
                "$currentVal / ${badge.targetMilestone} ${badge.unitLabel}"
            }
            BadgeProgressItem(
                badge = badge,
                isEarned = isEarned,
                currentProgress = currentVal,
                targetMilestone = badge.targetMilestone,
                progressLabel = label
            )
        }
    }
}
