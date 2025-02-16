package com.example.myapplication.screens

import android.widget.Toast
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
import androidx.navigation.NavController
import com.example.myapplication.BackgroundImage
import com.example.myapplication.R
import com.example.myapplication.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    var predictionState by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Toast.makeText(context, "FAB Clicked!", Toast.LENGTH_SHORT).show()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ✅ Background Image (Now ignores Scaffold padding)
            BackgroundImage()

            // ✅ Content Layout (Applies padding properly)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // This applies the scaffold's padding only to the content, NOT the background
                    .padding(16.dp), // Add spacing inside the content
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter text to classify:", modifier = Modifier.padding(bottom = 16.dp))
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Enter your text") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(onClick = {
                    try {
                        val input = arrayOf(floatArrayOf(textState.length.toFloat()))
                        predictionState = "Prediction: ${input[0][0]}"
                        Toast.makeText(context, "Prediction: ${input[0][0]}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        predictionState = "Error: ${e.message}"
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    textState = ""
                }) {
                    Text("Submit")
                }

                Text(predictionState, modifier = Modifier.padding(top = 16.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { navController.navigate(Screen.Second.route) }) {
                    Text("Go to Second Screen")
                }
            }
        }
    }
}
