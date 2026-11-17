package com.example.movit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Token warna MOVIT, sama persis dengan desain di canvas.
 * Warna yang berubah antara light dan dark ada di MovitColors.
 * Warna yang sama di kedua mode ada di bawahnya.
 */
@Immutable
data class MovitColors(
    val background: Color,
    val surface: Color,        // kartu, bottom nav, input
    val subtle: Color,         // chip, track progress bar, segmented control
    val text: Color,
    val muted: Color,          // teks sekunder
    val border: Color,
    val accent: Color,         // teks/ikon emerald
    val accentSoft: Color,     // latar ikon & kartu terpilih
    val accentBorder: Color,   // border dashed (slot maskot, banner)
    val selected: Color,       // border opsi yang dipilih
    val buttonShadow: Color,   // bayangan 4dp di bawah tombol utama
    val orange: Color,         // streak & koreksi postur
    val orangeSoft: Color,
    val orangeBorder: Color,
    val barSoft: Color,        // batang grafik non-aktif
    val switchOff: Color,
    val isDark: Boolean,
)

val LightMovitColors = MovitColors(
    background = Color(0xFFF9FAFB),
    surface = Color(0xFFFFFFFF),
    subtle = Color(0xFFF3F4F6),
    text = Color(0xFF111827),
    muted = Color(0xFF4B5563),
    border = Color(0xFFE5E7EB),
    accent = Color(0xFF047857),
    accentSoft = Color(0xFFECFDF5),
    accentBorder = Color(0xFFA7F3D0),
    selected = Color(0xFF10B981),
    buttonShadow = Color(0xFF064E3B),
    orange = Color(0xFFC2410C),
    orangeSoft = Color(0xFFFFF7ED),
    orangeBorder = Color(0xFFFED7AA),
    barSoft = Color(0xFFA7F3D0),
    switchOff = Color(0xFF9CA3AF),
    isDark = false,
)

val DarkMovitColors = MovitColors(
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    subtle = Color(0xFF273449),
    text = Color(0xFFF1F5F9),
    muted = Color(0xFF94A3B8),
    border = Color(0xFF334155),
    accent = Color(0xFF34D399),
    accentSoft = Color(0xFF0B3B30),
    accentBorder = Color(0xFF065F46),
    selected = Color(0xFF10B981),
    buttonShadow = Color(0xFF022C22),
    orange = Color(0xFFFB923C),
    orangeSoft = Color(0xFF3B2412),
    orangeBorder = Color(0xFF7C2D12),
    barSoft = Color(0xFF065F46),
    switchOff = Color(0xFF475569),
    isDark = true,
)

// Sama di light & dark
val Emerald700 = Color(0xFF047857)   // tombol utama (teks putih di atasnya)
val Emerald600 = Color(0xFF059669)   // switch menyala
val Emerald500 = Color(0xFF10B981)   // progress bar
val StreakOrange = Color(0xFFEA580C) // lingkaran hari streak
val CorrectionOrange = Color(0xFFC2410C) // banner koreksi di layar live detection
val CorrectionOrangeShadow = Color(0xFF7C2D12)
val SkeletonGood = Color(0xFF34D399) // garis tubuh yang benar
val SkeletonBad = Color(0xFFFB923C)  // garis tubuh yang salah