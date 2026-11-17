package com.example.movit.ui.theme
/**
 * Semua teks UI MOVIT. Pakai: Text(MovitStrings.Login.subtitle)
 * Teks yang butuh isian berbentuk fungsi: MovitStrings.Home.greeting("Raka")
 */
object MovitStrings {
    const val appName = "MOVIT"

    // ===== UMUM (dipakai di banyak layar) =====
    object Common {
        const val email = "Email"
        const val password = "Password"
        const val emailHint = "nama@email.com"
        const val passwordHint = "Minimal 8 karakter"
        const val login = "Masuk"
        const val register = "Daftar"
        const val next = "Lanjut"
        const val skip = "Lewati"
        const val back = "Kembali"
        const val yes = "Ya"
        const val no = "Tidak"
        const val or = "atau"
        const val startWorkout = "Mulai latihan"
        const val levelBeginner = "Pemula"
        const val levelIntermediate = "Menengah"
        const val reps = "Repetisi"
        const val postureCorrect = "Postur tepat"
        const val duration = "Durasi"
        const val countReps = "Hitung rep"
        const val timer = "Timer"
    }

    // ===== NAVIGASI BAWAH =====
    object Nav {
        const val home = "Beranda"
        const val exercises = "Latihan"
        const val progress = "Progres"
        const val profile = "Profil"
    }

    // ===== 01 SPLASH =====
    object Splash {
        const val tagline = "“Mau FIT? ya MOVIT”"
        const val logoDesc = "Logo MOVIT"
    }

    // ===== 02 ONBOARDING =====
    object Onboarding {
        const val title1 = "Latihan dengan postur yang benar"
        const val body1 = "MOVIT membaca gerakan tubuhmu lewat kamera HP dan memberi tahu bagian yang perlu diperbaiki."
        const val title2 = "Koreksi langsung saat latihan"
        const val body2 = "Lihat garis tubuhmu di layar, dengar arahan suara, dan pantau repetisi secara real-time."
        const val title3 = "Pantau progres, jaga streak"
        const val body3 = "Ikuti program sesuai levelmu, kumpulkan badge, dan lihat perkembanganmu setiap minggu."
        const val start = "Mulai sekarang"
    }

    // ===== 03 MASUK =====
    object Login {
        const val subtitle = "Selamat datang kembali! Yuk lanjut latihan."
        const val forgotPassword = "Lupa password?"
        const val withGoogle = "Masuk dengan Google"
        const val noAccount = "Belum punya akun?"
    }

    // ===== 03b LUPA PASSWORD =====
    object Forgot {
        const val title = "Lupa password?"
        const val body = "Masukkan email akunmu. Kami akan kirim link untuk membuat password baru."
        const val sendLink = "Kirim link reset"
        const val remember = "Ingat password?"
    }

    // ===== 04 DAFTAR =====
    object Register {
        const val title = "Buat akun"
        const val subtitle = "Gratis, cuma butuh satu menit."
        const val nickname = "Nama panggilan"
        const val nicknameHint = "Contoh: Raka"
        const val passwordHelp = "Gunakan kombinasi huruf dan angka."
        const val submit = "Buat akun"
        const val terms = "Dengan mendaftar, kamu menyetujui Syarat & Ketentuan dan Kebijakan Privasi."
        const val haveAccount = "Sudah punya akun?"
    }

    // ===== 05 SETUP PROFIL =====
    object Setup {
        const val title = "Atur profil latihanmu"
        const val subtitle = "Biar MOVIT bisa menyesuaikan program untukmu."
        const val levelLabel = "Level kamu"
        const val beginnerDesc = "Baru mulai atau jarang olahraga"
        const val intermediateDesc = "Sudah rutin latihan 2–3x seminggu"
        const val dailyTargetLabel = "Target latihan harian"
        const val minutes = "menit"
        const val injuryLabel = "Sedang sakit / cedera?"
    }

    // ===== 06 BERANDA =====
    object Home {
        fun greeting(name: String) = "Halo, $name!"
        const val todayWorkout = "Latihan hari ini"
        const val dailyTarget = "Target harian"
        fun dailyProgress(done: Int, target: Int) = "$done / $target menit"
        const val nextBadge = "Berikutnya"
        fun continueWith(exercise: String) = "Lanjutkan: $exercise"
        const val lastResult = "Hasil terakhir"
        const val seeDetail = "Lihat detail"
        const val nextFocus = "Fokus berikutnya"
    }

