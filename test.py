#PYTHON STUFF FOR FILTERING AND IDENTIFICATION

import tensorflow_hub as hub
import librosa
import numpy as np
import pandas as pd
from scipy.signal import butter, filtfilt


#filtering stuff!!
def butter_bandpass(lowcut, highcut, fs, order=5):
    nyquist = 0.5 * fs
    low = lowcut / nyquist
    high = highcut / nyquist
    b, a = butter(order, [low, high], btype='band')
    return b, a

def apply_bandpass_filter(data, lowcut, highcut, fs, order=5):
    b, a = butter_bandpass(lowcut, highcut, fs, order=order)
    y = filtfilt(b, a, data)
    return y


# Load YAMNet and class labels
yamnet = hub.load('https://tfhub.dev/google/yamnet/1')
class_map = pd.read_csv("https://raw.githubusercontent.com/tensorflow/models/master/research/audioset/yamnet/yamnet_class_map.csv")
class_names = class_map["display_name"].tolist()

# Load audio
audio, sr = librosa.load("audio_test/audio1.wav", sr=16000, mono=True)

# Apply bandpass filter (20 Hz to 8000 Hz)
filtered_audio = apply_bandpass_filter(audio, lowcut=100, highcut=7800, fs=sr)

# Run inference on filtered audio
scores, embeddings, spectrogram = yamnet(filtered_audio)

#dangerous sounds dict

dangerous_classes = ['Breaking', 'Shout', 'Bellow', 'Yell', 'Screaming', 'Roar', 'Vehicle horn, car horn, honking', 'Car alarm', 'Explosion', 'Slam', 'Emergency vehicle', 'Police car (siren)', 'Ambulance (siren)', 'Fire engine, fire truck (siren)', 'Train whistle', 'Train horn', 'Train wheels squealing', 'Chainsaw', 'Alarm', 'Siren', 'Civil defense siren', 'Smoke detector, smoke alarm', 'Fire alarm', 'Explosion', 'Gunshot, gunfire', 'Machine gun', 'Fusillade', 'Artillery fire', 'Eruption', 'Boom', 'Bang', 'Smash, crash', 'Whip', 'Crushing']

# Print top 2 predictions per window
for i, window_scores in enumerate(scores):
    # Convert TensorFlow tensor to numpy and get top 2 indices
    scores_np = window_scores.numpy()
    top_indices = np.argsort(scores_np)[-2:][::-1]  # Indices sorted descendingly
    top_classes = [class_names[idx] for idx in top_indices]
    top_confidences = scores_np[top_indices]

    if (top_classes[0] in dangerous_classes) and top_confidences[0] >= 0.5:
        print(f"Window {i} ({i*0.975:.2f}s - {(i+1)*0.975:.2f}s):")
        print(f"{top_classes[0]:<20} Confidence: {top_confidences[0]:.4f}")

    if top_classes[1] in dangerous_classes and top_confidences[1] >= 0.5:
        print(f"Window {i} ({i*0.975:.2f}s - {(i+1)*0.975:.2f}s):")
        print(f"{top_classes[1]:<20} Confidence: {top_confidences[1]:.4f}")

    # print(f"Window {i} ({i*0.975:.2f}s - {(i+1)*0.975:.2f}s):")
    # print(f"  1st: {top_classes[0]:<20} Confidence: {top_confidences[0]:.4f}")
    # print(f"  2nd: {top_classes[1]:<20} Confidence: {top_confidences[1]:.4f}\n")




#KOTLIN CODE???

# import org.tensorflow.lite.Interpreter
# import java.io.File
# import java.nio.FloatBuffer
# import java.nio.MappedByteBuffer
# import android.content.Context
# import kotlin.math.PI

