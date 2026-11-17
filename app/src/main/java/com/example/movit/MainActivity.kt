package com.example.movit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.movit.ui.auth.ForgotPasswordPage
import com.example.movit.ui.auth.LoginPage
import com.example.movit.ui.auth.RegisterPage
import com.example.movit.ui.main.MainScreen
import com.example.movit.ui.splash.SplashScreen
import com.example.movit.ui.theme.MovitTheme

enum class Screen {
    SPLASH,
    LOGIN,
    FORGOT,
    REGISTER,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovitTheme {
                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

                when (currentScreen) {

                    // Splash → (selesai) → Login
                    Screen.SPLASH -> SplashScreen(
                        onFinished = { currentScreen = Screen.LOGIN }
                    )

                    // Login → "Lupa password?" → Forgot
                    // Login → "Daftar" → Register
                    Screen.LOGIN -> LoginPage(
                        onForgotClick = { currentScreen = Screen.FORGOT },
                        onRegisterClick = { currentScreen = Screen.REGISTER },
                        onLoginClick = { email, password -> },
                        onGoogleClick = { },
                    )

                    // Forgot → "kembali" / "Masuk" → Login
                    Screen.FORGOT -> ForgotPasswordPage(
                        onBackClick = { currentScreen = Screen.LOGIN },
                        onLoginClick = { currentScreen = Screen.LOGIN },
                        onSendLinkClick = { email -> },
                    )

                    // Register → "Masuk" → Login
                    Screen.REGISTER -> RegisterPage(
                        onLoginClick = { currentScreen = Screen.LOGIN },
                        onSubmitClick = { nickname, email, password -> },
                    )
                    Screen.REGISTER -> TODO()
                }
            }
        }
    }
}
