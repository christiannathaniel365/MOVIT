package com.example.movit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movit.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.example.movit.ui.theme.Emerald700
import com.example.movit.ui.theme.MovitTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeHeader(name = "Raka", dateText = "Selasa, 17 November", streak = 0)
        TodayWorkoutCard(
            programName = "Dasar Kekuatan",
            day = 3,
            minutesDone = 16,
            minutesTarget = 15,
        )
        // Tahap 4: kartu Hasil terakhir
    }
}

@Composable
fun HomeHeader(name: String, dateText: String, streak: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(dateText, style = MaterialTheme.typography.bodyMedium, color = MovitTheme.colors.muted)
            Text("Halo, $name!", style = MaterialTheme.typography.headlineMedium, color = MovitTheme.colors.text)
        }
        StreakPill(streak = streak)
    }
}

@Composable
fun StreakPill(streak: Int) {
    val isStreak = streak > 0
    val backgroundColor = if (isStreak) MovitTheme.colors.orangeSoft else MovitTheme.colors.subtle
    val contentColor = if (isStreak) MovitTheme.colors.orange else MovitTheme.colors.muted
    val borderWidth = if (isStreak) 2.dp  else 0.dp

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(borderWidth, MovitTheme.colors.orangeBorder, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flame),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Text("$streak", style = MaterialTheme.typography.titleMedium, color = contentColor )
    }
}
@Composable
fun TodayWorkoutCard(
    programName: String,
    day: Int,
    minutesDone: Int,
    minutesTarget: Int,
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MovitTheme.colors.buttonShadow, shape) // lapisan bayangan (hijau tua)
            .padding(bottom = 4.dp)
            .background(Emerald700, shape)                     // kartu hijau utama
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val isDone = if (minutesDone >= minutesTarget) "Target tercapai!" else "$minutesDone / $minutesTarget menit"
        Column {
            Text(
                "Latihan hari ini",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
            )
            Text(
                "$programName · hari $day",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Target harian", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Text(isDone, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }

        ProgressBar(progress = minutesDone.toFloat() / minutesTarget)

        // Tahap 3b: daftar gerakan + tombol Lanjutkan
    }
}

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f)), // jalur (track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))  // isi sesuai persentase
                .fillMaxHeight()
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun HomeScreenPreview() {
    MovitTheme { HomeScreen() }
}