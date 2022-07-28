package ru.mephi.voip.ui

sealed class MasterScreens(
    val route: String
) {
    object HomeScreen : MasterScreens("home_screen")
    object DetailedInfoScreen : MasterScreens("detailed_info_screen")
    object AccountManagerScreen : MasterScreens("account_manager_screen")
}
