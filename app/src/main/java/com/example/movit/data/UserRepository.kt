package com.example.movit.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Profil pengguna di Firestore: users/{uid} */
class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun userDoc() =
        db.collection("users").document(auth.currentUser?.uid ?: error("Belum masuk"))

    /** Simpan (atau timpa) profil. Dipanggil dari layar Setup profil. */
    suspend fun saveProfile(profile: UserProfile): Result<Unit> = runCatching {
        userDoc().set(profile).await()
        Unit
    }

    /** Ambil profil. Hasilnya null kalau pengguna belum pernah mengisi Setup profil. */
    suspend fun getProfile(): Result<UserProfile?> = runCatching {
        userDoc().get().await().toObject(UserProfile::class.java)
    }
}