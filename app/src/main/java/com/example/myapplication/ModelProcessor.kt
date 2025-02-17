package com.example.myapplication.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YAMNetHelper(context: Context) {
    private val interpreter: Interpreter

    init {
        // Load the TFLite model from the assets folder
        val modelFileDescriptor = context.assets.openFd("yamnet.tflite")
        val modelBuffer = loadModelFile(modelFileDescriptor)
        interpreter = Interpreter(modelBuffer)
    }

    private fun loadModelFile(fileDescriptor: android.content.res.AssetFileDescriptor): MappedByteBuffer {
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Function to classify audio data
    fun classifyAudio(audioData: FloatArray): Array<FloatArray> {
        // Log the input tensor shape and content
        Log.d("YAMNetHelper", "Input tensor shape: [${audioData.size}]")
        Log.d("YAMNetHelper", "First 10 samples: ${audioData.take(10).joinToString()}")

        // Define the output array with shape [4, 521]
        val output = Array(4) { FloatArray(521) } // Shape: [4, 521]

        // Run the interpreter with the 1D input tensor
        interpreter.run(audioData, output)

        // Return the predictions
        return output
    }

    fun close() {
        interpreter.close()
    }
}