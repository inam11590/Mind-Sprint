package com.example.domain

import androidx.compose.ui.graphics.Color

enum class QuizCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeColor: Color
) {
    MENTAL_MATH(
        id = "mental_math",
        title = "Mental Math",
        subtitle = "Arithmetic, squares, percentages & speed calc",
        badgeColor = Color(0xFF38BDF8)
    ),
    ENGLISH_VOCAB(
        id = "english_vocab",
        title = "English Vocabulary",
        subtitle = "300+ words: synonyms, antonyms & meanings",
        badgeColor = Color(0xFFF472B6)
    ),
    SCIENCE(
        id = "science",
        title = "Science",
        subtitle = "200+ Physics, Chemistry, Biology & Space facts",
        badgeColor = Color(0xFF34D399)
    ),
    LOGIC_PATTERNS(
        id = "logic_patterns",
        title = "Logic Patterns",
        subtitle = "Number sequences, progressions & brain puzzles",
        badgeColor = Color(0xFFFBBF24)
    ),
    MIXED(
        id = "mixed",
        title = "Mixed All-Stars",
        subtitle = "Ultimate blend of all 4 brain sprint categories",
        badgeColor = Color(0xFFA78BFA)
    );

    companion object {
        fun fromId(id: String): QuizCategory =
            entries.find { it.id == id } ?: MIXED
    }
}

enum class GameMode(val title: String) {
    SPRINT("Sprint Mode"),
    DAILY_CHALLENGE("Daily Challenge"),
    EXAM_MODE("Exam Mode")
}

enum class ExamDifficulty(val label: String, val baseLevel: Int, val xpMultiplier: Float) {
    EASY("Foundation (Easy)", 1, 1.0f),
    MEDIUM("Varsity (Medium)", 4, 1.35f),
    HARD("Olympiad (Hard)", 8, 1.75f)
}

data class QuizQuestion(
    val signature: String,
    val category: QuizCategory,
    val subTopic: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)

// 8 Unlockable SVG/Canvas Avatars
enum class AvatarPreset(
    val id: String,
    val displayName: String,
    val titleTag: String,
    val cost: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    ASTRO_OWL(
        id = "astro_owl",
        displayName = "Professor Hoot",
        titleTag = "Wisdom Seeker",
        cost = 0,
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFFA5B4FC),
        accentColor = Color(0xFFFBBF24)
    ),
    VOLT_FOX(
        id = "volt_fox",
        displayName = "Blaze Fox",
        titleTag = "Speed Sprinter",
        cost = 100,
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFFED7AA),
        accentColor = Color(0xFF38BDF8)
    ),
    CYBER_BOT(
        id = "cyber_bot",
        displayName = "Nano Bot X9",
        titleTag = "Calculator Core",
        cost = 150,
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF67E8F9),
        accentColor = Color(0xFFF43F5E)
    ),
    NEON_CAT(
        id = "neon_cat",
        displayName = "Pixel Whiskers",
        titleTag = "Night Scholar",
        cost = 200,
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFFFBCFE8),
        accentColor = Color(0xFFA3E635)
    ),
    QUANTUM_DRAGON(
        id = "quantum_dragon",
        displayName = "Draco Nova",
        titleTag = "Mythic Genius",
        cost = 250,
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFA7F3D0),
        accentColor = Color(0xFFFACC15)
    ),
    ZEN_PANDA(
        id = "zen_panda",
        displayName = "Master Bamboo",
        titleTag = "Logic Monk",
        cost = 300,
        primaryColor = Color(0xFF14B8A6),
        secondaryColor = Color(0xFFF8FAFC),
        accentColor = Color(0xFFFB7185)
    ),
    STAR_UNICORN(
        id = "star_unicorn",
        displayName = "Celestia Horn",
        titleTag = "Astral Prodigy",
        cost = 350,
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFFDDD6FE),
        accentColor = Color(0xFFFDE047)
    ),
    CROWN_LION(
        id = "crown_lion",
        displayName = "Apex Leo",
        titleTag = "Grand Champion",
        cost = 450,
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFFEF08A),
        accentColor = Color(0xFFEF4444)
    );

    companion object {
        fun fromId(id: String): AvatarPreset =
            entries.find { it.id == id } ?: ASTRO_OWL
    }
}

