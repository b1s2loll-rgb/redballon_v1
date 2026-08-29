package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

class GameSoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()
    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                if (isGameActive) resumeBackgroundMusic()
            } else {
                pauseBackgroundMusic()
            }
        }
    var isHapticsEnabled: Boolean = true
    private var isGameActive: Boolean = false

    private var musicPlayer: MediaPlayer? = null
    private var isGameActive: Boolean = false
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun startBackgroundMusic() {
        isGameActive = true
        if (!isMusicEnabled) return
        startMusic()
    }

    fun pauseBackgroundMusic() {
        try {
            musicPlayer?.pause()
        } catch (_: Throwable) {}
    }

    fun resumeBackgroundMusic() {
        if (!isMusicEnabled || !isGameActive) return
        try {
            val player = musicPlayer
            if (player == null) {
                startMusic()
            } else if (!player.isPlaying) {
                player.start()
            }
        } catch (_: Throwable) {
            startMusic()
        }
    }

    fun stopBackgroundMusic() {
        isGameActive = false
        stopMusic()
    }

    fun release() {
        isGameActive = false
        stopMusic()
    }

    @Synchronized
    private fun startMusic() {
        if (!isMusicEnabled || !isGameActive) return
        try {
            val existing = musicPlayer
            if (existing != null) {
                if (!existing.isPlaying) existing.start()
                return
            }

            val player = MediaPlayer.create(context, com.example.R.raw.background_music)
            if (player != null) {
                player.isLooping = true
                player.setVolume(0.20f, 0.20f)
                player.setOnCompletionListener { mp ->
                    try {
                        if (!mp.isLooping && isMusicEnabled && isGameActive) mp.start()
                    } catch (_: Throwable) {}
                }
                musicPlayer = player
                player.start()
            }
        } catch (_: Throwable) {
            musicPlayer = null
        }
    }

    @Synchronized
    private fun stopMusic() {
        try {
            musicPlayer?.stop()
        } catch (_: Throwable) {}
        try {
            musicPlayer?.release()
        } catch (_: Throwable) {}
        musicPlayer = null
    }

    fun playTakeoff() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 380
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = sin(progress * Math.PI) * 0.4
                    val freq = 200.0 + progress * progress * 350.0
                    val sample = (sin(2.0 * Math.PI * freq * t) * 0.7 + (random.nextDouble() * 2.0 - 1.0) * 0.3) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playCollectBubble() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 120
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress) * 0.35
                    val freq = 880.0 + progress * 440.0 // A5 to E6 chime
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playPop() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 280
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val env = (1.0 - progress) * (1.0 - progress)

                    // Sharp noise burst + dropping pitch sine thump
                    val noise = (random.nextDouble() * 2.0 - 1.0) * 0.7
                    val pitch = 440.0 * (1.0 - progress * 0.8) + 80.0
                    val sine = sin(2.0 * Math.PI * pitch * t) * 0.6
                    
                    val combined = if (progress < 0.25) {
                        (noise * 0.8 + sine * 0.4) * env
                    } else {
                        (noise * 0.3 + sine * 0.7) * env
                    }

                    buffer[i] = (combined * Short.MAX_VALUE * 0.9).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playLift() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 80
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = sin(progress * Math.PI) * 0.25
                    val freq = 280.0 + progress * 160.0
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playMilestone() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 350
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress) * 0.4
                    val freq1 = 523.25 // C5
                    val freq2 = 659.25 // E5
                    val freq3 = 783.99 // G5
                    val s = (sin(2.0 * Math.PI * freq1 * t) * 0.35 +
                             sin(2.0 * Math.PI * freq2 * t) * 0.35 +
                             sin(2.0 * Math.PI * freq3 * t) * 0.3) * env
                    buffer[i] = (s * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playThunder() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 400
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val env = (1.0 - progress * 0.9) * 0.5
                    val noise = (random.nextDouble() * 2.0 - 1.0)
                    val rumble = sin(2.0 * Math.PI * (60.0 + random.nextDouble() * 40.0) * (i.toDouble() / sampleRate))
                    val combined = (noise * 0.7 + rumble * 0.6) * env
                    buffer[i] = (combined * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playClick() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 30
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = 1.0 - progress
                    val sample = sin(2.0 * Math.PI * 880.0 * t) * env * 0.2
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playPowerUp() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 280
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = sin(progress * Math.PI * 0.95) * 0.45
                    // Rapid ascending chords (C5 -> E5 -> G5 -> C6)
                    val chordPhase = (progress * 4.0).toInt().coerceIn(0, 3)
                    val freq = when (chordPhase) {
                        0 -> 523.25 // C5
                        1 -> 659.25 // E5
                        2 -> 783.99 // G5
                        else -> 1046.50 // C6
                    }
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playShieldDeflect() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 220
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress) * 0.5
                    val freq1 = 440.0 * (1.0 - progress * 0.5)
                    val freq2 = 880.0
                    val sample = (sin(2.0 * Math.PI * freq1 * t) * 0.6 + sin(2.0 * Math.PI * freq2 * t) * 0.4) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playSpeedBoost() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 240
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = sin(progress * Math.PI) * 0.4
                    val freq = 300.0 + progress * 600.0
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playLifeLost() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 260
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress) * 0.5
                    val freq = 320.0 - progress * 140.0 // Warning pitch drop
                    val sample = (sin(2.0 * Math.PI * freq * t) * 0.7 + (random.nextDouble() * 2.0 - 1.0) * 0.3) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playLifeGain() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 360
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = sin(progress * Math.PI * 0.9) * 0.45
                    // Rapid 4-note joyful chord (F5 -> A5 -> C6 -> F6)
                    val noteIndex = (progress * 4.0).toInt().coerceIn(0, 3)
                    val freq = when (noteIndex) {
                        0 -> 698.46 // F5
                        1 -> 880.00 // A5
                        2 -> 1046.50 // C6
                        else -> 1396.91 // F6
                    }
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playCoinEarned() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 180
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress) * 0.4
                    // Ascending chime note: B5 (987.77) to E6 (1318.5)
                    val freq = if (progress < 0.45) 987.77 else 1318.51
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playSkinUnlock() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 450
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress * 0.7) * 0.35
                    // Celebratory arcade arpeggio: C5 (523), E5 (659), G5 (784), C6 (1046)
                    val freq = when {
                        progress < 0.22 -> 523.25
                        progress < 0.44 -> 659.25
                        progress < 0.66 -> 783.99
                        else -> 1046.50
                    }
                    val sample = (sin(2.0 * Math.PI * freq * t) * 0.8 + sin(2.0 * Math.PI * (freq * 2.0) * t) * 0.2) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playAdRevive() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 450
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress * 0.3) * 0.45
                    // Triumphant revival chime arpeggio
                    val noteIndex = (progress * 5.0).toInt().coerceIn(0, 4)
                    val freq = when (noteIndex) {
                        0 -> 523.25 // C5
                        1 -> 659.25 // E5
                        2 -> 783.99 // G5
                        3 -> 1046.50 // C6
                        else -> 1318.51 // E6
                    }
                    val sample = sin(2.0 * Math.PI * freq * t) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playThunderZap() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 280
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - progress) * 0.35
                    // Atmospheric electric crackle & low thunder rumble
                    val noise = (Math.random() * 2.0 - 1.0) * 0.4
                    val rumble = sin(2.0 * Math.PI * (120.0 - progress * 60.0) * t) * 0.6
                    val sample = (noise + rumble) * env
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun vibrateElectricShock() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Intense, rapid buzzing electrical shock vibration pattern
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 35, 25, 45, 25, 60, 30, 80, 25, 40),
                        intArrayOf(0, 255, 0, 240, 0, 255, 0, 220, 0, 200),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 40, 30, 50, 30, 70), -1)
            }
        } catch (_: Throwable) {}
    }

    fun vibrateLifeLost() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 40, 70),
                        intArrayOf(0, 220, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } catch (_: Throwable) {}
    }

    fun vibrateLifeGain() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, 150))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (_: Throwable) {}
    }

    fun vibrateShieldHit() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(45, 180)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(45)
            }
        } catch (_: Throwable) {}
    }

    fun vibratePop() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 30, 40, 80),
                        intArrayOf(0, 255, 0, 200),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(120)
            }
        } catch (_: Throwable) {}
    }

    fun vibrateTap() {
        if (!isHapticsEnabled || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (_: Throwable) {}
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            scope.launch {
                kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 80)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }
}
