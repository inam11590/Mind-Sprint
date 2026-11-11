package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Synthesizes crisp arcade sound effects on the fly (no external audio files required)
 * and delegates tactile haptic vibration to [HapticFeedbackManager].
 */
class SoundAndHapticsManager(context: Context) {

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val hapticManager = HapticFeedbackManager(context)

    var soundEnabled: Boolean = true

    var vibrationEnabled: Boolean
        get() = hapticManager.isVibrationEnabled
        set(value) {
            hapticManager.updatePreference(value)
        }

    /**
     * Plays a cheerful ascending chime and triggers correct-answer haptic vibration.
     */
    fun playCorrectFeedback(comboMultiplier: Int = 1) {
        hapticManager.vibrateCorrectAnswer(comboMultiplier)
        if (!soundEnabled) return

        val pitchBoost = 1.0 + (comboMultiplier.coerceIn(1, 5) - 1) * 0.08
        val note1 = (523.25 * pitchBoost)
        val note2 = (659.25 * pitchBoost)
        val note3 = (783.99 * pitchBoost)
        playToneSequence(
            listOf(
                ToneStep(freqHz = note1, durationMs = 65, volume = 0.35f),
                ToneStep(freqHz = note2, durationMs = 65, volume = 0.38f),
                ToneStep(freqHz = note3, durationMs = 120, volume = 0.42f)
            )
        )
    }

    /**
     * Plays a soft low two-tone buzz and triggers incorrect-answer double-pulse vibration.
     */
    fun playWrongFeedback() {
        hapticManager.vibrateIncorrectAnswer()
        if (!soundEnabled) return

        playToneSequence(
            listOf(
                ToneStep(freqHz = 220.0, durationMs = 95, volume = 0.35f, isTriangle = true),
                ToneStep(freqHz = 164.81, durationMs = 150, volume = 0.38f, isTriangle = true)
            )
        )
    }

    /**
     * Subtle pop and haptic tick for UI button taps.
     */
    fun playTapFeedback() {
        hapticManager.vibrateTap()
        if (!soundEnabled) return

        playToneSequence(
            listOf(ToneStep(freqHz = 440.0, durationMs = 35, volume = 0.20f))
        )
    }

    /**
     * Celebratory fanfare and victory haptic rhythm when finishing a round or leveling up.
     */
    fun playVictoryFanfare() {
        hapticManager.vibrateVictory()
        if (!soundEnabled) return

        playToneSequence(
            listOf(
                ToneStep(freqHz = 523.25, durationMs = 80, volume = 0.35f),
                ToneStep(freqHz = 659.25, durationMs = 80, volume = 0.35f),
                ToneStep(freqHz = 783.99, durationMs = 80, volume = 0.38f),
                ToneStep(freqHz = 1046.50, durationMs = 200, volume = 0.45f)
            )
        )
    }

    private data class ToneStep(
        val freqHz: Double,
        val durationMs: Int,
        val volume: Float,
        val isTriangle: Boolean = false
    )

    private fun playToneSequence(steps: List<ToneStep>) {
        audioScope.launch {
            try {
                val sampleRate = 22050
                val totalMs = steps.sumOf { it.durationMs }
                val totalSamples = (sampleRate * totalMs) / 1000
                if (totalSamples <= 0) return@launch

                val pcmBuffer = ShortArray(totalSamples)
                var sampleIndex = 0

                for (step in steps) {
                    val stepSamples = (sampleRate * step.durationMs) / 1000
                    for (i in 0 until stepSamples) {
                        if (sampleIndex >= totalSamples) break
                        val progress = i.toDouble() / stepSamples
                        val envelope = when {
                            progress < 0.08 -> (progress / 0.08)
                            progress > 0.75 -> ((1.0 - progress) / 0.25)
                            else -> 1.0
                        }
                        val angle = 2.0 * PI * i * step.freqHz / sampleRate
                        val rawWave = if (step.isTriangle) {
                            (2.0 / PI) * kotlin.math.asin(sin(angle))
                        } else {
                            sin(angle)
                        }
                        val sampleVal = (rawWave * step.volume * envelope * Short.MAX_VALUE)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                        pcmBuffer[sampleIndex++] = sampleVal
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcmBuffer, 0, pcmBuffer.size)
                audioTrack.play()
                kotlinx.coroutines.delay(totalMs.toLong() + 60L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Throwable) {
                // Ignore audio hardware unavailability on headless/test environments
            }
        }
    }
}
