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
import com.example.myapplication.BackgroundImage
import com.example.myapplication.AudioRecorder
import com.example.myapplication.R
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ml.YAMNetHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var isImageOne by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var predictionState by remember { mutableStateOf("") }
    val yamNetHelper = remember { YAMNetHelper(context) }
    val audioRecorder = remember { AudioRecorder(context, durationInSeconds = 2) }

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
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val audioData = audioRecorder.startRecording()
                    val predictions = yamNetHelper.classifyAudio(audioData)
                    val firstPrediction = predictions[0]

                    val topPrediction = firstPrediction.maxOrNull()
                    val topIndex = firstPrediction.indexOfMax()
                    val topClass = classNames[topIndex]

                    if (topPrediction!! >= 0.5) {
                        predictionState = "Dangerous sound detected: $topClass (Confidence: $topPrediction)"
                    } else {
                        predictionState = "No dangerous sounds detected."
                    }
                } catch (e: Exception) {
                    predictionState = "Error: ${e.message}"
                }
            }
        } else {
            Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val audioData = audioRecorder.startRecording()
                                    val predictions = yamNetHelper.classifyAudio(audioData)
                                    val firstPrediction = predictions[2]

                                    val topPrediction = firstPrediction.maxOrNull()
                                    val topIndex = firstPrediction.indexOfMax()
                                    val topClass = classNames[topIndex]

                                    if (topPrediction!! >= 0.5) {
                                        predictionState = "Dangerous sound detected: $topClass (Confidence: $topPrediction)"
                                    } else {
                                        predictionState = "No dangerous sounds detected."
                                    }
                                } catch (e: Exception) {
                                    predictionState = "Error: ${e.message}"
                                }
                            }
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            (context as Activity),
                            Manifest.permission.RECORD_AUDIO
                        ) -> {
                            Toast.makeText(context, "Audio recording permission is required to detect sounds.", Toast.LENGTH_LONG).show()
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        else -> {
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (isImageOne) R.drawable.spideybelloff
                        else R.drawable.spideybellon
                    ),
                    contentDescription = "Custom FAB",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            BackgroundImage()

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

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    navController.navigate(Screen.Second.route)
                }) {
                    Text("Go to Second Screen")
                }

            }

        }
    }
}

// Function to find the index of the max value in a FloatArray
fun FloatArray.indexOfMax(): Int {
    var maxIndex = 0
    for (i in 1 until this.size) {
        if (this[i] > this[maxIndex]) {
            maxIndex = i
        }
    }
    return maxIndex
}
