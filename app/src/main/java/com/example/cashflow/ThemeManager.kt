package com.example.cashflow

import android.content.Context
import android.graphics.Color

data class AppTheme(
    val key: String,
    val name: String,
    val bgDark: Int,         // Dark background (replaces #071108)
    val cardDark: Int,       // Dark card/container background (replaces #172419)
    val buttonShade: Int,    // Medium button shade (replaces #365A3A)
    val lightShade: Int,     // Lighter accent/text/selected shade (replaces #A5D6A7 / #A5E6A8)
    val textDark: Int,       // Text on lightShade button (replaces #16301A)
    val textLight: Int,      // Text on dark/button backgrounds (replaces #E8F5E9)
    val radioColor: Int      // Representative dot color for radio button
)

object ThemeManager {
    private const val PREFS_NAME = "cashflow_theme_prefs"
    private const val KEY_THEME = "selected_theme_key"

    val THEMES = listOf(
        AppTheme(
            key = "VIOLET",
            name = "Violet",
            bgDark = Color.parseColor("#0C0614"),
            cardDark = Color.parseColor("#1D122B"),
            buttonShade = Color.parseColor("#4A2B6E"),
            lightShade = Color.parseColor("#CE93D8"),
            textDark = Color.parseColor("#1F0038"),
            textLight = Color.parseColor("#F5EDFF"),
            radioColor = Color.parseColor("#CE93D8")
        ),
        AppTheme(
            key = "INDIGO",
            name = "Indigo",
            bgDark = Color.parseColor("#060717"),
            cardDark = Color.parseColor("#121530"),
            buttonShade = Color.parseColor("#243375"),
            lightShade = Color.parseColor("#9EA8FF"),
            textDark = Color.parseColor("#0C103B"),
            textLight = Color.parseColor("#EFF1FF"),
            radioColor = Color.parseColor("#9EA8FF")
        ),
        AppTheme(
            key = "BLUE",
            name = "Blue",
            bgDark = Color.parseColor("#040D17"),
            cardDark = Color.parseColor("#0E2033"),
            buttonShade = Color.parseColor("#1B4F78"),
            lightShade = Color.parseColor("#7BC0FF"),
            textDark = Color.parseColor("#03213B"),
            textLight = Color.parseColor("#E6F3FF"),
            radioColor = Color.parseColor("#7BC0FF")
        ),
        AppTheme(
            key = "GREEN",
            name = "Green (Default)",
            bgDark = Color.parseColor("#071108"),
            cardDark = Color.parseColor("#172419"),
            buttonShade = Color.parseColor("#365A3A"),
            lightShade = Color.parseColor("#A5D6A7"),
            textDark = Color.parseColor("#16301A"),
            textLight = Color.parseColor("#E8F5E9"),
            radioColor = Color.parseColor("#A5D6A7")
        ),
        AppTheme(
            key = "YELLOW",
            name = "Yellow",
            bgDark = Color.parseColor("#141203"),
            cardDark = Color.parseColor("#262309"),
            buttonShade = Color.parseColor("#5E5014"),
            lightShade = Color.parseColor("#FFD54F"),
            textDark = Color.parseColor("#292100"),
            textLight = Color.parseColor("#FFFDE8"),
            radioColor = Color.parseColor("#FFD54F")
        ),
        AppTheme(
            key = "ORANGE",
            name = "Orange",
            bgDark = Color.parseColor("#140903"),
            cardDark = Color.parseColor("#291609"),
            buttonShade = Color.parseColor("#6E3813"),
            lightShade = Color.parseColor("#FFB049"),
            textDark = Color.parseColor("#2E1200"),
            textLight = Color.parseColor("#FFF3E6"),
            radioColor = Color.parseColor("#FFB049")
        ),
        AppTheme(
            key = "RED",
            name = "Red",
            bgDark = Color.parseColor("#140505"),
            cardDark = Color.parseColor("#290C0C"),
            buttonShade = Color.parseColor("#6E2222"),
            lightShade = Color.parseColor("#FF8A80"),
            textDark = Color.parseColor("#2B0000"),
            textLight = Color.parseColor("#FFEBEE"),
            radioColor = Color.parseColor("#FF8A80")
        )
    )

    private val listeners = mutableListOf<(AppTheme) -> Unit>()

    fun getTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_THEME, "GREEN") ?: "GREEN"
        return THEMES.find { it.key == key } ?: THEMES[3] // Default Green
    }

    fun setSelectedTheme(context: Context, themeKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, themeKey).apply()
        val theme = getTheme(context)
        notifyThemeChanged(theme)
    }

    fun addListener(listener: (AppTheme) -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: (AppTheme) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyThemeChanged(theme: AppTheme) {
        // Iterate over a copy to avoid ConcurrentModificationException if a listener modifies the list
        val currentListeners = ArrayList(listeners)
        for (listener in currentListeners) {
            listener.invoke(theme)
        }
    }
}
