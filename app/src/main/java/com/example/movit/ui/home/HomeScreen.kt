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
        HomeHeader(name = "Raka", dateText = "Selasa, 17 November", streak = 6)
        // Tahap 3: kartu Latihan hari ini
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

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun HomeScreenPreview() {
    MovitTheme { HomeScreen() }
}