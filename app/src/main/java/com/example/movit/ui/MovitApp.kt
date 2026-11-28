package com.example.movit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.movit.data.AuthRepository
import com.example.movit.data.OnboardingPrefs
import com.example.movit.ui.auth.ForgotPasswordScreen
import com.example.movit.ui.auth.LoginScreen
import com.example.movit.ui.auth.RegisterScreen
import com.example.movit.ui.main.MainScreen
import com.example.movit.ui.onboarding.OnboardingScreen
import com.example.movit.ui.splash.SplashScreen
import kotlinx.coroutines.delay

enum class AppScreen { SPLASH, ONBOARDING, LOGIN, REGISTER, FORGOT, MAIN }
@Composable
fun MovitApp() {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository() }
    val onboardingPrefs = remember { OnboardingPrefs(context) }

    var screen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }
    when(screen){
        AppScreen.SPLASH -> {
            LaunchedEffect(Unit) {
                delay(1500)   // tampil 1,5 detik
                screen = when {
                    authRepo.currentUserId != null -> AppScreen.MAIN

                    !onboardingPrefs.isDone()      -> AppScreen.ONBOARDING

                    else                           -> AppScreen.LOGIN
                }
            }
            SplashScreen()
        }
        AppScreen.LOGIN -> LoginScreen(
            onLoggedIn = { screen = AppScreen.MAIN },
            onRegisterClick = { screen = AppScreen.REGISTER },
            onForgotClick = {screen = AppScreen.FORGOT}
        )
        AppScreen.REGISTER -> RegisterScreen(
            onRegistered = { screen = AppScreen.MAIN },
            onLoginClick = { screen = AppScreen.LOGIN },
        )
        AppScreen.MAIN -> MainScreen(
            onLogout = {
                authRepo.signOut()
                screen = AppScreen.LOGIN
            },
        )

        AppScreen.FORGOT -> ForgotPasswordScreen(
            onBackClick = { screen = AppScreen.LOGIN }
        )
        AppScreen.ONBOARDING -> OnboardingScreen(
            onFinish = {
                onboardingPrefs.setDone()
                screen = AppScreen.LOGIN
            },
        )
    }


}