package com.example.movit.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

/**
 * Layar Daftar (Buat akun).
 * Tiga input: nama panggilan, email, password.
 * verticalScroll dipakai karena isinya banyak dan bisa tertutup keyboard.
 */
@Composable
fun RegisterPage(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmitClick: (nickname: String, email: String, password: String) -> Unit = { _, _, _ -> },
    onLoginClick: () -> Unit = {},
) {
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val colors = MovitTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        // --- JUDUL ---
        Text(
            text = MovitStrings.Register.title,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.text,
        )

        Spacer(Modifier.height(4.dp))

        // --- SUBTITLE ---
        Text(
            text = MovitStrings.Register.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )

        Spacer(Modifier.height(24.dp))

        // --- NAMA PANGGILAN ---
        FieldLabel(MovitStrings.Register.nickname, colors.text)
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            placeholder = { Text(MovitStrings.Register.nicknameHint) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // --- EMAIL ---
        FieldLabel(MovitStrings.Common.email, colors.text)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(MovitStrings.Common.emailHint) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // --- PASSWORD ---
        FieldLabel(MovitStrings.Common.password, colors.text)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(MovitStrings.Common.passwordHint) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(6.dp))

        // --- BANTUAN PASSWORD (teks kecil di bawah input) ---
        Text(
            text = MovitStrings.Register.passwordHelp,
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
        )

        // --- PESAN ERROR (muncul hanya kalau ada) ---
        Spacer(Modifier.height(20.dp))
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(12.dp))
        }

        // --- TOMBOL BUAT AKUN ---
        Button(
            onClick = { onSubmitClick(nickname, email, password) },
            enabled = !isLoading,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp,
                )
            } else {
                Text(
                    text = MovitStrings.Register.submit,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- TEKS SYARAT & KETENTUAN (rata tengah) ---
        Text(
            text = MovitStrings.Register.terms,
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        // --- "Sudah punya akun? Masuk" ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row {
                Text(
                    text = MovitStrings.Register.haveAccount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = MovitStrings.Common.login,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Label kecil di atas tiap input. */
@Composable
private fun FieldLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
