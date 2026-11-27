package com.example.movit.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movit.data.AuthRepository
import com.example.movit.data.UserProfile
import com.example.movit.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel (
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
): ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String){
        if(email.isBlank() || password.isBlank()){
            _uiState.value = AuthUiState(errorMessage = "Email atau password wajib diisi.")
            return
        }


        _uiState.value = AuthUiState(isLoading = true)


        viewModelScope.launch {
            val result = authRepo.signIn(email, password)

            _uiState.value = if (result.isSuccess){
                AuthUiState(isSuccess = true)
            }else {
                AuthUiState(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(nickname: String, email: String, password: String){
        if (nickname.isBlank() || email.isBlank() || password.isBlank()){
            _uiState.value = AuthUiState(errorMessage = "Nama, email, dan password wajib diisi.")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)


        viewModelScope.launch {
            val result = authRepo.signUp(email, password)

            // Kalau daftar gagal, tampilkan error lalu berhenti
            if (result.isFailure){
                _uiState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.message)
                return@launch
            }

            // Daftar berhasil → simpan nickname ke users/{uid}
            val saveResult = userRepo.saveProfile(UserProfile(nickname = nickname.trim()))
            Log.d("MOVIT-Auth", "simpan profil: $saveResult")

            _uiState.value = AuthUiState(isSuccess = true)
        }
    }
    fun resetState() {
        _uiState.value = AuthUiState()
    }
    fun sendPasswordReset(email: String) {
        if (email.isBlank()){
            _uiState.value = AuthUiState(errorMessage = "Email wajib diisi.")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepo.sendPasswordReset(email)
            if (result.isFailure){
                _uiState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.message)
                return@launch
            }

            // Daftar berhasil → simpan nickname ke users/{uid}
            _uiState.value = AuthUiState(isSuccess = true)
        }
    }
}