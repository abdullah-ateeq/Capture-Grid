package com.example.capturegrid.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class Sfx { PLACE, CAPTURE, INVALID, PASS, GAME_END }

/**
 * Restrained, procedurally synthesised effects (soft decaying tones), so the app ships
 * with no audio assets. Clips are rendered once to cache WAV files and played via SoundPool.
 */
class SoundEffects(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mutableMapOf<Sfx, Int>()

    init {
        val dir = File(context.cacheDir, "sfx").apply { mkdirs() }
        Sfx.entries.forEach { sfx ->
            val file = File(dir, "${sfx.name.lowercase()}_v1.wav")
            if (!file.exists()) file.writeBytes(wav(render(sfx)))
            ids[sfx] = pool.load(file.absolutePath, 1)
        }
    }

    fun play(sfx: Sfx) {
        ids[sfx]?.let { pool.play(it, 1f, 1f, 1, 0, 1f) }
    }

    fun release() = pool.release()

    private data class Tone(val startMs: Int, val freq: Double, val durationMs: Int, val gain: Double, val decay: Double)

    private fun render(sfx: Sfx): FloatArray {
        val tones = when (sfx) {
            Sfx.PLACE -> listOf(Tone(0, 540.0, 90, 0.50, 38.0), Tone(0, 1080.0, 50, 0.12, 60.0))
            Sfx.CAPTURE -> listOf(
                Tone(0, 784.0, 160, 0.34, 14.0),
                Tone(70, 587.0, 220, 0.34, 11.0),
                Tone(0, 150.0, 90, 0.25, 30.0)
            )
            Sfx.INVALID -> listOf(Tone(0, 196.0, 70, 0.40, 30.0), Tone(95, 185.0, 90, 0.40, 26.0))
            Sfx.PASS -> listOf(Tone(0, 392.0, 170, 0.30, 16.0))
            Sfx.GAME_END -> listOf(
                Tone(0, 392.0, 380, 0.26, 6.0),
                Tone(110, 523.3, 380, 0.26, 6.0),
                Tone(220, 659.3, 520, 0.28, 5.0)
            )
        }
        val totalMs = tones.maxOf { it.startMs + it.durationMs }
        val out = FloatArray(SAMPLE_RATE * totalMs / 1000)
        for (tone in tones) {
            val start = SAMPLE_RATE * tone.startMs / 1000
            val length = SAMPLE_RATE * tone.durationMs / 1000
            for (i in 0 until length) {
                val t = i.toDouble() / SAMPLE_RATE
                val attack = (i / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
                val release = ((length - i) / (SAMPLE_RATE * 0.012)).coerceAtMost(1.0)
                val envelope = attack * release * exp(-tone.decay * t)
                val sample = sin(2 * PI * tone.freq * t) + 0.18 * sin(4 * PI * tone.freq * t)
                val index = start + i
                if (index < out.size) out[index] += (sample * envelope * tone.gain).toFloat()
            }
        }
        return out
    }

    private fun wav(samples: FloatArray): ByteArray {
        val data = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { data.putShort((it.coerceIn(-1f, 1f) * Short.MAX_VALUE * 0.8f).toInt().toShort()) }
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + samples.size * 2); put("WAVE".toByteArray())
            put("fmt ".toByteArray()); putInt(16); putShort(1); putShort(1)
            putInt(SAMPLE_RATE); putInt(SAMPLE_RATE * 2); putShort(2); putShort(16)
            put("data".toByteArray()); putInt(samples.size * 2)
        }
        return ByteArrayOutputStream().apply { write(header.array()); write(data.array()) }.toByteArray()
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