# class AudioAnalyzer(private val context: Context) {
#     private lateinit var interpreter: Interpreter
#     private val dangerousClasses = setOf(
#         "Breaking", "Shout", "Bellow", "Yell", "Screaming", "Roar",
#         "Vehicle horn, car horn, honking", "Car alarm", "Explosion", "Slam",
#         "Emergency vehicle", "Police car (siren)", "Ambulance (siren)",
#         "Fire engine, fire truck (siren)", "Train whistle", "Train horn",
#         "Train wheels squealing", "Chainsaw", "Alarm", "Siren", "Civil defense siren",
#         "Smoke detector, smoke alarm", "Fire alarm", "Explosion", "Gunshot, gunfire",
#         "Machine gun", "Fusillade", "Artillery fire", "Eruption", "Boom", "Bang",
#         "Smash, crash", "Whip", "Crushing"
#     )

#     init {
#         loadModel()
#     }

#     private fun loadModel() {
#         // Load the TFLite model from assets
#         val modelBuffer = loadModelFile()
#         interpreter = Interpreter(modelBuffer)
#     }

#     private fun loadModelFile(): MappedByteBuffer {
#         // Implementation to load the TFLite model from assets
#         // You'll need to convert YAMNet to TFLite format first
#         return context.assets.openFd("yamnet.tflite").let { fileDescriptor ->
#             // Implementation details for loading the model buffer
#             TODO("Implement model loading from assets")
#         }
#     }

#     // Butterworth bandpass filter implementation
#     private fun butterBandpass(
#         lowcut: Double,
#         highcut: Double,
#         fs: Double,
#         order: Int = 5
#     ): Pair<DoubleArray, DoubleArray> {
#         val nyquist = 0.5 * fs
#         val low = lowcut / nyquist
#         val high = highcut / nyquist
        
#         // This is a simplified implementation - you might want to use a DSP library
#         // for more accurate filter coefficients
#         TODO("Implement Butterworth filter coefficients calculation")
#     }

#     private fun applyBandpassFilter(
#         data: FloatArray,
#         lowcut: Double,
#         highcut: Double,
#         fs: Double,
#         order: Int = 5
#     ): FloatArray {
#         val (b, a) = butterBandpass(lowcut, highcut, fs, order)
#         // Implementation of filtfilt operation
#         // You might want to use a DSP library for this
#         TODO("Implement filter application")
#     }

#     fun analyzeAudio(audioFile: File) {
#         // Load audio file
#         // Note: You'll need to use Android's MediaExtractor or a similar library
#         // to load and process audio files
#         val (audio, sampleRate) = loadAudioFile(audioFile)

#         // Apply bandpass filter
#         val filteredAudio = applyBandpassFilter(
#             data = audio,
#             lowcut = 100.0,
#             highcut = 7800.0,
#             fs = sampleRate.toDouble()
#         )

#         // Run inference
#         val results = runInference(filteredAudio)
        
#         // Process results
#         results.forEachIndexed { windowIndex, scores ->
#             val topPredictions = getTopPredictions(scores, 2)
            
#             topPredictions.forEach { (className, confidence) ->
#                 if (className in dangerousClasses && confidence >= 0.5f) {
#                     val startTime = windowIndex * 0.975
#                     val endTime = (windowIndex + 1) * 0.975
#                     println("Window $windowIndex (${startTime.format(2)}s - ${endTime.format(2)}s):")
#                     println("$className: Confidence: ${confidence.format(4)}")
#                 }
#             }
#         }
#     }

#     private fun loadAudioFile(file: File): Pair<FloatArray, Int> {
#         // Implementation for loading audio file
#         TODO("Implement audio file loading")
#     }

#     private fun runInference(audioData: FloatArray): Array<FloatArray> {
#         // Implementation for running the model inference
#         TODO("Implement model inference")
#     }

#     private fun getTopPredictions(
#         scores: FloatArray,
#         topK: Int
#     ): List<Pair<String, Float>> {
#         return scores.withIndex()
#             .sortedByDescending { it.value }
#             .take(topK)
#             .map { (index, score) -> 
#                 Pair(getClassName(index), score)
#             }
#     }

#     private fun getClassName(index: Int): String {
#         // Implementation to map index to class name
#         // You'll need to load the class mapping from a resource file
#         TODO("Implement class name mapping")
#     }

#     private fun Double.format(digits: Int) = "%.${digits}f".format(this)
#     private fun Float.format(digits: Int) = "%.${digits}f".format(this)
# }