package com.diffusethinking.cognitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.diffusethinking.cognitrack.ui.screens.MainScreen
import com.diffusethinking.cognitrack.ui.theme.CogniTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CogniTrackTheme {
                MainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
