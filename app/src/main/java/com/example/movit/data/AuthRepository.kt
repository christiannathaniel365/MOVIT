package com.example.movit.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    val currentUserId: String? get() = auth.currentUser?.uid
    val currentEmail: String? get() = auth.currentUser?.email

    /** Mengalirkan ID pengguna setiap kali status login berubah. */
    fun observeUserId(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        result.user?.uid ?: error("Akun gagal dibuat")
    }.withFriendlyError()

    suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        result.user?.uid ?: error("Gagal masuk")
    }.withFriendlyError()

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.withFriendlyError()

    fun signOut() = auth.signOut()

    /** Ubah error teknis Firebase jadi pesan yang mudah dipahami. */
    private fun <T> Result<T>.withFriendlyError(): Result<T> {
        val e = exceptionOrNull() ?: return this
        val message = when (e) {
            is FirebaseAuthWeakPasswordException -> "Password terlalu lemah. Gunakan minimal 6 karakter."
            is FirebaseAuthUserCollisionException -> "Email ini sudah terdaftar. Silakan masuk."
            is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan atau sudah dinonaktifkan."
            is FirebaseAuthInvalidCredentialsException -> "Email atau password tidak valid."
            is FirebaseNetworkException -> "Tidak ada koneksi internet."
            else -> e.message ?: "Terjadi kesalahan. Coba lagi."
        }
        return Result.failure(Exception(message, e))
    }
}