package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Semantic Feedback Colors
val CorrectGreen = Color(0xFF10B981)
val CorrectGreenDark = Color(0xFF047857)
val WrongRed = Color(0xFFEF4444)
val WrongRedDark = Color(0xFFB91C1C)
val GoldCoin = Color(0xFFFBBF24)
val XpCyan = Color(0xFF06B6D4)
val StreakFlame = Color(0xFFF97316)

/**
 * 7 Unique Color Palettes with Linear-Gradient Backgrounds and Complementary Button Colors.
 */
enum class AppThemePreset(
    val id: String,
    val displayName: String,
    val cost: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val buttonContainerColor: Color,
    val buttonContentColor: Color,
    val secondaryButtonColor: Color,
    val buttonBorderColor: Color,
    val cardBackground: Color,
    val gradientColors: List<Color>
) {
    COSMIC_INDIGO(
        id = "cosmic_indigo",
        displayName = "Cosmic Indigo",
        cost = 0,
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFF4338CA),
        accentColor = Color(0xFF22D3EE),
        buttonContainerColor = Color(0xFF4F46E5),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFF06B6D4),
        buttonBorderColor = Color(0xFF818CF8),
        cardBackground = Color(0xFF1E1B4B),
        gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81))
    ),
    SUNSET_BLAZE(
        id = "sunset_blaze",
        displayName = "Sunset Blaze",
        cost = 150,
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFEA580C),
        accentColor = Color(0xFFFACC15),
        buttonContainerColor = Color(0xFFEA580C),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFFF59E0B),
        buttonBorderColor = Color(0xFFFDBA74),
        cardBackground = Color(0xFF431407),
        gradientColors = listOf(Color(0xFF270905), Color(0xFF7C2D12), Color(0xFF9A3412))
    ),
    EMERALD_FOREST(
        id = "emerald_forest",
        displayName = "Emerald Forest",
        cost = 200,
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669),
        accentColor = Color(0xFF6EE7B7),
        buttonContainerColor = Color(0xFF059669),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFF14B8A6),
        buttonBorderColor = Color(0xFF6EE7B7),
        cardBackground = Color(0xFF064E3B),
        gradientColors = listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF065F46))
    ),
    CYBER_BERRY(
        id = "cyber_berry",
        displayName = "Cyber Berry",
        cost = 250,
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFFDB2777),
        accentColor = Color(0xFFF472B6),
        buttonContainerColor = Color(0xFFDB2777),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFFA855F7),
        buttonBorderColor = Color(0xFFF9A8D4),
        cardBackground = Color(0xFF500724),
        gradientColors = listOf(Color(0xFF2E0515), Color(0xFF500724), Color(0xFF831843))
    ),
    OCEANIC_ABYSS(
        id = "oceanic_abyss",
        displayName = "Oceanic Abyss",
        cost = 300,
        primaryColor = Color(0xFF0EA5E9),
        secondaryColor = Color(0xFF0284C7),
        accentColor = Color(0xFF38BDF8),
        buttonContainerColor = Color(0xFF0284C7),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFF2DD4BF),
        buttonBorderColor = Color(0xFF7DD3FC),
        cardBackground = Color(0xFF0C4A6E),
        gradientColors = listOf(Color(0xFF082F49), Color(0xFF0C4A6E), Color(0xFF075985))
    ),
    ROYAL_GOLD(
        id = "royal_gold",
        displayName = "Royal Gold",
        cost = 400,
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFD97706),
        accentColor = Color(0xFFFDE047),
        buttonContainerColor = Color(0xFFD97706),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFFEAB308),
        buttonBorderColor = Color(0xFFFDE047),
        cardBackground = Color(0xFF292524),
        gradientColors = listOf(Color(0xFF1C1917), Color(0xFF451A03), Color(0xFF78350F))
    ),
    AURORA_MYSTIC(
        id = "aurora_mystic",
        displayName = "Aurora Borealis",
        cost = 500,
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFF0D9488),
        accentColor = Color(0xFFA3E635),
        buttonContainerColor = Color(0xFF7C3AED),
        buttonContentColor = Color(0xFFFFFFFF),
        secondaryButtonColor = Color(0xFF10B981),
        buttonBorderColor = Color(0xFFC4B5FD),
        cardBackground = Color(0xFF2E1065),
        gradientColors = listOf(Color(0xFF134E4A), Color(0xFF2E1065), Color(0xFF4C1D95))
    );

    /**
     * Diagonal linear-gradient background brush for the entire application viewport.
     */
    fun backgroundBrush(): Brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),
        end = Offset(900f, 1800f)
    )

    /**
     * Complementary linear-gradient brush for primary action buttons.
     */
    fun complementaryButtonBrush(): Brush = Brush.linearGradient(
        colors = listOf(buttonContainerColor, secondaryButtonColor),
        start = Offset(0f, 0f),
        end = Offset(600f, 200f)
    )

    companion object {
        fun fromId(id: String): AppThemePreset =
            entries.find { it.id == id } ?: COSMIC_INDIGO
    }
}
