package com.example.movit.data

import android.content.Context
import androidx.core.content.edit

class OnboardingPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("movit_prefs", Context.MODE_PRIVATE)
    private val keyDone = "onboarding_done"

    fun isDone(): Boolean = prefs.getBoolean(keyDone, false)

    fun setDone() {
        prefs.edit { putBoolean(keyDone, true) }
    }
}