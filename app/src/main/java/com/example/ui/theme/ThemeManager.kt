package com.example.ui.theme

import android.content.Context
import com.example.data.PlayerStatsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages the application's visual style across 7 unique linear-gradient color palettes
 * with complementary button colors, persisting the user's selected and unlocked themes
 * in Jetpack DataStore via [PlayerStatsDataStore].
 */
class ThemeManager(
    private val statsDataStore: PlayerStatsDataStore
) {
    constructor(context: Context) : this(PlayerStatsDataStore(context.applicationContext))

    /**
     * All 7 built-in linear-gradient color palettes (exceeds the 6+ minimum requirement).
     */
    val availablePalettes: List<AppThemePreset> = AppThemePreset.entries

    /**
     * Reactive stream of the currently active [AppThemePreset] stored in DataStore.
     */
    val selectedThemeFlow: Flow<AppThemePreset> = statsDataStore.selectedThemeIdFlow
        .map { themeId -> AppThemePreset.fromId(themeId) }

    /**
     * Reactive stream of all unlocked theme IDs stored in DataStore.
     */
    val unlockedThemeIdsFlow: Flow<Set<String>> = statsDataStore.playerProfileFlow
        .map { it.unlockedThemeIds }

    /**
     * Resolves a theme palette by its string ID.
     */
    fun getPalette(themeId: String): AppThemePreset = AppThemePreset.fromId(themeId)

    /**
     * Returns the currently active [AppThemePreset] snapshot from DataStore.
     */
    suspend fun getCurrentTheme(): AppThemePreset {
        val id = statsDataStore.selectedThemeIdFlow.first()
        return AppThemePreset.fromId(id)
    }

    /**
     * Switches the active theme in DataStore if already unlocked, or purchases and equips it
     * if the player has enough coins.
     */
    suspend fun unlockOrSelectTheme(theme: AppThemePreset): Boolean {
        return statsDataStore.unlockAndSelectTheme(theme.id, theme.cost)
    }

    /**
     * Directly persists the selected theme ID into DataStore (and adds it to unlocked themes).
     */
    suspend fun setSelectedTheme(theme: AppThemePreset) {
        statsDataStore.setSelectedTheme(theme.id)
    }
}
