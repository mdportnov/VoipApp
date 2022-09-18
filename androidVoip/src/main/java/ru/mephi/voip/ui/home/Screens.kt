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
    object Favourites : Screens(
        route = "favourites",
        title = R.string.bottom_bar_title_favourites,
        icon = Icons.Outlined.Call,
        selectedIcon = Icons.Default.Call
    )

    object History : Screens(
        route = "history",
        title = R.string.bottom_bar_title_history,
        icon = Icons.Outlined.Call,
        selectedIcon = Icons.Default.Call
    )

    // TODO: Даже Outlined.MenuBook выглядит как Default.MenuBook, то есть необходимо создать локальную копию Outlined.MenuBook, с незакрашенной левой стороной
    object Catalog : Screens(
        route = "catalog",
        title = R.string.bottom_bar_title_catalog,
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Default.MenuBook,
    )

    object Settings : Screens(
        route = "settings",
        title = R.string.bottom_bar_title_settings,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Default.Person,
    )
}

val homeScreensList = listOf(Screens.History, Screens.Catalog, Screens.Settings)