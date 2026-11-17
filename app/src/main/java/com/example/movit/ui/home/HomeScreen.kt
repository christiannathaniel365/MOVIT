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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import com.example.movit.ui.theme.Emerald500
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
enum class ExerciseStatus { DONE, NEXT, TODO }

data class TodayExercise(
    val name: String,
    val target: String,
    val status: ExerciseStatus,
    val score: Int? = null,   // hanya ada untuk gerakan yang sudah selesai
)
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
            minutesDone = 9,
            minutesTarget = 15,
            exercises = listOf(
                TodayExercise("Squat", "3×10", ExerciseStatus.DONE, score = 86),
                TodayExercise("Push-up", "3×8", ExerciseStatus.NEXT),
                TodayExercise("Plank", "3×20 detik", ExerciseStatus.TODO),
            ),
        )
        LastResultCard(
            exerciseName = "Squat",
            whenText = "kemarin",
            score = 86,
            reps = 30,
            duration = "6:12",
            trend = listOf(72, 76, 78, 86, 82),
            focusTip = "Jaga punggung tetap tegak",
            focusCount = 4,
        )
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
    exercises: List<TodayExercise>,   // ← baru
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
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            exercises.forEachIndexed { index, exercise ->
                ExerciseRow(number = index + 1, exercise = exercise)
            }
        }

        val next = exercises.firstOrNull { it.status == ExerciseStatus.NEXT }
        if (next != null) {
            CardButton(text = "Lanjutkan: ${next.name}", onClick = { /* nanti: buka Live detection */ })
        }
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

@Composable
fun ExerciseRow(number: Int, exercise: TodayExercise) {
    val shape = RoundedCornerShape(12.dp)
    val isNext = exercise.status == ExerciseStatus.NEXT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = if (isNext) 0.22f else 0.14f))
            .border(2.dp, if (isNext) Color.White else Color.Transparent, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatusDot(number = number, status = exercise.status)
        Text(
            text = "${exercise.name} · ${exercise.target}",
            modifier = Modifier.weight(1f),
            style = if (isNext) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (exercise.status == ExerciseStatus.DONE) Color.White.copy(alpha = 0.75f) else Color.White,
        )
        when (exercise.status) {
            ExerciseStatus.DONE -> Text(
                "${exercise.score}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
            )
            ExerciseStatus.NEXT -> Text(
                "Berikutnya",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            ExerciseStatus.TODO ->{} // tidak menampilkan apa-apa di kanan
        }
    }
}

@Composable
fun StatusDot(number: Int, status: ExerciseStatus) {
    val dot = Modifier.size(24.dp).clip(CircleShape)
    when (status) {
        ExerciseStatus.DONE -> Box(dot.background(Color.White), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_check), null, tint = Emerald700, modifier = Modifier.size(14.dp))
        }
        ExerciseStatus.NEXT -> Box(dot.background(Emerald500), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_arrow_forward), null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        ExerciseStatus.TODO -> Box(dot.border(1.5.dp,Color.White.copy(alpha = 0.55f), CircleShape), contentAlignment = Alignment.Center){
            Text("$number", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
fun CardButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MovitTheme.colors.buttonShadow, shape) // bayangan bawah
            .padding(bottom = 4.dp)
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_play), null, tint = Emerald700, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = Emerald700)
    }
}

@Composable
fun MovitCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 14.dp,
    spacing: Dp = 12.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(MovitTheme.colors.border, RoundedCornerShape(cornerRadius))         // warna garis tepi
            .padding(start = 2.dp, top = 2.dp, end = 2.dp, bottom = 4.dp)                  // bawah lebih tebal
            .background(MovitTheme.colors.surface, RoundedCornerShape(cornerRadius - 2.dp)) // isi kartu
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun LastResultCard(
    exerciseName: String,
    whenText: String,
    score: Int,
    reps: Int,
    duration: String,
    trend: List<Int>,
    focusTip: String,
    focusCount: Int,
) {
    MovitCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Hasil terakhir", style = MaterialTheme.typography.titleMedium, color = MovitTheme.colors.text)
                Text("$exerciseName · $whenText", style = MaterialTheme.typography.bodySmall, color = MovitTheme.colors.muted)
            }
            Text(
                "Lihat detail ›",
                style = MaterialTheme.typography.labelMedium,
                color = MovitTheme.colors.accent,
                modifier = Modifier.clickable { /* nanti: buka Hasil sesi */ },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(value = "$score%", label = "Postur tepat", modifier = Modifier.weight(1f), valueColor = MovitTheme.colors.accent)
            StatTile(value = "$reps", label = "Repetisi", modifier = Modifier.weight(1f))
            StatTile(value = duration, label = "Durasi", modifier = Modifier.weight(1f))
        }

        TrendBars(exerciseName = exerciseName, scores = trend)
        FocusTip(tip = focusTip, count = focusCount)
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = MovitTheme.colors.text) {
    MovitCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        contentPadding = 10.dp,
        spacing = 0.dp,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color =valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MovitTheme.colors.muted)
    }
}

@Composable
fun TrendBars(exerciseName: String, scores: List<Int>) {
    val change = scores.last() - scores[scores.size - 2]   // skor terakhir - skor sebelumnya
    val trendText = when {
        change > 0 -> "Naik $change%"
        change < 0 -> "Turun ${abs(change)}%"
        else -> "Sama seperti sesi lalu"
    }
    val trendColor = when {
        change > 0 -> MovitTheme.colors.accent
        change < 0 -> MovitTheme.colors.orange
        else -> MovitTheme.colors.muted
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${scores.size} sesi ${exerciseName.lowercase()} terakhir",
                style = MaterialTheme.typography.bodySmall,
                color = MovitTheme.colors.muted,
            )
            Text(trendText, style = MaterialTheme.typography.labelSmall, color = trendColor)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,   // batang tumbuh dari bawah
        ) {
            scores.forEachIndexed { index, score ->
                val isLast = index == scores.lastIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(score / 100f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLast) MovitTheme.colors.selected else MovitTheme.colors.barSoft),
                )
            }
        }
    }
}

@Composable
fun FocusTip(tip: String, count: Int) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MovitTheme.colors.orangeSoft)
            .border(2.dp, MovitTheme.colors.orangeBorder, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text("Fokus berikutnya", style = MaterialTheme.typography.labelMedium, color = MovitTheme.colors.orange)
        Text("$tip · dikoreksi $count×", style = MaterialTheme.typography.bodySmall, color = MovitTheme.colors.orange)
    }
}
@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun HomeScreenPreview() {
    MovitTheme { HomeScreen() }
}