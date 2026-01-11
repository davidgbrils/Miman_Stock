package com.example.gudangumkm.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Helper class for managing Light/Dark theme
 */
object ThemeHelper {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_SYSTEM = 2

    /**
     * Apply the saved theme mode
     */
    fun applyTheme(context: Context) {
        val mode = getThemeMode(context)
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Toggle between light and dark mode
     */
    fun toggleTheme(context: Context) {
        val currentMode = getThemeMode(context)
        val newMode = if (currentMode == MODE_DARK) MODE_LIGHT else MODE_DARK
        setThemeMode(context, newMode)
        applyTheme(context)
    }

    /**
     * Check if dark mode is currently active
     */
    fun isDarkMode(context: Context): Boolean {
        return getThemeMode(context) == MODE_DARK
    }

    /**
     * Set theme mode and save to preferences
     */
    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    /**
     * Get current theme mode from preferences
     */
    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME_MODE, MODE_LIGHT)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
