package ru.mephi.voip.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(
    var title: String,
    var icon: ImageVector,
    var selectedIcon: ImageVector,
    var route: String
) {
    object Caller : Screen(
        "Звонки",
        Icons.Outlined.Call,
        Icons.Filled.Call,
        "caller?caller_number={caller_number}&caller_name={caller_name}"
    )

    object Catalog : Screen("Каталог", Icons.Outlined.Home, Icons.Filled.Home, "catalog")
    object Profile : Screen("Профиль", Icons.Outlined.Person, Icons.Filled.Person, "profile")
    object Settings :
        Screen("Настройки", Icons.Outlined.Settings, Icons.Filled.Settings, "settings")
}