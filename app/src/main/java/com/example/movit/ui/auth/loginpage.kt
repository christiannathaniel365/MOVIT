package com.example.movit.ui.auth

import androidx.compose.foundation.Image
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movit.R
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

@Composable
fun LoginPage(
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onForgotClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val colors = MovitTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        // --- LOGO ---
        Image(
            painter = painterResource(R.drawable.greenmovit),
            contentDescription = MovitStrings.Splash.logoDesc,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(48.dp),
        )

        Spacer(Modifier.height(16.dp))

        // --- JUDUL "Masuk" ---
        Text(
            text = MovitStrings.Common.login,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.text,
        )

        Spacer(Modifier.height(4.dp))

        // --- SUBTITLE ---
        Text(
            text = MovitStrings.Login.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )

        Spacer(Modifier.height(24.dp))

        // --- LABEL + INPUT EMAIL ---
        Text(
            text = MovitStrings.Common.email,
            style = MaterialTheme.typography.titleSmall,
            color = colors.text,
            modifier = Modifier.padding(bottom = 6.dp),
        )
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

        // --- LABEL + INPUT PASSWORD ---
        Text(
            text = MovitStrings.Common.password,
            style = MaterialTheme.typography.titleSmall,
            color = colors.text,
            modifier = Modifier.padding(bottom = 6.dp),
        )
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

        Spacer(Modifier.height(12.dp))

        // --- LINK "Lupa password?" (rata kanan) ---
        Text(
            text = MovitStrings.Login.forgotPassword,
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotClick() },
        )

        Spacer(Modifier.height(20.dp))

        // --- TOMBOL "Masuk" ---
        Button(
            onClick = { onLoginClick(email, password) },
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
                text = MovitStrings.Common.login,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- PEMISAH "atau" ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
            Text(
                text = MovitStrings.Common.or,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
        }

        Spacer(Modifier.height(16.dp))

        // --- TOMBOL "Masuk dengan Google" ---
        OutlinedButton(
            onClick = onGoogleClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = MovitStrings.Login.withGoogle,
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
            )
        }

        Spacer(Modifier.height(24.dp))

        // --- "Belum punya akun? Daftar" ---
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = MovitStrings.Login.noAccount,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = MovitStrings.Common.register,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegisterClick() },
            )
        }
    }
}
