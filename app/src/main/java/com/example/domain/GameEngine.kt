package com.example.domain

import java.util.Random
import kotlin.math.abs
import kotlin.math.max

/**
 * Result of evaluating a player's answer in a game session.
 */
data class AnswerEvaluation(
    val isCorrect: Boolean,
    val pointsAwarded: Int,
    val newScore: Int,
    val newComboMultiplier: Int,
    val timeDeltaSeconds: Int,
    val updatedDifficulty: DynamicDifficultyConfig
)

/**
 * Core Game Engine for Mind Sprint.
 * Defines the infinite procedural and bank-backed question generation across all four categories
 * (Mental Math, English Vocabulary, Science, and Logic Patterns), dynamic difficulty scaling,
 * believable distractor generation, answer randomization, and session deduplication.
 */
open class GameEngine {

    private val sessionUsedSignatures = mutableSetOf<String>()

    /**
     * Clears the set of seen question signatures when starting a new game session.
     */
    fun resetSession() {
        sessionUsedSignatures.clear()
    }

    /**
     * Dynamically calculates player difficulty based on their current level and live session score.
     * Adjusts math operand bounds, multi-step operation availability, sequence complexity,
     * and vocabulary rarity tier as the score increases during a session.
     */
    fun calculateDynamicDifficulty(
        playerLevel: Int,
        currentScore: Int
    ): DynamicDifficultyConfig {
        val safePlayerLevel = playerLevel.coerceAtLeast(1)
        val safeScore = currentScore.coerceAtLeast(0)

        // Every 30 points scored within a session boosts effective difficulty by +1 level
        val sessionScoreBoost = safeScore / 30
        val effectiveLevel = (safePlayerLevel + sessionScoreBoost).coerceIn(1, 20)

        val mathMin = when {
            effectiveLevel <= 1 -> 2
            effectiveLevel <= 3 -> 6 * effectiveLevel
            else -> 10 * effectiveLevel
        }
        val mathMax = when {
            effectiveLevel <= 1 -> 9 // Single-digit operands at Level 1 with 0 score
            effectiveLevel <= 3 -> 18 * effectiveLevel
            else -> (25 * effectiveLevel).coerceAtMost(350)
        }

        val mulFactorAMax = if (effectiveLevel == 1) 9 else (7 + effectiveLevel * 2).coerceAtMost(28)
        val mulFactorBMax = if (effectiveLevel == 1) 9 else (6 + effectiveLevel).coerceAtMost(18)
        val divisorMax = (4 + effectiveLevel).coerceAtMost(16)
        val quotientMax = (5 + effectiveLevel * 2).coerceAtMost(25)
        val squareMaxBase = (5 + effectiveLevel * 2).coerceAtMost(30)

        val vocabRarity = when {
            effectiveLevel <= 2 -> VocabRarity.COMMON
            effectiveLevel <= 5 -> VocabRarity.INTERMEDIATE
            else -> VocabRarity.ADVANCED
        }

        val difficultyLabel = when {
            effectiveLevel <= 2 -> "Lv.$effectiveLevel • Starter"
            effectiveLevel <= 5 -> "Lv.$effectiveLevel • Challenger"
            effectiveLevel <= 8 -> "Lv.$effectiveLevel • Varsity"
            else -> "Lv.$effectiveLevel • Grandmaster"
        }

        return DynamicDifficultyConfig(
            playerLevel = safePlayerLevel,
            currentScore = safeScore,
            effectiveLevel = effectiveLevel,
            difficultyLabel = difficultyLabel,
            mathAddSubMin = mathMin,
            mathAddSubMax = max(mathMin + 4, mathMax),
            mathMulFactorAMax = mulFactorAMax,
            mathMulFactorBMax = mulFactorFactorBMax(mulFactorBMax),
            mathDivisorMax = divisorMax,
            mathQuotientMax = quotientMax,
            mathSquareMaxBase = squareMaxBase,
            allowMultiStepMath = effectiveLevel >= 4,
            targetVocabRarity = vocabRarity
        )
    }

