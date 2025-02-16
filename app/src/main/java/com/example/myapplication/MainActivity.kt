package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.myapplication.ui.theme.MyApplicationTheme


// Define screen routes using a sealed class
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Second : Screen("second")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

// Navigation setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Second.route) {
            SecondScreen(navController)
        }
    }
}

// Home Screen
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val textState = remember { mutableStateOf("") }
    val predictionState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter text to classify:", modifier = Modifier.padding(bottom = 16.dp))
        TextField(
            value = textState.value,
            onValueChange = { newText -> textState.value = newText },
            label = { Text("Enter your text") },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = {
            try {
                val input = arrayOf(floatArrayOf(textState.value.length.toFloat()))
                predictionState.value = "Prediction: ${input[0][0]}"
                Toast.makeText(context, "Prediction: ${input[0][0]}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                predictionState.value = "Error: ${e.message}"
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            textState.value = ""
        }) {
            Text("Submit")
        }

        Text(predictionState.value, modifier = Modifier.padding(top = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(Screen.Second.route) }) {
            Text("Go to Second Screen")
        }
    }
}

// Second Screen
@Composable
fun SecondScreen(navController: NavController) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(false) }

    // Request SMS permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    // Check permission on startup
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter a phone number:", modifier = Modifier.padding(bottom = 8.dp))

        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("Enter your message:", modifier = Modifier.padding(bottom = 8.dp))

        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = {
            if (phoneNumber.isNotBlank() && message.isNotBlank()) { // Ensure inputs are not empty
                if (hasPermission) {
                    sendSMS(context, phoneNumber, message)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                }
            } else {
                Toast.makeText(context, "Please enter both a phone number and a message", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Send SMS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}



@Composable
fun SendSmsScreen() {
    val context = LocalContext.current
    val phoneNumber = "8253437690"  // Replace with your actual number
    val message = "Hello from my app!"

    var hasPermission by remember { mutableStateOf(false) }

    // Request SMS permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    // Check permission on startup
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            if (hasPermission) {
                sendSMS(context, phoneNumber, message)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        }) {
            Text("Send SMS")
        }
    }
}

fun sendSMS(context: android.content.Context, phoneNumber: String, message: String) {
    try {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        Toast.makeText(context, "SMS Sent!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Preview for HomeScreen
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyApplicationTheme {
        HomeScreen(navController = rememberNavController())
    }
}

// Preview for SecondScreen
@Preview(showBackground = true)
@Composable
fun SecondScreenPreview() {
    MyApplicationTheme {
        SecondScreen(navController = rememberNavController())
    }
}
