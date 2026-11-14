package com.example.movit.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Riwayat sesi latihan di Firestore: users/{uid}/sessions/{id} */
class SessionRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun sessionsRef(uid: String) =
        db.collection("users").document(uid).collection("sessions")

    /**
     * Simpan satu sesi. Sengaja TIDAK menunggu server, supaya tetap jalan saat offline.
     * @return false kalau pengguna belum masuk.
     */
    fun saveSession(session: WorkoutSession): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        sessionsRef(uid).add(session)
            .addOnFailureListener { Log.e(TAG, "Gagal menyimpan sesi", it) }
        return true
    }

    /** Mengalirkan daftar sesi terbaru, otomatis update setiap ada perubahan. */
    fun observeSessions(limit: Long = 50): Flow<List<WorkoutSession>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = sessionsRef(uid)
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Gagal membaca sesi", error)
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents
                    ?.mapNotNull { it.toObject(WorkoutSession::class.java) }
                    .orEmpty()
                trySend(sessions)
            }
        awaitClose { registration.remove() }
    }

    companion object {
        private const val TAG = "MOVIT-Session"
    }
}