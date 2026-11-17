package com.example.movit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.movit.R

// File font ada di app/src/main/res/font/ (nama file harus huruf kecil + underscore)
val PlusJakartaSans = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold),
    Font(R.font.plusjakartasans_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.35).toInt()) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

/**
 * Ukuran mengikuti desain:
 * - judul layar 24-30 ExtraBold
 * - judul bagian 17 ExtraBold
 * - isi 15-16 Regular
 * - keterangan kecil 12-13
 */
val MovitTypography = Typography(
    displayLarge = style(44, FontWeight.ExtraBold),     // nama MOVIT di splash
    headlineLarge = style(30, FontWeight.ExtraBold),    // Masuk, Buat akun
    headlineMedium = style(26, FontWeight.ExtraBold),   // Beranda, Latihan, Progres, Profil
    headlineSmall = style(24, FontWeight.ExtraBold),    // judul layar dengan tombol kembali
    titleLarge = style(20, FontWeight.ExtraBold),       // judul kartu program
    titleMedium = style(17, FontWeight.ExtraBold),      // judul bagian, teks tombol
    titleSmall = style(15, FontWeight.ExtraBold),       // judul item daftar
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(15, FontWeight.Normal, 22),
    bodySmall = style(13, FontWeight.Normal, 18),
    labelLarge = style(16, FontWeight.Bold),
    labelMedium = style(14, FontWeight.Bold),
    labelSmall = style(12, FontWeight.Bold),
)