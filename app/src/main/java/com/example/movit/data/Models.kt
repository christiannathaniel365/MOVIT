package com.example.movit.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/** Profil pengguna, diisi dari layar Setup profil. */
data class UserProfile(
    var nickname: String = "",
    var level: String = Level.PEMULA,
    var dailyTargetMin: Int = 15,
    var language: String = "id",
)

/** Satu sesi latihan yang sudah selesai. */
data class WorkoutSession(
    @DocumentId var id: String = "",
    var exercise: String = Exercise.SQUAT,
    var startedAt: Timestamp = Timestamp.now(),
    var durationSec: Int = 0,
    var reps: Int = 0,
    var holdSec: Int = 0,
    var postureScore: Int = 0,
    var corrections: Map<String, Long> = emptyMap(),
)

object Level {
    const val PEMULA = "pemula"
    const val MENENGAH = "menengah"
}

object Exercise {
    const val SQUAT = "squat"
    const val PUSH_UP = "push_up"
    const val PLANK = "plank"
    const val LUNGE = "lunge"
}