package com.example.movit.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

@Composable
fun ForgotPasswordPage(
    onBackClick: () -> Unit = {},
    onSendLinkClick: (email: String) -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    val colors = MovitTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        // --- TOMBOL KEMBALI ---
        Text(
            text = "‹",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.text,
            modifier = Modifier.clickable { onBackClick() },
        )

        Spacer(Modifier.height(16.dp))

        // --- JUDUL ---
        Text(
            text = MovitStrings.Forgot.title,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.text,
        )

        Spacer(Modifier.height(8.dp))

        // --- PENJELASAN ---
        Text(
            text = MovitStrings.Forgot.body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )

        Spacer(Modifier.height(24.dp))

        // --- LABEL EMAIL ---
        Text(
            text = MovitStrings.Common.email,
            style = MaterialTheme.typography.titleSmall,
            color = colors.text,
            modifier = Modifier.padding(bottom = 6.dp),
        )

        // --- INPUT EMAIL ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(MovitStrings.Common.emailHint) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        // --- TOMBOL KIRIM LINK ---
        Button(
            onClick = { onSendLinkClick(email) },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = MovitStrings.Forgot.sendLink,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // Dorong link "Ingat password?" ke bawah layar
        Spacer(Modifier.weight(1f))

        // --- "Ingat password? Masuk" ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row {
                Text(
                    text = MovitStrings.Forgot.remember,
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

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordPagePreview() {
    MovitTheme {
        ForgotPasswordPage()
    }
}
