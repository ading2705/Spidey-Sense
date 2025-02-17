package com.example.myapplication.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.AudioRecorder
import com.example.myapplication.R
import com.example.myapplication.ml.YAMNetHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var predictionState by remember { mutableStateOf("Press FAB to start detecting.") }
    val yamNetHelper = remember { YAMNetHelper(context) }
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    // List of classes from YAMNet for speech
    val speechClasses = listOf(
        "Speech", "Human speech", "Telephone", "Voice"
    )

    // Launcher to request the RECORD_AUDIO permission
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Check mic permission when the composable is first launched
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val classNames = remember {
        context.resources.openRawResource(R.raw.yamnet_class_map)
            .bufferedReader()
            .useLines { lines ->
                lines.drop(1) // Skip the header row
                    .map { it.split(",")[2].trim('"') } // Extract class names
                    .toList()
            }
    }

    // Launch a coroutine that continuously records while `isRecording` is true
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                try {
                    val audioData = audioRecorder.startRecording()
                    val predictions = yamNetHelper.classifyAudio(audioData)
                    var firstPrediction = predictions[0]

                    // List of classes to exclude (speech-related)
                    val speechClasses = listOf("Speech", "Human speech", "Telephone", "Voice")

                    // Get indices of all speech-related classes
                    val speechIndices = speechClasses.mapNotNull { classNames.indexOf(it).takeIf { it != -1 } }

                    // Get indices of all classes except speech-related ones
                    val remainingIndices = classNames.indices.filter { it !in speechIndices }

                    // Extract corresponding probabilities from predictions
                    val remainingProbabilities = remainingIndices.map { firstPrediction[it] }

                    // Renormalize probabilities to sum to 1
                    val totalRemainingProb = remainingProbabilities.sum().takeIf { it > 0 } ?: 1f // Avoid division by zero
                    val renormalizedProbs = (remainingProbabilities.map { it / totalRemainingProb }).toFloatArray()

                    // Find the class with the highest probability
                    val maxIndex = renormalizedProbs.indexOfMax()
                    val topClass = classNames[remainingIndices[maxIndex]] // Get class name from original list
                    val topProb = renormalizedProbs[maxIndex]

                    val dangerousClasses = listOf(
                        "Breaking", "Shout", "Bellow", "Yell", "Screaming", "Roar",
                        "Vehicle horn, car horn, honking", "Car alarm", "Explosion", "Slam",
                        "Emergency vehicle", "Police car (siren)", "Ambulance (siren)",
                        "Fire engine, fire truck (siren)", "Train whistle", "Train horn",
                        "Train wheels squealing", "Chainsaw", "Alarm", "Siren",
                        "Civil defense siren", "Smoke detector, smoke alarm", "Fire alarm",
                        "Explosion", "Gunshot, gunfire", "Machine gun", "Fusillade",
                        "Artillery fire", "Eruption", "Boom", "Bang", "Smash, crash", "Whip", "Crushing"
                    )

                    predictionState = if (topClass in dangerousClasses && topProb >= 0.4) {
                        "Dangerous sound detected: $topClass (Confidence: $topProb)"
                    } else {
                        "No dangerous sounds detected."
                    }
                    delay(500)
                } catch (e: Exception) {
                    predictionState = "Error: ${e.message}"
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isRecording = !isRecording },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.spideybelloff),
                    contentDescription = "Custom FAB",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Press FAB to detect dangerous sounds", modifier = Modifier.padding(bottom = 16.dp))
            Text(predictionState, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

// Extension function to find the index of the maximum value in a FloatArray
fun FloatArray.indexOfMax(): Int {
    var maxIndex = 0
    for (i in 1 until this.size) {
        if (this[i] > this[maxIndex]) {
            maxIndex = i
        }
    }
    return maxIndex
}