    private fun mulFactorFactorBMax(value: Int): Int = value.coerceAtLeast(3)

    /**
     * Evaluates a submitted answer, computing combo multiplier transitions (x1..x5),
     * Sprint timer adjustments (+2s for correct, -3s for wrong), score increments,
     * and the updated dynamic difficulty configuration.
     */
    fun evaluateAnswer(
        mode: GameMode,
        difficulty: ExamDifficulty,
        isCorrect: Boolean,
        currentScore: Int,
        currentCombo: Int,
        playerLevel: Int
    ): AnswerEvaluation {
        val nextCombo = if (isCorrect) (currentCombo + 1).coerceAtMost(5) else 1
        val pointsAwarded = if (isCorrect) {
            when (mode) {
                GameMode.SPRINT -> 10 * currentCombo
                GameMode.DAILY_CHALLENGE -> 15
                GameMode.EXAM_MODE -> (10 * difficulty.xpMultiplier).toInt()
            }
        } else {
            0
        }
        val nextScore = currentScore + pointsAwarded
        val timeDelta = if (mode == GameMode.SPRINT) {
            if (isCorrect) 2 else -3
        } else {
            0
        }
        val baseLevel = if (mode == GameMode.EXAM_MODE) {
            maxOf(playerLevel, difficulty.baseLevel)
        } else {
            playerLevel
        }
        val updatedDiff = calculateDynamicDifficulty(baseLevel, nextScore)

        return AnswerEvaluation(
            isCorrect = isCorrect,
            pointsAwarded = pointsAwarded,
            newScore = nextScore,
            newComboMultiplier = nextCombo,
            timeDeltaSeconds = timeDelta,
            updatedDifficulty = updatedDiff
        )
    }

    /**
     * Generates the next non-repeating question for the specified [category],
     * dynamically scaled by [playerLevel] and [currentScore].
     */
    fun nextQuestion(
        category: QuizCategory,
        playerLevel: Int,
        currentScore: Int = 0,
        random: Random = Random()
    ): QuizQuestion {
        val diffConfig = calculateDynamicDifficulty(playerLevel, currentScore)

        val resolvedCategory = if (category == QuizCategory.MIXED) {
            val coreCategories = listOf(
                QuizCategory.MENTAL_MATH,
                QuizCategory.ENGLISH_VOCAB,
                QuizCategory.SCIENCE,
                QuizCategory.LOGIC_PATTERNS
            )
            coreCategories[random.nextInt(coreCategories.size)]
        } else {
            category
        }

        // Attempt up to 60 times to guarantee no duplicate question within the active session
        repeat(60) {
            val candidate = when (resolvedCategory) {
                QuizCategory.MENTAL_MATH -> generateMentalMathQuestion(diffConfig, random)
                QuizCategory.ENGLISH_VOCAB -> generateVocabQuestion(diffConfig, random)
                QuizCategory.SCIENCE -> generateScienceQuestion(diffConfig, random)
                QuizCategory.LOGIC_PATTERNS -> generateLogicPatternQuestion(diffConfig, random)
                QuizCategory.MIXED -> generateMentalMathQuestion(diffConfig, random)
            }
            if (sessionUsedSignatures.add(candidate.signature)) {
                return candidate
            }
        }

        // Fallback with boosted difficulty if session has exhausted many questions
        val boostedConfig = calculateDynamicDifficulty(playerLevel + 1, currentScore + 30)
        val fallback = generateMentalMathQuestion(boostedConfig, random)
        sessionUsedSignatures.add(fallback.signature)
        return fallback
    }

