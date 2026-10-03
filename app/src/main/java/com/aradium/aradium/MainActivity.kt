package com.aradium.aradium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aradium.aradium.ui.navigation.MainScreen
import com.aradium.aradium.ui.theme.AradiumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AradiumTheme {
                MainScreen()
            }
        }
    }
}