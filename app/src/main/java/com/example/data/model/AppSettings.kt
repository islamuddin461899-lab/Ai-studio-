package com.example.data.model

enum class DarkModeOption(val labelBn: String, val labelEn: String) {
    SYSTEM("সিস্টেম অনুযায়ী", "System Default"),
    LIGHT("লাইট মোড", "Light Mode"),
    DARK("ডার্ক মোড", "Dark Mode")
}

enum class AccentThemeOption(val label: String, val hexPrimary: Long, val hexSecondary: Long) {
    VIOLET("Violet", 0xFF8B5CF6, 0xFF6366F1),
    CYAN("Cyan", 0xFF06B6D4, 0xFF0284C7),
    EMERALD("Emerald", 0xFF10B981, 0xFF059669),
    AMBER("Amber", 0xFFF59E0B, 0xFFD97706),
    ROSE("Rose", 0xFFF43F5E, 0xFFE11D48)
}

data class AppSettings(
    val darkMode: DarkModeOption = DarkModeOption.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val defaultLanguage: String = "বাংলা + English",
    val autoCopy: Boolean = false,
    val accentTheme: AccentThemeOption = AccentThemeOption.VIOLET
)