    /**
     * Generates a deterministic 10-question Daily Challenge set seeded by [dateSeedStr] (YYYY-MM-DD).
     */
    fun generateDailyChallengeSet(dateSeedStr: String): List<QuizQuestion> {
        resetSession()
        var hash = 2166136261L
        for (ch in dateSeedStr) {
            hash = (hash xor ch.code.toLong()) * 16777619L
        }
        val seededRandom = Random(hash)
        val categoriesCycle = listOf(
            QuizCategory.MENTAL_MATH,
            QuizCategory.ENGLISH_VOCAB,
            QuizCategory.SCIENCE,
            QuizCategory.LOGIC_PATTERNS,
            QuizCategory.MENTAL_MATH,
            QuizCategory.ENGLISH_VOCAB,
            QuizCategory.SCIENCE,
            QuizCategory.LOGIC_PATTERNS,
            QuizCategory.MENTAL_MATH,
            QuizCategory.SCIENCE
        )
        return categoriesCycle.mapIndexed { index, cat ->
            val simulatedScore = index * 25
            nextQuestion(
                category = cat,
                playerLevel = 2,
                currentScore = simulatedScore,
                random = seededRandom
            )
        }
    }

    // ------------------------------------------------------------------------
    // 1. MENTAL MATH PROCEDURAL GENERATOR
    // ------------------------------------------------------------------------
    fun generateMentalMathQuestion(
        config: DynamicDifficultyConfig,
        random: Random = Random()
    ): QuizQuestion {
        val availableOps = when {
            config.effectiveLevel <= 1 -> listOf("ADD", "SUB", "MUL")
            !config.allowMultiStepMath -> listOf("ADD", "SUB", "MUL", "DIV", "SQUARE")
            else -> listOf("ADD", "SUB", "MUL", "DIV", "SQUARE", "PERCENT", "MIXED_OP")
        }
        val op = availableOps[random.nextInt(availableOps.size)]

        val subTopic: String
        val prompt: String
        val correctVal: Int
        val explanation: String

        when (op) {
            "ADD" -> {
                subTopic = "Addition"
                val span = max(2, config.mathAddSubMax - config.mathAddSubMin + 1)
                val a = config.mathAddSubMin + random.nextInt(span)
                val b = config.mathAddSubMin + random.nextInt(span)
                correctVal = a + b
                prompt = "$a + $b = ?"
                explanation = "$a plus $b equals $correctVal"
            }
            "SUB" -> {
                subTopic = "Subtraction"
                val span = max(3, (config.mathAddSubMax - config.mathAddSubMin) / 2 + 2)
                val b = config.mathAddSubMin + random.nextInt(span)
                val diff = config.mathAddSubMin + random.nextInt(span)
                val a = b + diff
                correctVal = diff
                prompt = "$a − $b = ?"
                explanation = "$a minus $b equals $correctVal"
            }
            "MUL" -> {
                subTopic = "Multiplication"
                val a = 2 + random.nextInt(max(2, config.mathMulFactorAMax - 1))
                val b = 2 + random.nextInt(max(2, config.mathMulFactorBMax - 1))
                correctVal = a * b
                prompt = "$a × $b = ?"
                explanation = "$a multiplied by $b equals $correctVal"
            }
            "DIV" -> {
                subTopic = "Division"
                val divisor = 2 + random.nextInt(max(2, config.mathDivisorMax - 1))
                val quotient = 2 + random.nextInt(max(2, config.mathQuotientMax - 1))
                val dividend = divisor * quotient
                correctVal = quotient
                prompt = "$dividend ÷ $divisor = ?"
                explanation = "$dividend divided by $divisor is $correctVal"
            }
            "SQUARE" -> {
                subTopic = "Squares & Powers"
                val base = 3 + random.nextInt(max(3, config.mathSquareMaxBase - 2))
                correctVal = base * base
                prompt = "$base² = ?"
                explanation = "$base × $base = $correctVal"
            }
            "PERCENT" -> {
                subTopic = "Percentages"
                val percents = if (config.effectiveLevel >= 6) {
                    listOf(10, 15, 20, 25, 30, 35, 40, 50, 60, 75, 80)
                } else {
                    listOf(10, 20, 25, 50, 75)
                }
                val pct = percents[random.nextInt(percents.size)]
                val multiplier = 1 + random.nextInt((2 + config.effectiveLevel).coerceAtMost(15))
                val base = 20 * multiplier
                correctVal = (base * pct) / 100
                prompt = "$pct% of $base = ?"
                explanation = "$pct% × $base = $correctVal"
            }
            else -> {
                subTopic = "Mixed Operations"
                val a = 2 + random.nextInt((4 + config.effectiveLevel).coerceAtMost(14))
                val b = 2 + random.nextInt((4 + config.effectiveLevel).coerceAtMost(12))
                val c = 3 + random.nextInt((5 + config.effectiveLevel * 2).coerceAtMost(30))
                if (random.nextBoolean()) {
                    correctVal = (a * b) + c
                    prompt = "($a × $b) + $c = ?"
                    explanation = "$a × $b = ${a * b}, plus $c = $correctVal"
                } else {
                    val prod = a * b
                    val sub = c.coerceAtMost(prod - 1).coerceAtLeast(1)
                    correctVal = prod - sub
                    prompt = "($a × $b) − $sub = ?"
                    explanation = "$a × $b = $prod, minus $sub = $correctVal"
                }
            }
        }

        val (options, correctIdx) = buildNumericOptionsWithIndex(correctVal, random)

        return QuizQuestion(
            signature = "MATH:$prompt",
            category = QuizCategory.MENTAL_MATH,
            subTopic = subTopic,
            questionText = prompt,
            options = options,
            correctIndex = correctIdx,
            explanation = explanation
        )
    }

