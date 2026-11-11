package com.example

import com.example.domain.GameEngine
import com.example.domain.QuizCategory
import com.example.domain.ScienceBank
import com.example.domain.VocabBank
import com.example.domain.VocabRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun builtInBanksMeetMinimumSizeRequirements() {
        assertTrue("VocabBank must contain at least 300 words", VocabBank.words.size >= 300)
        assertTrue("ScienceBank must contain at least 200 questions", ScienceBank.questions.size >= 200)
    }

    @Test
    fun gameEngineDynamicDifficultyScalesMathOperandsAndVocabRarityWithScoreAndLevel() {
        val engine = GameEngine()

        // Level 1, Score 0 -> Effective Level 1, single-digit math, Common vocabulary
        val starterConfig = engine.calculateDynamicDifficulty(playerLevel = 1, currentScore = 0)
        assertEquals(1, starterConfig.effectiveLevel)
        assertEquals(9, starterConfig.mathAddSubMax)
        assertFalse(starterConfig.allowMultiStepMath)
        assertEquals(VocabRarity.COMMON, starterConfig.targetVocabRarity)

        // Level 1, Score 90 -> Effective Level 4, larger operands, multi-step math, Intermediate vocab
        val midSessionConfig = engine.calculateDynamicDifficulty(playerLevel = 1, currentScore = 90)
        assertEquals(4, midSessionConfig.effectiveLevel)
        assertTrue(midSessionConfig.mathAddSubMax > starterConfig.mathAddSubMax)
        assertTrue(midSessionConfig.allowMultiStepMath)
        assertEquals(VocabRarity.INTERMEDIATE, midSessionConfig.targetVocabRarity)

        // Level 3, Score 120 -> Effective Level 7, high operands, Scholarly Rare vocabulary
        val highSessionConfig = engine.calculateDynamicDifficulty(playerLevel = 3, currentScore = 120)
        assertEquals(7, highSessionConfig.effectiveLevel)
        assertTrue(highSessionConfig.mathAddSubMax > midSessionConfig.mathAddSubMax)
        assertEquals(VocabRarity.ADVANCED, highSessionConfig.targetVocabRarity)
    }

    @Test
    fun gameEngineGeneratesUniqueQuestionsAcrossAllFourCategoriesAndRandomizesAnswers() {
        val engine = GameEngine()
        val seenSignatures = mutableSetOf<String>()

        for (category in QuizCategory.entries) {
            for (level in 1..6) {
                val q = engine.nextQuestion(
                    category = category,
                    playerLevel = level,
                    currentScore = (level - 1) * 35
                )
                assertEquals(4, q.options.size)
                assertEquals(4, q.options.distinct().size)
                assertTrue(q.correctIndex in 0..3)
                assertTrue("Duplicate question generated in session: ${q.signature}", seenSignatures.add(q.signature))
            }
        }
    }

    @Test
    fun dailyChallengeSeedProducesDeterministic10Questions() {
        val engine1 = GameEngine()
        val engine2 = GameEngine()
        val set1 = engine1.generateDailyChallengeSet("2026-11-10")
        val set2 = engine2.generateDailyChallengeSet("2026-11-10")

        assertEquals(10, set1.size)
        assertEquals(set1.map { it.questionText }, set2.map { it.questionText })
    }
}
