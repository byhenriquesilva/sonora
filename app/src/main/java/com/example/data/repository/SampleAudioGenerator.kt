package com.example.data.repository

import android.content.Context
import com.example.data.model.Song
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object SampleAudioGenerator {

    data class TrackInfo(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val durationSec: Int,
        val baseFreq: Double,
        val chordType: Int,
        val genre: String
    )

    private val sampleTracks = listOf(
        TrackInfo(100001L, "Sonora Chill Sunrise", "Acoustic Waves", "Ambient Horizons", 18, 220.0, 1, "Acústico"),
        TrackInfo(100002L, "Midnight Groove", "Echo Collective", "Night Flight", 22, 174.6, 2, "Lounge"),
        TrackInfo(100003L, "Velvet Neon Waves", "Cyber Lounge", "Night Flight", 16, 261.6, 3, "Eletrônica"),
        TrackInfo(100004L, "Aura Resonance", "Echo Collective", "Ambient Horizons", 20, 196.0, 1, "Ambient"),
        TrackInfo(100005L, "Starlight Melodies", "Luna Duo", "Cosmic Dreams", 24, 293.6, 2, "Pop")
    )

    fun getSampleSongs(context: Context): List<Song> {
        val sampleDir = File(context.filesDir, "samples")
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
        }

        return sampleTracks.map { info ->
            val wavFile = File(sampleDir, "sample_${info.id}.wav")
            if (!wavFile.exists() || wavFile.length() < 1000) {
                generateWavFile(wavFile, info.durationSec, info.baseFreq, info.chordType)
            }

            Song(
                id = info.id,
                title = info.title,
                artist = info.artist,
                album = info.album,
                durationMs = info.durationSec * 1000L,
                albumId = info.album.hashCode().toLong(),
                contentUri = wavFile.toURI().toString(),
                dataPath = wavFile.absolutePath,
                size = wavFile.length(),
                mimeType = "audio/wav",
                year = 2024,
                dateAdded = System.currentTimeMillis() / 1000,
                folder = "Músicas de Demonstração",
                genre = info.genre
            )
        }
    }

    private fun generateWavFile(file: File, durationSec: Int, baseFreq: Double, chordType: Int) {
        val sampleRate = 44100
        val numSamples = durationSec * sampleRate
        val pcmData = ByteArray(numSamples * 2) // 16-bit mono

        val intervals = when (chordType) {
            1 -> doubleArrayOf(1.0, 1.25, 1.5, 1.875) // Major 7th
            2 -> doubleArrayOf(1.0, 1.2, 1.5, 1.78)  // Minor 7th
            else -> doubleArrayOf(1.0, 1.333, 1.5, 2.0) // Sus4 / Octave
        }

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sampleVal = 0.0

            // Multi-oscillator gentle synth pad
            for (idx in intervals.indices) {
                val freq = baseFreq * intervals[idx]
                // Subtle arpeggio / tremolo modulation
                val lfo = 0.8 + 0.2 * sin(2.0 * Math.PI * 0.5 * t + idx)
                sampleVal += sin(2.0 * Math.PI * freq * t) * lfo * 0.22
            }

            // Envelope (gentle attack, sustain, smooth decay)
            val envelope = when {
                t < 1.0 -> t
                t > durationSec - 1.5 -> (durationSec - t) / 1.5
                else -> 1.0
            }.coerceIn(0.0, 1.0)

            val shortVal = (sampleVal * envelope * 24000).toInt().coerceIn(-32767, 32767).toShort()
            val index = i * 2
            pcmData[index] = (shortVal.toInt() and 0xFF).toByte()
            pcmData[index + 1] = ((shortVal.toInt() shr 8) and 0xFF).toByte()
        }

        FileOutputStream(file).use { fos ->
            writeWavHeader(fos, 1, sampleRate, 16, pcmData.size)
            fos.write(pcmData)
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        channels: Short,
        sampleRate: Int,
        bitsPerSample: Short,
        pcmDataSize: Int
    ) {
        val totalDataLen = pcmDataSize + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size for PCM
            putShort(1) // AudioFormat 1 = PCM
            putShort(channels)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign)
            putShort(bitsPerSample)
            put("data".toByteArray())
            putInt(pcmDataSize)
        }
        out.write(header.array())
    }
}
