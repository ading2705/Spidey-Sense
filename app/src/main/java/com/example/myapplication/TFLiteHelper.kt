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

    fun predict(input: Array<IntArray>): FloatArray {
        val output = FloatArray(1)  // Output is a single probability value
        interpreter.run(input, output)
        return output
    }
}
