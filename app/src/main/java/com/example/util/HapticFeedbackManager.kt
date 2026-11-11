package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Dedicated Haptic Feedback Utility Class for Mind Sprint.
 * Triggers distinct device vibrations on correct answers, incorrect answers,
 * UI taps, and game completion when enabled in user preferences.
 */
class HapticFeedbackManager(context: Context) {

    private val appContext: Context = context.applicationContext

    /**
     * Reflects the user's toggleable vibration preference stored in DataStore / SharedPreferences.
     */
    @Volatile
    var isVibrationEnabled: Boolean = true

    /**
     * Updates the haptic feedback preference toggle.
     * When toggled ON, triggers a short confirmation pulse.
     */
    fun updatePreference(enabled: Boolean, playConfirmationPulse: Boolean = false) {
        isVibrationEnabled = enabled
        if (enabled && playConfirmationPulse) {
            vibrateTap()
        }
    }

    /**
     * Crisp, single-pulse (or combo-boosted double-pulse) haptic feedback for a CORRECT answer.
     */
    fun vibrateCorrectAnswer(comboMultiplier: Int = 1) {
        if (!isVibrationEnabled) return

        val clampedCombo = comboMultiplier.coerceIn(1, 5)
        if (clampedCombo >= 3) {
            // Energetic double-tap pulse for high combos (x3..x5)
            vibrateWaveform(
                timings = longArrayOf(0, 28, 30, 40),
                amplitudes = intArrayOf(0, 160, 0, 230)
            )
        } else {
            vibrateWaveform(
                timings = longArrayOf(0, 35),
                amplitudes = intArrayOf(0, 175)
            )
        }
    }

    /**
     * Distinct double-buzz shake vibration for an INCORRECT answer.
     */
    fun vibrateIncorrectAnswer() {
        if (!isVibrationEnabled) return

        vibrateWaveform(
            timings = longArrayOf(0, 65, 45, 85),
            amplitudes = intArrayOf(0, 220, 0, 255)
        )
    }

    /**
     * Subtle micro-pulse for button taps and UI switches.
     */
    fun vibrateTap() {
        if (!isVibrationEnabled) return

        vibrateWaveform(
            timings = longArrayOf(0, 15),
            amplitudes = intArrayOf(0, 95)
        )
    }

    /**
     * Celebratory rhythm when completing a game or unlocking a badge.
     */
    fun vibrateVictory() {
        if (!isVibrationEnabled) return

        vibrateWaveform(
            timings = longArrayOf(0, 40, 40, 40, 40, 110),
            amplitudes = intArrayOf(0, 150, 0, 185, 0, 255)
        )
    }

    private fun resolveVibrator(): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager =
                    appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        try {
            val vibrator = resolveVibrator() ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Throwable) {
            // Safe fallback on devices/test environments without vibrator hardware
        }
    }
}
