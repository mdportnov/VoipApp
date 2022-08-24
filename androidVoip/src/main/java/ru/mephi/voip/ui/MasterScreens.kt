package ru.mephi.voip.ui

sealed class MasterScreens(
    val route: String
) {
    object HomeScreen : MasterScreens("home_screen")
    object LoginScreen : MasterScreens("login_screen")
}
