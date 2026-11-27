package com.example.movit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.movit.data.AuthRepository
import com.example.movit.ui.auth.ForgotPasswordScreen
import com.example.movit.ui.auth.LoginScreen
import com.example.movit.ui.auth.RegisterScreen
import com.example.movit.ui.main.MainScreen

enum class AppScreen { LOGIN, REGISTER, FORGOT, MAIN }
@Composable
fun MovitApp() {
    val authRepo = remember { AuthRepository() }

    var screen by rememberSaveable {
        mutableStateOf(
            if (authRepo.currentUserId != null ) AppScreen.MAIN else AppScreen.LOGIN
        )
    }
    when(screen){
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
    }


}