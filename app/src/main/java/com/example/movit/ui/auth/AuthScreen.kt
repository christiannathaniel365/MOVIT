package com.example.movit.ui.auth

import androidx.activity.compose.BackHandler
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

    // Tombol Back HP → kembali ke Login (bukan menutup app)
    BackHandler {
        viewModel.resetState()
        onLoginClick()
    }

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
    onForgotClick: () -> Unit,
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
        onForgotClick = {
            viewModel.resetState()
            onForgotClick()
        }
    )

}

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    BackHandler {
        viewModel.resetState()
        onBackClick()
    }
    ForgotPasswordPage(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        isSent = state.isSuccess,          // ingat: isSuccess dipakai sebagai tanda "link terkirim"
        onSendClick = { email ->
            viewModel.sendPasswordReset(email)
        },
        onBackClick = {
            viewModel.resetState()                // reset dulu
            onBackClick()                // baru kembali
        },
    )
}