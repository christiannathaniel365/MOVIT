package com.example.movit.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movit.ui.theme.MovitTheme

@Composable
fun ForgotPasswordPage(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isSent: Boolean = false,
    onSendClick: (email: String) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    val colors = MovitTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        // --- JUDUL ---
        Text(
            text = "Lupa password?",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.text,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Masukkan email akunmu. Kami akan kirim link untuk membuat password baru.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )

        Spacer(Modifier.height(24.dp))

        if (isSent) {
            // --- KOTAK SUKSES (menggantikan form) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.accentSoft, MaterialTheme.shapes.medium)
                    .border(2.dp, colors.accentBorder, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Cek email kamu ✉️",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.accent,
                )
                Text(
                    text = "Kalau email ini terdaftar, link untuk membuat password baru sudah dikirim. Cek juga folder Spam.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.text,
                )
            }
        } else {
            // --- FORM EMAIL ---
            Text(
                text = "Email",
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("nama@email.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { onSendClick(email) },
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
                    Text("Kirim link", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- "Ingat password? Masuk" ---
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Ingat password?",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordFormPreview() {
    MovitTheme { ForgotPasswordPage() }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordSentPreview() {
    MovitTheme { ForgotPasswordPage(isSent = true) }
}