package com.example.movit.ui.dbtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.movit.data.AuthRepository
import com.example.movit.data.Exercise
import com.example.movit.data.SessionRepository
import com.example.movit.data.UserProfile
import com.example.movit.data.UserRepository
import com.example.movit.data.WorkoutSession
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/** LAYAR TES SEMENTARA untuk mengecek koneksi Firebase. */
@Composable
fun DbTestScreen(modifier: Modifier = Modifier) {
    val authRepo = remember { AuthRepository() }
    val userRepo = remember { UserRepository() }
    val sessionRepo = remember { SessionRepository() }
    val scope = rememberCoroutineScope()

    val userId by remember { authRepo.observeUserId() }.collectAsState(initial = authRepo.currentUserId)
    val sessions by remember(userId) {
        if (userId == null) flowOf(emptyList()) else sessionRepo.observeSessions()
    }.collectAsState(initial = emptyList())

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Belum ada aksi") }
    var loading by remember { mutableStateOf(false) }

    fun runAction(label: String, action: suspend () -> Result<*>) {
        scope.launch {
            loading = true
            status = "$label…"
            status = action().fold(
                onSuccess = { "$label berhasil ✅" },
                onFailure = { "$label gagal: ${it.message}" },
            )
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tes koneksi Firebase", style = MaterialTheme.typography.headlineSmall)

        if (userId == null) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password (min. 6 karakter)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !loading, onClick = { runAction("Daftar") { authRepo.signUp(email, password) } }) {
                    Text("Daftar")
                }
                OutlinedButton(enabled = !loading, onClick = { runAction("Masuk") { authRepo.signIn(email, password) } }) {
                    Text("Masuk")
                }
            }
        } else {
            Text("Masuk sebagai: ${authRepo.currentEmail}", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !loading, onClick = {
                    runAction("Simpan profil") { userRepo.saveProfile(UserProfile(nickname = "Tes")) }
                }) { Text("Simpan profil") }
                Button(enabled = !loading, onClick = {
                    val saved = sessionRepo.saveSession(
                        WorkoutSession(
                            exercise = Exercise.SQUAT,
                            durationSec = 372,
                            reps = (10..30).random(),
                            postureScore = (60..95).random(),
                            corrections = mapOf("punggung_bungkuk" to 4L),
                        )
                    )
                    status = if (saved) "Sesi contoh disimpan ✅" else "Belum masuk"
                }) { Text("Simpan sesi") }
            }
            OutlinedButton(onClick = { authRepo.signOut(); status = "Sudah keluar" }) { Text("Keluar") }
        }

        Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text("Riwayat sesi (${sessions.size})", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sessions, key = { it.id }) { s ->
                Text("${s.exercise} · ${s.reps} rep · skor ${s.postureScore}%", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}