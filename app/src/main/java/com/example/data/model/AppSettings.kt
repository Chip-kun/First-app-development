package com.example.data.model

import android.content.Context
import android.content.SharedPreferences

data class AppSettings(
    val autoSearchTarget: SearchTarget = SearchTarget.ALL_HUB,
    val autoOpenBrowser: Boolean = false,
    val autoOpenUrls: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val continuousScan: Boolean = false,
    val preferredCountry: String = "JP"
) {
    companion object {
        private const val PREFS_NAME = "barcode_search_prefs"
        private const val KEY_TARGET = "auto_search_target"
        private const val KEY_AUTO_OPEN = "auto_open_browser"
        private const val KEY_AUTO_OPEN_URLS = "auto_open_urls"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_CONTINUOUS = "continuous_scan"

        fun load(context: Context): AppSettings {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val targetName = prefs.getString(KEY_TARGET, SearchTarget.ALL_HUB.name) ?: SearchTarget.ALL_HUB.name
            val target = try {
                SearchTarget.valueOf(targetName)
            } catch (_: Exception) {
                SearchTarget.ALL_HUB
            }
            return AppSettings(
                autoSearchTarget = target,
                autoOpenBrowser = prefs.getBoolean(KEY_AUTO_OPEN, false),
                autoOpenUrls = prefs.getBoolean(KEY_AUTO_OPEN_URLS, true),
                soundEnabled = prefs.getBoolean(KEY_SOUND, true),
                vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
                continuousScan = prefs.getBoolean(KEY_CONTINUOUS, false)
            )
        }

        fun save(context: Context, settings: AppSettings) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_TARGET, settings.autoSearchTarget.name)
                .putBoolean(KEY_AUTO_OPEN, settings.autoOpenBrowser)
                .putBoolean(KEY_AUTO_OPEN_URLS, settings.autoOpenUrls)
                .putBoolean(KEY_SOUND, settings.soundEnabled)
                .putBoolean(KEY_VIBRATION, settings.vibrationEnabled)
                .putBoolean(KEY_CONTINUOUS, settings.continuousScan)
                .apply()
        }
    }
}
