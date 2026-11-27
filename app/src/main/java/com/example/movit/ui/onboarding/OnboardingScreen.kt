package com.example.movit.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.movit.R
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

// Data satu halaman onboarding
private data class OnboardingItem(
    @DrawableRes val image: Int,
    val title: String,
    val body: String,
)

// Isi ketiga halaman
private val onboardingItems = listOf(
    OnboardingItem(
        image = R.drawable.onboard1_element,
        title = "Latihan dengan postur yang benar",
        body = "MOVIT membaca gerakan tubuhmu lewat kamera HP dan memberi tahu bagian yang perlu diperbaiki.",

    ),
    OnboardingItem(
        image = R.drawable.onboard2_element,
        title = "Koreksi langsung saat latihan",
        body = "Lihat garis tubuhmu di layar, dengar arahan suara, dan pantau repetisi secara real-time.",
    ),
    OnboardingItem(
        image = R.drawable.onboard3_element,
        title = "Pantau progres, jaga streak",
        body = "Ikuti program sesuai levelmu, kumpulkan badge, dan lihat perkembanganmu setiap minggu.",
    ),
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }

    val item = onboardingItems[page]
    val isLast = page == onboardingItems.lastIndex

    OnboardingPage(
        image = item.image,
        title = item.title,
        body = item.body,
        pageIndex = page,
        nextLabel = if (isLast) "Mulai sekarang" else MovitStrings.Common.next,
        onSkipClick = onFinish,
        onNextClick = {
            if (isLast) onFinish() else page++
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    MovitTheme { OnboardingScreen(onFinish = {}) }
}