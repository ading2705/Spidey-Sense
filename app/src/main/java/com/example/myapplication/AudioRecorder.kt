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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

class AudioRecorder(private val context: Context, private val durationInSeconds: Int = 1) {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 15600 // YAMNet requires ~16 kHz (adjust if needed)
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO // Mono audio
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT // 16-bit PCM

    private val bufferSize = maxOf(
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat),
        sampleRate * durationInSeconds // Ensure enough space
    )

    /**
     * Records audio for the specified duration, applies a Butterworth high-pass filter,
     * and suppresses low-magnitude noise.
     */
    suspend fun startRecording(): FloatArray {
        return withContext(Dispatchers.IO) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("RECORD_AUDIO permission is required to record audio.")
            }

            val recordingDuration = durationInSeconds * sampleRate // Duration in samples
            val audioData = FloatArray(recordingDuration)
            val audioBuffer = ShortArray(bufferSize)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("Failed to initialize AudioRecord")
            }

            try {
                audioRecord?.startRecording()

                var index = 0
                var totalSamplesRead = 0

                while (totalSamplesRead < recordingDuration) {
                    val samplesRead = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                    if (samplesRead > 0) {
                        for (i in 0 until samplesRead) {
                            if (index < recordingDuration) {
                                // Convert to float in the range [-1, 1]
                                audioData[index] = audioBuffer[i].toFloat() / Short.MAX_VALUE
                                index++
                            }
                        }
                        totalSamplesRead += samplesRead
                    }
                }
            } finally {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            }

            Log.d("AudioRecorder", "Recorded audio shape: [${audioData.size}]")
            Log.d("AudioRecorder", "First 10 samples (raw): ${audioData.take(10).joinToString()}")

            // Apply Butterworth high-pass filter (to remove low-frequency noise)
            val cutoffFrequency = 300f
            val filteredData = butterworthHighPassFilter(audioData, sampleRate, cutoffFrequency)

            // Apply magnitude-based noise suppression (to remove low-magnitude noise)
            val magnitudeThresholdFraction = 0.05f // Remove anything below 5% of max amplitude
            val finalFilteredData = adaptiveMagnitudeFilter(filteredData, magnitudeThresholdFraction)

            Log.d("AudioRecorder", "First 10 samples (filtered): ${finalFilteredData.take(10).joinToString()}")

            finalFilteredData
        }
    }

    /**
     * Applies a second-order Butterworth high-pass filter to the given audio data.
     *
     * @param data The input audio samples.
     * @param sampleRate The sample rate of the audio.
     * @param cutoffFrequency The cutoff frequency in Hz.
     * @return A new FloatArray containing the filtered audio.
     */
    private fun butterworthHighPassFilter(
        data: FloatArray,
        sampleRate: Int,
        cutoffFrequency: Float
    ): FloatArray {
        val filteredData = FloatArray(data.size)

        // Calculate angular frequency
        val omega = 2 * PI * cutoffFrequency / sampleRate
        val cosOmega = cos(omega)
        val sinOmega = sin(omega)
        // For Butterworth, Q is set to 1/√2
        val Q = 1 / sqrt(2.0)
        val alpha = sinOmega / (2 * Q)

        // Biquad coefficients for high-pass filter
        val b0 = (1 + cosOmega) / 2
        val b1 = -(1 + cosOmega)
        val b2 = (1 + cosOmega) / 2
        val a0 = 1 + alpha
        val a1 = -2 * cosOmega
        val a2 = 1 - alpha

        // Normalize coefficients
        val b0n = (b0 / a0).toFloat()
        val b1n = (b1 / a0).toFloat()
        val b2n = (b2 / a0).toFloat()
        val a1n = (a1 / a0).toFloat()
        val a2n = (a2 / a0).toFloat()

        // Filter state: previous input (x) and output (y) values
        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        // Apply the difference equation:
        // y[n] = b0*x[n] + b1*x[n-1] + b2*x[n-2] - a1*y[n-1] - a2*y[n-2]
        for (i in data.indices) {
            val x0 = data[i]
            val y0 = b0n * x0 + b1n * x1 + b2n * x2 - a1n * y1 - a2n * y2
            filteredData[i] = y0

            // Update delay buffers
            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0
        }
        return filteredData
    }

    /**
     * Applies magnitude-based noise suppression.
     * Any values below a certain percentage of the max amplitude are zeroed out.
     *
     * @param data The input audio samples.
     * @param thresholdFraction A value between 0 and 1 (e.g., 0.05 means remove anything below 5% of max amplitude).
     * @return A new FloatArray with suppressed low-magnitude noise.
     */
    private fun adaptiveMagnitudeFilter(data: FloatArray, sensitivity: Float = 0.1f): FloatArray {
        // Compute median absolute deviation (MAD) to estimate noise level
        val absData = data.map { kotlin.math.abs(it) }
        val median = absData.sorted()[absData.size / 2]  // Approximate median
        val mad = absData.map { kotlin.math.abs(it - median) }.sorted()[absData.size / 2]  // MAD

        val noiseThreshold = median + (sensitivity * mad)  // Adaptive threshold

        return data.map {
            if (kotlin.math.abs(it) < noiseThreshold) it * 0.1f  // Attenuate rather than zeroing out
            else it
        }.toFloatArray()
    }

}
