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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var tfLiteHelper: TFLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize TFLiteHelper
        tfLiteHelper = TFLiteHelper(this)

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting(
                            modifier = Modifier.padding(innerPadding),
                            tfLiteHelper = tfLiteHelper
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier, tfLiteHelper: TFLiteHelper) {
    val context = LocalContext.current
    val textState = remember { mutableStateOf("") } // State to hold the text input
    val predictionState = remember { mutableStateOf("") } // State for prediction result

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter text to classify:",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = textState.value,
            onValueChange = { newText -> textState.value = newText },
            label = { Text("Enter your text") },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = {
            try {
                // Convert input to a FloatArray (example: using text length as a float)
                val input = arrayOf(floatArrayOf(textState.value.length.toFloat()))  // Adjust input preprocessing if needed

                // Call the model prediction
                val result = tfLiteHelper.predict(input)
                predictionState.value = "Prediction: $result"
                Toast.makeText(context, "Prediction: $result", Toast.LENGTH_SHORT).show()


            } catch (e: Exception) {
                // Catch any exceptions and display an error message
                predictionState.value = "Error: ${e.message}"
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            // Reset the input field
            textState.value = ""
        }) {
            Text("Submit")
        }

        Text(
            text = predictionState.value,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting(tfLiteHelper = TFLiteHelper(LocalContext.current))
    }
}
