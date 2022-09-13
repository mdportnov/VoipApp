package ru.mephi.voip.ui.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import ru.mephi.voip.R

sealed class Screens(
    var route: String,
    @StringRes var title: Int,
    var icon: ImageVector,
    var selectedIcon: ImageVector,
) {
    object Dialer : Screens(
        route = "dialer",
        title = R.string.dialer,
        icon = Icons.Outlined.Call,
        selectedIcon = Icons.Default.Call
    )

    // TODO: Даже Outlined.MenuBook выглядит как Default.MenuBook, то есть необходимо создать локальную копию Outlined.MenuBook, с незакрашенной левой стороной
    object Catalog : Screens(
        route = "catalog",
        title = R.string.catalog,
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Default.MenuBook,
    )

    object Settings : Screens(
        route = "settings",
        title = R.string.profile,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Default.Person,
    )
}

val homeScreensList = listOf(Screens.Dialer, Screens.Catalog, Screens.Settings)