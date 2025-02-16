package com.example.myapplication

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteHelper(context: Context) {
    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context))
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd("text_classification_model.tflite")  // Ensure the filename matches
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // Modify predict method to accept FloatArray
    fun predict(input: Array<FloatArray>): Float {
        val output = Array(1) { FloatArray(1) }  // Adjust to match [1,1] output shape
        interpreter.run(input, output)
        return output[0][0]  // Extract the value from [1,1] shape
    }
}

