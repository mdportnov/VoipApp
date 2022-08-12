package ru.mephi.voip.ui.login

sealed class Screens(val route: String) {
    object HelloScreen : Screens("hello_screen")
    object CredentialsInputScreen : Screens("credentials_input_screen")
    object AskPermissionsScreen : Screens("ask_permissions_screen")
    object AskOverlayScreen : Screens("ask_overlay_screen")
}
