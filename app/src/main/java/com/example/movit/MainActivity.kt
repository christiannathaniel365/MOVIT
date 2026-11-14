package com.example.movit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.movit.ui.dbtest.DbTestScreen
import com.example.movit.ui.theme.MovitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovitTheme {
                DbTestScreen()
            }
        }
    }
}