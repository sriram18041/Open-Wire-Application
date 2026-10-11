package com.sriramanappindi.openwire.data

import android.content.Context

/** Tracks whether the first-launch walkthrough has already been shown. */
object OnboardingStore {
    private const val PREFS_NAME = "openwire_prefs"
    private const val KEY_SEEN = "onboarding_seen"

    fun hasSeenOnboarding(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SEEN, true).apply()
    }
}
