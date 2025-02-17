package com.example.myapplication

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.util.Log

class AudioRecorder(private val context: Context, private val durationInSeconds: Int = 3) {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000 // YAMNet requires 16 kHz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO // Mono audio
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT // 16-bit PCM
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    // Start recording audio
    suspend fun startRecording(): FloatArray {
        return withContext(Dispatchers.IO) {
            // Check if RECORD_AUDIO permission is granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("RECORD_AUDIO permission is required to record audio.")
            }

            // Initialize AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            val audioBuffer = ShortArray(bufferSize)
            val audioData = mutableListOf<Float>()

            audioRecord?.startRecording()

            // Record for the specified duration
            val recordingDuration = durationInSeconds * sampleRate // Duration in samples
            var totalSamplesRead = 0

            while (totalSamplesRead < recordingDuration) {
                val samplesRead = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                if (samplesRead > 0) {
                    // Convert 16-bit PCM to FloatArray and normalize to [-1, 1]
                    for (i in 0 until samplesRead) {
                        audioData.add(audioBuffer[i].toFloat() / Short.MAX_VALUE)
                    }
                    totalSamplesRead += samplesRead
                }
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            // Ensure the audio data has exactly the required number of samples
            if (audioData.size > recordingDuration) {
                audioData.subList(recordingDuration, audioData.size).clear()
            } else if (audioData.size < recordingDuration) {
                audioData.addAll(List(recordingDuration - audioData.size) { 0f })
            }

            // Log the audio data
            Log.d("AudioRecorder", "Recorded audio shape: [${audioData.size}]")
            Log.d("AudioRecorder", "First 10 samples: ${audioData.take(10).joinToString()}")

            // Convert to FloatArray
            audioData.toFloatArray()
        }
    }
}