    // ===== 07 DAFTAR GERAKAN =====
    object Exercises {
        const val title = "Latihan"
        const val subtitle = "Pilih gerakan yang mau kamu latih."
        const val filterAll = "Semua"
        const val filterLegs = "Kaki"
        const val filterChestArms = "Dada & lengan"
        const val filterCore = "Core"
        const val programBanner = "Bingung mulai dari mana? Ikuti program latihan"
    }

    // ===== 08 DETAIL GERAKAN =====
    object Detail {
        const val howTo = "Cara melakukan"
        const val commonMistakes = "Kesalahan yang sering terjadi"
        const val sessionTarget = "Target sesi"
        fun targetReps(sets: Int, reps: Int) = "$sets set × $reps rep"
    }

    // ===== 09 PERSIAPAN KAMERA =====
    object Camera {
        const val title = "Siapkan kamera"
        const val distance = "2–3 meter"
        const val tipPlace = "Letakkan HP berdiri di lantai atau meja"
        const val tipStepBack = "Mundur sampai seluruh tubuh terlihat"
        const val tipLight = "Pastikan ruangan cukup terang"
        const val voiceTitle = "Arahan suara"
        const val voiceDesc = "Koreksi dibacakan saat latihan"
        const val ready = "Saya siap"
    }

    // ===== 10 LIVE DETECTION =====
    object Live {
        fun setInfo(exercise: String, set: Int, total: Int) = "$exercise · Set $set dari $total"
        fun repTarget(target: Int) = "/ $target"
        const val pausedTitle = "Latihan dijeda"
        fun pausedInfo(set: Int, done: Int, target: Int) = "Set $set · $done dari $target rep selesai"
        const val resume = "Lanjutkan"
        const val repeatSet = "Ulangi set ini"
        const val endSession = "Akhiri sesi"
    }

    // ===== 11 HASIL SESI =====
    object Result {
        fun title(name: String) = "Mantap, $name!"
        fun subtitle(exercise: String) = "Sesi $exercise selesai"
        fun streak(days: Int) = "Streak $days hari!"
        const val streakDesc = "Latihan lagi besok supaya tidak putus."
        const val topCorrections = "Koreksi yang paling sering"
        const val done = "Selesai"
        const val again = "Latihan lagi"
    }

    // ===== 12-13 PROGRAM =====
    object Program {
        const val title = "Program latihan"
        const val following = "Sedang diikuti"
        fun week(n: Int) = "Minggu $n"
        fun weekOf(n: Int, total: Int) = "Minggu $n dari $total"
        fun daysDone(done: Int, total: Int) = "$done / $total hari latihan"
        fun day(n: Int) = "Hari $n"
        const val today = "Hari ini"
        const val rest = "Istirahat"
        const val restDesc = "Biarkan ototmu pulih"
        fun locked(day: Int) = "Terbuka setelah Hari $day selesai"
        fun startDay(day: Int) = "Mulai Hari $day"
    }

    // ===== 14 PROGRES =====
    object Progress {
        const val title = "Progres"
        const val thisWeek = "Minggu ini"
        const val thisMonth = "Bulan ini"
        const val sessions = "Sesi"
        const val minutes = "Menit"
        const val avgPosture = "Rata-rata postur"
        const val postureScore = "Skor postur"
        const val history = "Riwayat sesi"
    }

    // ===== 15 PENCAPAIAN =====
    object Achievements {
        const val title = "Pencapaian"
        fun streakDays(days: Int) = "$days hari"
        const val streakRunning = "Streak berjalan"
        fun streakRecord(days: Int) = "Rekor terpanjang: $days hari"
        const val badge = "Badge"
        fun badgeCount(earned: Int, total: Int) = "$earned dari $total"
        const val earned = "Didapat"
    }

    // ===== 16 PROFIL =====
    object Profile {
        const val title = "Profil"
        const val totalSessions = "Total sesi"
        const val streak = "Streak"
        const val achievementsDesc = "Streak dan badge"
        const val myProgram = "Program saya"
        const val dailyTarget = "Target harian"
        const val settingsDesc = "Tema, bahasa, suara"
        const val logout = "Keluar"
    }

    // ===== 17 PENGATURAN =====
    object Settings {
        const val title = "Pengaturan"
        const val sectionDisplay = "TAMPILAN"
        const val theme = "Tema"
        const val themeLight = "Terang"
        const val themeDark = "Gelap"
        const val themeSystem = "Sistem"
        const val sectionWorkout = "SAAT LATIHAN"
        const val voiceDesc = "Koreksi postur dibacakan"
        const val countdown = "Hitung mundur"
        const val countdownDesc = "3 detik sebelum mulai"
        const val sectionNotification = "NOTIFIKASI"
        const val reminder = "Pengingat latihan"
        fun reminderDesc(time: String) = "Setiap hari pukul $time"
    }
}