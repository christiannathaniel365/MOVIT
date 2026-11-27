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
//                val context = LocalContext.current
//                LoginScreen(
//                    onLoggedIn = {
//                        Toast.makeText(context, "Login berhasil!", Toast.LENGTH_SHORT).show()
//                    },
//                    onRegisterClick = {},
//                )
//                RegisterScreen(
//                    onRegistered = {
//                        Toast.makeText(context, "Daftar berhasil!", Toast.LENGTH_SHORT).show()
//                    },
//                    onLoginClick = {},
//                )
                // MainScreen()
            }
//            MovitTheme {
//                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
//
//                when (currentScreen) {
//
//                    Screen.SPLASH -> SplashScreen(
//                        onFinished = { currentScreen = Screen.ONBOARD1 }
//                    )
//
//                    Screen.ONBOARD1 -> OnboardingPage(
//                        image = R.drawable.onboard1_element,
//                        title = MovitStrings.Onboarding.title1,
//                        body = MovitStrings.Onboarding.body1,
//                        pageIndex = 0,
//                        onSkipClick = { currentScreen = Screen.LOGIN },
//                        onNextClick = { currentScreen = Screen.ONBOARD2 },
//                    )
//
//                    Screen.ONBOARD2 -> OnboardingPage(
//                        image = R.drawable.onboard2_element,
//                        title = MovitStrings.Onboarding.title2,
//                        body = MovitStrings.Onboarding.body2,
//                        pageIndex = 1,
//                        onSkipClick = { currentScreen = Screen.LOGIN },
//                        onNextClick = { currentScreen = Screen.ONBOARD3 },
//                    )
//
//                    Screen.ONBOARD3 -> OnboardingPage(
//                        image = R.drawable.onboard3_element,
//                        title = MovitStrings.Onboarding.title3,
//                        body = MovitStrings.Onboarding.body3,
//                        pageIndex = 2,
//                        onSkipClick = { currentScreen = Screen.LOGIN },
//                        onNextClick = { currentScreen = Screen.LOGIN },
//                    )
//
//                    Screen.LOGIN -> LoginPage(
//                        onForgotClick = { currentScreen = Screen.FORGOT },
//                        onRegisterClick = { currentScreen = Screen.REGISTER },
//                        onLoginClick = { email, password -> currentScreen = Screen.MAINSCREEN },
//                        onGoogleClick = { currentScreen = Screen.MAINSCREEN },
//                    )
//
//                    Screen.FORGOT -> ForgotPasswordPage(
//                        onBackClick = { currentScreen = Screen.LOGIN },
//                        onLoginClick = { currentScreen = Screen.LOGIN },
//                        onSendLinkClick = { email -> currentScreen = Screen.LOGIN },
//                    )
//
//                    Screen.REGISTER -> RegisterPage(
//                        onLoginClick = { currentScreen = Screen.LOGIN },
//                        onSubmitClick = { nickname, email, password -> currentScreen = Screen.LOGIN },
//                    )
//
//                    Screen.MAINSCREEN -> MainScreen()
//                }
//            }
        }
    }
}
