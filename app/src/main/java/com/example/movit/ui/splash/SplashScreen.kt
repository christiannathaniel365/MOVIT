package com.example.movit.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movit.R
import com.example.movit.ui.theme.Emerald700
import com.example.movit.ui.theme.MovitStrings
import com.example.movit.ui.theme.MovitTheme
import com.example.movit.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit = {}
) {
    // Jalan sekali saat Splash muncul, tunggu 2 detik, lalu pindah
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Emerald700) // color/splash di Figma = #047857
    ) {
        // Konten tengah: logo + judul + tagline
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoBox()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = MovitStrings.appName,
                fontFamily = PlusJakartaSans,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = MovitStrings.Splash.tagline,
                fontFamily = PlusJakartaSans,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

    }
}

@Composable
private fun LogoBox() {
    Box(
        modifier = Modifier
            .size(140.dp)
            .dashedBorder(
                color = Color.White,
                strokeWidth = 2.dp,
                cornerRadius = 28.dp,
                dashLength = 10.dp,
                gapLength = 8.dp
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.large) // radius 20 dari MovitShapes
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.movit_logo),
                contentDescription = MovitStrings.Splash.logoDesc,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        }
    }
}

// Garis putus-putus di sekeliling logo (Compose tidak punya bawaannya)
private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp,
    dashLength: Dp,
    gapLength: Dp,
) = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx())
            )
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun SplashScreenPreview() {
    MovitTheme {
        SplashScreen()
    }
}