    // ------------------------------------------------------------------------
    // 2. ENGLISH VOCABULARY GENERATOR (Rarity-Scaled + Shuffled Options)
    // ------------------------------------------------------------------------
    fun generateVocabQuestion(
        config: DynamicDifficultyConfig,
        random: Random = Random()
    ): QuizQuestion {
        val rarityPool = VocabBank.wordsForRarity(config.targetVocabRarity)
        val targetIndex = random.nextInt(rarityPool.size)
        val target = rarityPool[targetIndex]
        val questionType = random.nextInt(3) // 0 = Synonym, 1 = Antonym, 2 = Meaning -> Word

        val distractorItems = mutableListOf<VocabItem>()
        var attempts = 0
        while (distractorItems.size < 3 && attempts < 80) {
            attempts++
            val candidate = rarityPool[random.nextInt(rarityPool.size)]
            if (candidate.word != target.word && distractorItems.none { it.word == candidate.word }) {
                val valid = when (questionType) {
                    0 -> candidate.synonym != target.synonym && candidate.synonym != target.word
                    1 -> candidate.antonym != target.antonym && candidate.antonym != target.word
                    else -> true
                }
                if (valid) distractorItems.add(candidate)
            }
        }
        while (distractorItems.size < 3) {
            val candidate = VocabBank.words[random.nextInt(VocabBank.words.size)]
            if (candidate.word != target.word && distractorItems.none { it.word == candidate.word }) {
                distractorItems.add(candidate)
            }
        }

        val rarityBadge = config.targetVocabRarity.label
        val subTopic: String
        val prompt: String
        val correctAnswer: String
        val wrongAnswers: List<String>

        when (questionType) {
            0 -> {
                subTopic = "Synonyms ($rarityBadge)"
                prompt = "What is a SYNONYM of \"${target.word}\"?"
                correctAnswer = target.synonym
                wrongAnswers = distractorItems.map { it.synonym }
            }
            1 -> {
                subTopic = "Antonyms ($rarityBadge)"
                prompt = "What is the OPPOSITE of \"${target.word}\"?"
                correctAnswer = target.antonym
                wrongAnswers = distractorItems.map { it.antonym }
            }
            else -> {
                subTopic = "Word Meanings ($rarityBadge)"
                prompt = "Which word means:\n\"${target.meaning}\"?"
                correctAnswer = target.word
                wrongAnswers = distractorItems.map { it.word }
            }
        }

        val (shuffledOptions, correctIdx) = randomizeOptionsWithCorrectIndex(
            correctAnswer = correctAnswer,
            wrongAnswers = wrongAnswers,
            random = random
        )

        return QuizQuestion(
            signature = "VOCAB:$questionType:${target.word}",
            category = QuizCategory.ENGLISH_VOCAB,
            subTopic = subTopic,
            questionText = prompt,
            options = shuffledOptions,
            correctIndex = correctIdx,
            explanation = "${target.word}: ${target.meaning} (Synonym: ${target.synonym}, Opposite: ${target.antonym})"
        )
    }

