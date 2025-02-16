package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.task.audio.classifier.Classifications
import org.tensorflow.lite.task.audio.classifier.Category
import org.tensorflow.lite.examples.audio.AudioClassificationHelper

class MainActivity : ComponentActivity(), AudioClassificationListener {
    private lateinit var audioHelper: AudioClassificationHelper
    private var classificationResult by mutableStateOf("Press Start to classify")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AudioClassificationHelper
        audioHelper = AudioClassificationHelper(this, this)

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ClassificationScreen(
                        classificationResult = classificationResult,
                        onStart = { audioHelper.startAudioClassification() },
                        onStop = { audioHelper.stopAudioClassification() }
                    )
                }
            }
        }
    }

    override fun onResult(categories: List<Category>, inferenceTime: Long) {
        val resultText = categories.joinToString("\n") { "${it.label}: ${it.score}" }
        classificationResult = "Results:\n$resultText\nTime: ${inferenceTime}ms"
    }

    override fun onError(error: String) {
        classificationResult = "Error: $error"
    }

    override fun onDestroy() {
        super.onDestroy()
        audioHelper.stopAudioClassification()
    }
}

@Composable
fun ClassificationScreen(
    classificationResult: String,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Audio Classification", modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = onStart) {
            Text("Start Classification")
        }

        Button(onClick = onStop, modifier = Modifier.padding(top = 8.dp)) {
            Text("Stop Classification")
        }

        Text(text = classificationResult, modifier = Modifier.padding(top = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreen() {
    ClassificationScreen(
        classificationResult = "Press Start to classify",
        onStart = {},
        onStop = {}
    )
}
