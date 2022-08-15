package ru.mephi.voip.ui

sealed class MasterScreens(
    val route: String
) {
    object HomeScreen : MasterScreens("home_screen")
    object SettingsScreen : MasterScreens("settings_screen")
    object LoginScreen : MasterScreens("login_screen")
}
