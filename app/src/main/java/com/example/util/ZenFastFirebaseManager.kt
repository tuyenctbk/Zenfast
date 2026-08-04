package com.example.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object ZenFastFirebaseManager {
    private const val TAG = "ZenFastFirebase"

    private var analytics: FirebaseAnalytics? = null
    private var remoteConfig: FirebaseRemoteConfig? = null

    fun initialize(context: Context) {
        try {
            analytics = FirebaseAnalytics.getInstance(context)
            Log.d(TAG, "Firebase Analytics initialized successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Analytics initialization skipped/failed: ${e.message}")
        }

        try {
            remoteConfig = FirebaseRemoteConfig.getInstance().apply {
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()
                setConfigSettingsAsync(configSettings)
                setDefaultsAsync(
                    mapOf(
                        "welcome_message" to "Welcome to ZenFast - Your Mindful Fasting & Hydration Companion",
                        "rate_app_fast_threshold" to 1L,
                        "daily_water_goal_default" to 2000L
                    )
                )
                fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Remote config fetched and activated")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase RemoteConfig initialization skipped/failed: ${e.message}")
        }
    }

    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        try {
            val bundle = Bundle().apply {
                params.forEach { (key, value) ->
                    putString(key, value)
                }
            }
            analytics?.logEvent(eventName, bundle)
            Log.d(TAG, "Logged event: $eventName with $params")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log event $eventName: ${e.message}")
        }
    }

    fun logFastStarted(targetHours: Float) {
        logEvent("fast_started", mapOf("target_hours" to targetHours.toString()))
    }

    fun logFastCompleted(actualHours: Float, isTargetReached: Boolean) {
        logEvent(
            "fast_completed",
            mapOf(
                "actual_hours" to String.format("%.1f", actualHours),
                "target_reached" to isTargetReached.toString()
            )
        )
    }

    fun logWaterLogged(amountMl: Int, todayTotalMl: Int) {
        logEvent(
            "water_logged",
            mapOf(
                "amount_ml" to amountMl.toString(),
                "today_total_ml" to todayTotalMl.toString()
            )
        )
    }

    fun logThemeChanged(themeModeName: String) {
        logEvent("theme_changed", mapOf("mode" to themeModeName))
    }

    fun logAppRated(stars: Int, feedback: String? = null) {
        logEvent(
            "app_rated",
            mapOf(
                "stars" to stars.toString(),
                "has_feedback" to (!feedback.isNull_blank()).toString()
            )
        )
    }

    fun logAppShared() {
        logEvent("app_shared")
    }

    fun getRemoteWelcomeMessage(): String {
        return try {
            remoteConfig?.getString("welcome_message")
                ?.takeIf { it.isNotBlank() }
                ?: "Welcome to ZenFast - Your Mindful Fasting & Hydration Companion"
        } catch (e: Exception) {
            "Welcome to ZenFast - Your Mindful Fasting & Hydration Companion"
        }
    }

    private fun String?.isNull_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
