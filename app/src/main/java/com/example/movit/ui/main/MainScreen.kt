package com.example.movit.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movit.R
import com.example.movit.ui.theme.MovitTheme
import com.example.movit.ui.home.HomeScreen
import com.example.movit.ui.profile.ProfileScreen

// Daftar tab di bottom nav: label + ikon
enum class MovitTab(val label: String, @DrawableRes val icon: Int) {
    BERANDA("Beranda", R.drawable.ic_home),
    LATIHAN("Latihan", R.drawable.ic_latihan),
    PROGRES("Progres", R.drawable.ic_progres),
    PROFIL("Profil", R.drawable.ic_profil),
}

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    // Tab yang sedang dipilih. Berubah → layar digambar ulang
    var selectedTab by rememberSaveable { mutableStateOf(MovitTab.BERANDA) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MovitBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab -> selectedTab = tab },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (selectedTab){
                MovitTab.BERANDA -> HomeScreen()
                MovitTab.LATIHAN -> Text("Daftar gerakan")
                MovitTab.PROGRES -> Text("Grafik progres")
                MovitTab.PROFIL -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun MovitBottomBar(
    selectedTab: MovitTab,
    onTabSelected: (MovitTab) -> Unit,
) {
    Column {
        HorizontalDivider(thickness = 2.dp, color = MovitTheme.colors.border)
        NavigationBar(containerColor = MovitTheme.colors.surface) {
            MovitTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MovitTheme.colors.accent,
                        selectedTextColor = MovitTheme.colors.accent,
                        indicatorColor = MovitTheme.colors.accentSoft,
                        unselectedIconColor = MovitTheme.colors.muted,
                        unselectedTextColor = MovitTheme.colors.muted,
                    ),
                )
            }
        }
    }
}
