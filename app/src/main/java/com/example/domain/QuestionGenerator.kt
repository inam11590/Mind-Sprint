package com.example.domain

/**
 * Dynamic difficulty configuration computed from the player's current level
 * and their live session score. Controls math operand bounds, allowed operations,
 * and vocabulary rarity tier.
 */
data class DynamicDifficultyConfig(
    val playerLevel: Int,
    val currentScore: Int,
    val effectiveLevel: Int,
    val difficultyLabel: String,
    val mathAddSubMin: Int,
    val mathAddSubMax: Int,
    val mathMulFactorAMax: Int,
    val mathMulFactorBMax: Int,
    val mathDivisorMax: Int,
    val mathQuotientMax: Int,
    val mathSquareMaxBase: Int,
    val allowMultiStepMath: Boolean,
    val targetVocabRarity: VocabRarity
)

/**
 * Convenience subclass of [GameEngine] preserving backward-compatible references.
 */
class QuestionGenerator : GameEngine()
