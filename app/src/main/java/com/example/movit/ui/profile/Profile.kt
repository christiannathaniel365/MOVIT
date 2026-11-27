package com.example.movit.ui.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movit.R
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme

// ───────────── DATA ─────────────
// Bentuk data yang tampil di layar Profil.
data class UserProfile(
    val name: String,
    val level: String,
    val joined: String,
    val totalSessions: Int,
    val streak: Int,
    val badges: Int,
    val programName: String,
    val dailyTargetMinutes: Int,
)

// ───────────── LAYAR UTAMA ─────────────
@Composable
fun ProfileScreen(
    profile: UserProfile,
    onAchievementsClick: () -> Unit = {},
    onProgramClick: () -> Unit = {},
    onTargetClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Judul
        Text(
            text = MovitStrings.Profile.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MovitTheme.colors.text
        )

        // 2. Header: avatar + nama + subtitle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MovitTheme.colors.accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MovitTheme.colors.text
                )
                Text(
                    text = MovitStrings.Profile.subtitle(profile.level, profile.joined),
                    style = MaterialTheme.typography.bodySmall,
                    color = MovitTheme.colors.muted
                )
            }
        }

        // 3. Tiga kartu statistik
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                value = profile.totalSessions.toString(),
                label = MovitStrings.Profile.totalSessions,
                valueColor = MovitTheme.colors.text,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = profile.streak.toString(),
                label = MovitStrings.Profile.streak,
                valueColor = MovitTheme.colors.orange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = profile.badges.toString(),
                label = MovitStrings.Achievements.badge,
                valueColor = MovitTheme.colors.accent,
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Daftar menu
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MovitTheme.colors.surface,
            border = BorderStroke(1.dp, MovitTheme.colors.border)
        ) {
            Column {
                ProfileMenuItem(
                    iconRes = R.drawable.ic_trophy,
                    title = MovitStrings.Achievements.title,
                    subtitle = MovitStrings.Profile.achievementsDesc,
                    onClick = onAchievementsClick
                )
                HorizontalDivider(color = MovitTheme.colors.border)
                ProfileMenuItem(
                    iconRes = R.drawable.ic_calendar,
                    title = MovitStrings.Profile.myProgram,
                    subtitle = profile.programName,
                    onClick = onProgramClick
                )
                HorizontalDivider(color = MovitTheme.colors.border)
                ProfileMenuItem(
                    iconRes = R.drawable.ic_target,
                    title = MovitStrings.Profile.dailyTarget,
                    subtitle = MovitStrings.Profile.dailyTargetDesc(profile.dailyTargetMinutes),
                    onClick = onTargetClick
                )
                HorizontalDivider(color = MovitTheme.colors.border)
                ProfileMenuItem(
                    iconRes = R.drawable.ic_settings,
                    title = MovitStrings.Settings.title,
                    subtitle = MovitStrings.Profile.settingsDesc,
                    onClick = onSettingsClick
                )
            }
        }

        // 5. Tombol keluar
        LogoutButton(onLogout = onLogoutClick)
    }
}

// ───────────── KARTU STATISTIK ─────────────
@Composable
private fun StatCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MovitTheme.colors.surface,
        border = BorderStroke(1.dp, MovitTheme.colors.border)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MovitTheme.colors.text
            )
        }
    }
}

// ───────────── SATU BARIS MENU ─────────────
@Composable
private fun ProfileMenuItem(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MovitTheme.colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MovitTheme.colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MovitTheme.colors.text
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MovitTheme.colors.muted
            )
        }

        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MovitTheme.colors.muted
        )
    }
}

// ───────────── TOMBOL KELUAR ─────────────
@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MovitTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MovitTheme.colors.surface,
            contentColor = MovitTheme.colors.orange
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_profil),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = MovitStrings.Profile.logout,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

// ───────────── PREVIEW ─────────────
private val previewProfile = UserProfile(
    name = "abii",
    level = "Pemula",
    joined = "November 2026",
    totalSessions = 18,
    streak = 6,
    badges = 2,
    programName = "Dasar Kekuatan",
    dailyTargetMinutes = 15,
)

@Preview(showBackground = true, name = "Light")
@Composable
private fun ProfileLightPreview() {
    MovitTheme(darkTheme = false) {
        Box(Modifier.background(MovitTheme.colors.background)) {
            ProfileScreen(profile = previewProfile)
        }
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun ProfileDarkPreview() {
    MovitTheme(darkTheme = true) {
        Box(Modifier.background(MovitTheme.colors.background)) {
            ProfileScreen(profile = previewProfile)
        }
    }
}