enum class BadgeInfo(
    val id: String,
    val title: String,
    val description: String,
    val targetMilestone: Int,
    val unitLabel: String,
    val accentColor: Color
) {
    FIRST_SPRINT(
        id = "first_sprint",
        title = "First Steps",
        description = "Complete your first game in any mode",
        targetMilestone = 1,
        unitLabel = "game",
        accentColor = Color(0xFF38BDF8)
    ),
    CENTURY_SCHOLAR(
        id = "century_scholar",
        title = "100 Questions Answered",
        description = "Answer 100 total questions across any game modes",
        targetMilestone = 100,
        unitLabel = "questions",
        accentColor = Color(0xFFF59E0B)
    ),
    MATH_MASTER(
        id = "math_master",
        title = "Math Master",
        description = "Answer 15 Mental Math questions right in one game (or 50 lifetime)",
        targetMilestone = 50,
        unitLabel = "math correct",
        accentColor = Color(0xFF06B6D4)
    ),
    SPEED_DEMON(
        id = "speed_demon",
        title = "Speed Demon",
        description = "Reach the maximum x5 Combo Multiplier in Sprint Mode",
        targetMilestone = 5,
        unitLabel = "max combo",
        accentColor = Color(0xFFF97316)
    ),
    VOCAB_VIRTUOSO(
        id = "vocab_virtuoso",
        title = "Word Wizard",
        description = "Answer 15 English Vocabulary questions right in one game (or 50 lifetime)",
        targetMilestone = 50,
        unitLabel = "vocab correct",
        accentColor = Color(0xFFF472B6)
    ),
    SCIENCE_WHIZ(
        id = "science_whiz",
        title = "Science Whiz",
        description = "Answer 15 Science questions right in one game (or 50 lifetime)",
        targetMilestone = 50,
        unitLabel = "science correct",
        accentColor = Color(0xFF10B981)
    ),
    LOGIC_PROPHET(
        id = "logic_prophet",
        title = "Pattern Prophet",
        description = "Answer 15 Logic Pattern questions right in one game (or 50 lifetime)",
        targetMilestone = 50,
        unitLabel = "logic correct",
        accentColor = Color(0xFFFBBF24)
    ),
    STREAK_7_DAYS(
        id = "streak_7_days",
        title = "7-Day Streak",
        description = "Maintain a 7-day learning streak",
        targetMilestone = 7,
        unitLabel = "days",
        accentColor = Color(0xFFEF4444)
    ),
    EXAM_ACE(
        id = "exam_ace",
        title = "Exam Ace",
        description = "Score 90% or higher (18/20+) in Exam Mode",
        targetMilestone = 18,
        unitLabel = "exam score",
        accentColor = Color(0xFFA78BFA)
    ),
    DAILY_CHAMPION(
        id = "daily_champion",
        title = "Daily Challenger",
        description = "Complete a Daily Challenge",
        targetMilestone = 1,
        unitLabel = "daily challenge",
        accentColor = Color(0xFF34D399)
    )
}

data class LeaderboardEntry(
    val id: String,
    val score: Int,
    val categoryId: String,
    val modeName: String,
    val accuracyPercent: Int,
    val dateLabel: String,
    val avatarId: String
)

data class TopicPerformance(
    val subTopic: String,
    val correct: Int,
    val total: Int
) {
    val accuracyPercent: Int
        get() = if (total == 0) 100 else ((correct * 100f) / total).toInt()
}

data class GameSummary(
    val mode: GameMode,
    val category: QuizCategory,
    val difficulty: ExamDifficulty,
    val finalScore: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val maxCombo: Int,
    val elapsedSeconds: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val isNewPersonalBest: Boolean,
    val newlyUnlockedBadges: List<BadgeInfo>,
    val topicBreakdown: List<TopicPerformance>,
    val weakTopics: List<String>,
    val dailyDateSeed: String = ""
)
