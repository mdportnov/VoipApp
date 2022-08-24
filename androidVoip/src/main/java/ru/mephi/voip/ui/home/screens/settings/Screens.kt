package ru.mephi.voip.ui.home.screens.settings


sealed class Screens(
    val route: String
) {
    object SettingsHomeScreen: Screens("settings_home_screen")
    object BackgroundWorkScreen: Screens("background_work_screen")
    object StartSelectionScreen: Screens("start_selection_screen")
    object ClearDataScreen: Screens("clear_data_screen")
    object EnableOverlayScreen: Screens("enable_overlay_screen")
    object AboutAppScreen: Screens("about_app_screen")
}