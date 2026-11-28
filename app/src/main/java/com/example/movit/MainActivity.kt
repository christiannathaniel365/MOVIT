package com.example.movit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.movit.ui.MovitApp
import com.example.movit.ui.auth.ForgotPasswordPage
import com.example.movit.ui.auth.LoginPage
import com.example.movit.ui.auth.LoginScreen
import com.example.movit.ui.auth.RegisterPage
import com.example.movit.ui.auth.RegisterScreen
import com.example.movit.ui.main.MainScreen
import com.example.movit.ui.onboarding.OnboardingPage
import com.example.movit.ui.splash.SplashScreen
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

enum class Screen {
    SPLASH,
    ONBOARD1,
    ONBOARD2,
    ONBOARD3,
    LOGIN,
    FORGOT,
    REGISTER,
    MAINSCREEN,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovitTheme {
                MovitApp()
            }
        }
    }
}
