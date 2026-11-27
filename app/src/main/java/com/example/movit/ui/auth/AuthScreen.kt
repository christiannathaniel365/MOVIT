package com.example.movit.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    // Baca papan skor dari ViewModel
    val state by viewModel.uiState.collectAsState()

    // Kalau sukses, beri tahu yang memanggil (nanti: pindah ke Beranda)
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetState()
            onRegistered()
        }
    }

    RegisterPage(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onSubmitClick = { nickname, email, password ->
            viewModel.register(nickname, email, password)
        },
        onLoginClick = {
            viewModel.resetState()
            onLoginClick()
        },
    )


}
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    // Baca papan skor dari ViewModel
    val state by viewModel.uiState.collectAsState()

    // Kalau sukses, beri tahu yang memanggil (nanti: pindah ke Beranda)
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetState()   // bersihkan dulu
            onLoggedIn()             // baru pindah
        }
    }

    LoginPage(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onLoginClick = { email, password ->
            viewModel.login(email, password)
        },
        onRegisterClick = {
            viewModel.resetState()   // bersihkan error sebelum pindah
            onRegisterClick()
        },
    )

}