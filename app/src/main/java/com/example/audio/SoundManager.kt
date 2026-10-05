package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var isSoundEnabled = true
    private var isVibrationEnabled = true
    private var soundVolume = 0.6f
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
            if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, (soundVolume * 100).toInt())
            }
        } catch (_: Exception) {}
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        isVibrationEnabled = enabled
    }

    fun setVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
        try {
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, (soundVolume * 100).toInt())
        } catch (_: Exception) {}
    }

    fun playClick(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (_: Exception) {}
        vibrate(context, 15)
    }

    fun playSuccess(context: Context? = null) {
        if (!isSoundEnabled) return
        playNotes(floatArrayOf(523.25f, 659.25f, 783.99f), intArrayOf(80, 80, 160)) // C5-E5-G5 "ding"
        vibrate(context, 35)
    }

    fun playError(context: Context? = null) {
        if (!isSoundEnabled) return
        playNotes(floatArrayOf(220f, 180f), intArrayOf(120, 180)) // Low tone
        vibrate(context, 70)
    }

    fun playCoins(context: Context? = null) {
        if (!isSoundEnabled) return
        playNotes(floatArrayOf(987.77f, 1318.51f, 1567.98f), intArrayOf(60, 60, 180)) // B5-E6-G6 "coins"
        vibrate(context, 40)
    }

    fun playFanfare(context: Context? = null) {
        if (!isSoundEnabled) return
        playNotes(floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f), intArrayOf(100, 100, 100, 300)) // Fanfare
        vibrate(context, 90)
    }

    fun playSwitch(context: Context? = null) {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 25)
        } catch (_: Exception) {}
        vibrate(context, 10)
    }

    private fun playNotes(frequencies: FloatArray, durationsMs: IntArray) {
        scope.launch {
            try {
                val sampleRate = 22050
                var totalSamples = 0
                for (d in durationsMs) {
                    totalSamples += (sampleRate * d / 1000)
                }
                val buffer = ShortArray(totalSamples)
                var offset = 0
                for (i in frequencies.indices) {
                    val freq = frequencies[i]
                    val numSamples = (sampleRate * durationsMs[i] / 1000)
                    for (j in 0 until numSamples) {
                        val angle = 2.0 * Math.PI * j / (sampleRate / freq)
                        val envelope = (1.0 - (j.toDouble() / numSamples)) // Fade out
                        val sample = (sin(angle) * Short.MAX_VALUE * soundVolume * 0.7 * envelope).toInt()
                        buffer[offset + j] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    offset += numSamples
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                track.setNotificationMarkerPosition(buffer.size)
                track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onPeriodicNotification(t: AudioTrack?) {}
                    override fun onMarkerReached(t: AudioTrack?) {
                        t?.release()
                    }
                })
            } catch (_: Exception) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
                } catch (_: Exception) {}
            }
        }
    }

    private fun vibrate(context: Context?, durationMs: Long) {
        if (!isVibrationEnabled || context == null) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
