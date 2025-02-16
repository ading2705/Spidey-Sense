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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.AudioRecorder
import com.example.myapplication.R
import com.example.myapplication.ml.YAMNetHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var predictionState by remember { mutableStateOf("") }
    val yamNetHelper = remember { YAMNetHelper(context) }
    val audioRecorder = remember { AudioRecorder(context) }

    val classNames = remember {
        context.resources.openRawResource(R.raw.yamnet_class_map)
            .bufferedReader()
            .useLines { lines ->
                lines.drop(1) // Skip the header row
                    .map { it.split(",")[2].trim('"') } // Extract the class names
                    .toList()
            }
    }

    // Permission launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with recording
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Record audio
                    val audioData = audioRecorder.startRecording()

                    // Classify the audio data
                    val predictions = yamNetHelper.classifyAudio(audioData)

                    // Use the first set of predictions (output[0])
                    val firstPrediction = predictions[0]

                    // Define dangerous classes
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

                    // Get the top prediction
                    val topPrediction = firstPrediction.maxOrNull()
                    val topIndex = firstPrediction.indexOfMax() // Use the custom function
                    val topClass = classNames[topIndex] // Replace with your class names

                    // Check if the top prediction is dangerous
                    if (topClass in dangerousClasses && topPrediction!! >= 0.5) {
                        predictionState = "Dangerous sound detected: $topClass (Confidence: $topPrediction)"
                    } else {
                        predictionState = "No dangerous sounds detected."
                    }
                } catch (e: SecurityException) {
                    // Handle permission denial
                    predictionState = "Error: ${e.message}"
                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    predictionState = "Error: ${e.message}"
                }
            }
        } else {
            // Permission denied, show a message
            Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Check and request permission
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // Permission already granted, start recording
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    // Record audio
                                    val audioData = audioRecorder.startRecording()

                                    // Classify the audio data
                                    val predictions = yamNetHelper.classifyAudio(audioData)

                                    // Use the first set of predictions (output[0])
                                    val firstPrediction = predictions[0]

                                    // Define dangerous classes
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

                                    // Get the top prediction
                                    val topPrediction = firstPrediction.maxOrNull()
                                    val topIndex = firstPrediction.indexOfMax() // Use the custom function
                                    val topClass = classNames[topIndex] // Replace with your class names

                                    // Check if the top prediction is dangerous
                                    if (topClass in dangerousClasses && topPrediction!! >= 0.5) {
                                        predictionState = "Dangerous sound detected: $topClass (Confidence: $topPrediction)"
                                    } else {
                                        predictionState = "No dangerous sounds detected."
                                    }
                                } catch (e: SecurityException) {
                                    // Handle permission denial
                                    predictionState = "Error: ${e.message}"
                                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    predictionState = "Error: ${e.message}"
                                }
                            }
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            (context as Activity),
                            Manifest.permission.RECORD_AUDIO
                        ) -> {
                            // Explain why the permission is needed
                            Toast.makeText(context, "Audio recording permission is required to detect sounds.", Toast.LENGTH_LONG).show()
                            // Request permission
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        else -> {
                            // Request permission
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
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

// Replace this with your actual class names
//val classNames = listOf(
//    "Breaking", "Shout", "Bellow", "Yell", "Screaming", "Roar",
//    "Vehicle horn, car horn, honking", "Car alarm", "Explosion", "Slam",
//    "Emergency vehicle", "Police car (siren)", "Ambulance (siren)",
//    "Fire engine, fire truck (siren)", "Train whistle", "Train horn",
//    "Train wheels squealing", "Chainsaw", "Alarm", "Siren",
//    "Civil defense siren", "Smoke detector, smoke alarm", "Fire alarm",
//    "Explosion", "Gunshot, gunfire", "Machine gun", "Fusillade",
//    "Artillery fire", "Eruption", "Boom", "Bang", "Smash, crash", "Whip", "Crushing"
//)