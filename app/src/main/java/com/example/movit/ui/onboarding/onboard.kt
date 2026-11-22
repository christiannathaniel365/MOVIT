package com.example.movit.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

@Composable
fun OnboardingPage(
    @DrawableRes image: Int,
    title: String,
    body: String,
    pageIndex: Int,
    onSkipClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    val colors = MovitTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {

        // --- Text "Lewati" (rata kanan) ---
        Text(
            text = MovitStrings.Common.skip,
            style = MaterialTheme.typography.titleSmall,
            color = colors.muted,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onSkipClick() },
        )

        Spacer(Modifier.weight(1f))

        // --- Image maskot (di tengah) ---
        Image(
            painter = painterResource(image),
            contentDescription = MovitStrings.Splash.logoDesc,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(32.dp))

        // --- Judul (tengah) ---
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // --- Body (tengah) ---
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))

        // --- INDIKATOR TITIK (otomatis) ---
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(3) { index ->
                val isActive = index == pageIndex
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (isActive) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) colors.accent else colors.subtle)
                )
                Spacer(Modifier.width(6.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Button "Lanjut" ---
        Button(
            onClick = onNextClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = MovitStrings.Common.next,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