    // ------------------------------------------------------------------------
    // 3. SCIENCE GENERATOR (212 Questions + Randomized Answer Order)
    // ------------------------------------------------------------------------
    fun generateScienceQuestion(
        config: DynamicDifficultyConfig,
        random: Random = Random()
    ): QuizQuestion {
        val bank = ScienceBank.questions
        val raw = bank[random.nextInt(bank.size)]
        val (shuffledOptions, correctIdx) = randomizeOptionsWithCorrectIndex(
            correctAnswer = raw.correctAnswer,
            wrongAnswers = raw.wrongAnswers.take(3),
            random = random
        )

        return QuizQuestion(
            signature = "SCI:${raw.prompt}",
            category = QuizCategory.SCIENCE,
            subTopic = raw.subTopic,
            questionText = raw.prompt,
            options = shuffledOptions,
            correctIndex = correctIdx,
            explanation = "Correct Answer: ${raw.correctAnswer}"
        )
    }

    // ------------------------------------------------------------------------
    // 4. LOGIC PATTERNS PROCEDURAL GENERATOR (Difficulty-Scaled Sequences)
    // ------------------------------------------------------------------------
    fun generateLogicPatternQuestion(
        config: DynamicDifficultyConfig,
        random: Random = Random()
    ): QuizQuestion {
        val clampedLevel = config.effectiveLevel.coerceIn(1, 15)
        val patternTypes = if (clampedLevel <= 2) {
            listOf("ARITHMETIC", "GEOMETRIC", "SQUARES", "STEP_DIFF")
        } else {
            listOf("ARITHMETIC", "GEOMETRIC", "SQUARES", "STEP_DIFF", "ALTERNATING", "FIBONACCI_LIKE")
        }
        val type = patternTypes[random.nextInt(patternTypes.size)]

        val subTopic: String
        val terms: List<Int>
        val nextVal: Int
        val ruleDesc: String

        when (type) {
            "ARITHMETIC" -> {
                subTopic = "Arithmetic Sequences"
                val start = 1 + random.nextInt(15 + clampedLevel * 2)
                val step = 2 + random.nextInt(4 + clampedLevel * 2)
                val isIncreasing = random.nextInt(4) != 0
                if (isIncreasing) {
                    terms = (0..4).map { start + it * step }
                    nextVal = start + 5 * step
                    ruleDesc = "Add $step each step"
                } else {
                    val highStart = start + 6 * step
                    terms = (0..4).map { highStart - it * step }
                    nextVal = highStart - 5 * step
                    ruleDesc = "Subtract $step each step"
                }
            }
            "GEOMETRIC" -> {
                subTopic = "Geometric Sequences"
                val ratio = if (random.nextInt(3) == 0 && clampedLevel >= 3) 3 else 2
                val start = 1 + random.nextInt(if (ratio == 3) 4 else 7)
                var curr = start
                val list = mutableListOf<Int>()
                repeat(5) {
                    list.add(curr)
                    curr *= ratio
                }
                terms = list
                nextVal = curr
                ruleDesc = "Multiply by $ratio each step"
            }
            "SQUARES" -> {
                subTopic = "Square Patterns"
                val offset = if (clampedLevel >= 3) random.nextInt(3) else 0
                val startBase = 1 + random.nextInt(5)
                terms = (0..4).map {
                    val n = startBase + it
                    n * n + offset
                }
                val nextN = startBase + 5
                nextVal = nextN * nextN + offset
                ruleDesc = if (offset == 0) "Consecutive squares (n²)" else "n² + $offset"
            }
            "STEP_DIFF" -> {
                subTopic = "Growing Differences"
                val start = 1 + random.nextInt(10)
                val stepIncrement = 1 + random.nextInt(3)
                val list = mutableListOf(start)
                var curr = start
                for (i in 1..4) {
                    curr += i * stepIncrement
                    list.add(curr)
                }
                terms = list
                nextVal = curr + 5 * stepIncrement
                ruleDesc = "Difference increases by +$stepIncrement each time"
            }
            "ALTERNATING" -> {
                subTopic = "Alternating Patterns"
                val start = 2 + random.nextInt(12)
                val addA = 3 + random.nextInt(6)
                val subB = 1 + random.nextInt((addA - 1).coerceAtLeast(1))
                val list = mutableListOf(start)
                var curr = start
                for (i in 0 until 4) {
                    curr = if (i % 2 == 0) curr + addA else curr - subB
                    list.add(curr)
                }
                terms = list
                nextVal = curr + addA
                ruleDesc = "Alternating +$addA, −$subB"
            }
            else -> {
                subTopic = "Sum Sequences"
                val a = 1 + random.nextInt(5)
                val b = 2 + random.nextInt(6)
                val list = mutableListOf(a, b)
                for (i in 2..4) {
                    list.add(list[i - 1] + list[i - 2])
                }
                terms = list
                nextVal = list[4] + list[3]
                ruleDesc = "Each number is the sum of the previous two"
            }
        }

        val seqStr = terms.joinToString(", ")
        val prompt = "Find the next number:\n$seqStr, ?"
        val (options, correctIdx) = buildNumericOptionsWithIndex(nextVal, random)

        return QuizQuestion(
            signature = "LOGIC:$seqStr->$nextVal",
            category = QuizCategory.LOGIC_PATTERNS,
            subTopic = subTopic,
            questionText = prompt,
            options = options,
            correctIndex = correctIdx,
            explanation = "$ruleDesc → Next is $nextVal"
        )
    }

