package ru.mephi.voip.ui.navigation

import androidx.annotation.StringRes
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
import ru.mephi.voip.R

sealed class Screen(
    var route: String,
    @StringRes var title: Int,
    var icon: ImageVector,
    var selectedIcon: ImageVector,
) {
    object Caller : Screen(
        route = "caller?caller_number={caller_number}&caller_name={caller_name}",
        title = R.string.caller,
        icon = Icons.Outlined.Call,
        selectedIcon = Icons.Default.Call
    )

    object Catalog : Screen(
        route = "catalog",
        title = R.string.catalog,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Default.Home,
    )

    object Profile : Screen(
        route = "profile",
        title = R.string.profile,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Default.Person,
    )

    object Settings : Screen(
        route = "settings",
        title = R.string.settings,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Default.Settings,
    )
}