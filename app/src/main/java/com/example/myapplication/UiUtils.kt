package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BackgroundImage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.spideypage),
            contentDescription = "Background Image",
            modifier = Modifier.matchParentSize() // Ensure it covers the full screen
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BackgroundImagePreview() {
    BackgroundImage()
}