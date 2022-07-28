package ru.mephi.voip.ui.settings

import ru.mephi.voip.ui.home.Screens

data class SettingsUiState(
    val isBackgroundModeEnabled: Boolean = false,
    val isCallScreenAlwaysEnabled: Boolean = false,
    val isSipEnabled: Boolean = false,
    val startScreen: Screens = Screens.Catalog,
    val deviceTheme: DeviceTheme = DeviceTheme.SYSTEM,
    val versionName: String = "",
    val versionCode: Long = 0,
)