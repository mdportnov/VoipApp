package ru.mephi.voip.ui.settings

import ru.mephi.voip.ui.navigation.Screen

data class SettingsUiState(
    val isBackgroundModeEnabled: Boolean = false,
    val isCallScreenAlwaysEnabled: Boolean = false,
    val isSipEnabled: Boolean = false,
    val startScreen: Screen = Screen.Catalog,
    val deviceTheme: DeviceTheme = DeviceTheme.SYSTEM,
    val versionName: String = "",
    val versionCode: Long = 0,
)