    // ------------------------------------------------------------------------
    // ANSWER RANDOMIZATION & BELIEVABLE DISTRACTOR HELPERS
    // ------------------------------------------------------------------------

    /**
     * Combines [correctAnswer] with 3 [wrongAnswers], shuffles their order using [random],
     * and returns the randomized 4-option list paired with the new index of the correct answer.
     */
    fun randomizeOptionsWithCorrectIndex(
        correctAnswer: String,
        wrongAnswers: List<String>,
        random: Random = Random()
    ): Pair<List<String>, Int> {
        val distinctWrongs = wrongAnswers.filter { it != correctAnswer }.distinct().take(3)
        val combined = (listOf(correctAnswer) + distinctWrongs).shuffled(random)
        val correctIndex = combined.indexOf(correctAnswer).coerceAtLeast(0)
        return combined to correctIndex
    }

    /**
     * Generates 3 believable numeric distractors close to [correctVal], shuffles all 4 options,
     * and returns the shuffled option strings paired with the correct answer's index.
     */
    fun buildNumericOptionsWithIndex(
        correctVal: Int,
        random: Random = Random()
    ): Pair<List<String>, Int> {
        val candidates = mutableSetOf<Int>()
        val closeOffsets = listOf(-10, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 10)
        val shuffledOffsets = closeOffsets.shuffled(random)

        for (offset in shuffledOffsets) {
            if (candidates.size >= 3) break
            val wrong = correctVal + offset
            if (wrong != correctVal && (correctVal < 0 || wrong >= 0)) {
                candidates.add(wrong)
            }
        }

        var fallbackDelta = 6
        while (candidates.size < 3) {
            val wrong = abs(correctVal + fallbackDelta)
            if (wrong != correctVal) candidates.add(wrong)
            fallbackDelta += 3
        }

        return randomizeOptionsWithCorrectIndex(
            correctAnswer = correctVal.toString(),
            wrongAnswers = candidates.take(3).map { it.toString() },
            random = random
        )
    }
}
