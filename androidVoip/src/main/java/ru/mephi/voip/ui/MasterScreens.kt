package ru.mephi.voip.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import ru.mephi.voip.R


sealed class MasterScreens(
    var route: String,
    @StringRes var title: Int,
    var icon: ImageVector,
    var selectedIcon: ImageVector,
) {
    object Favourites : MasterScreens(
        route = "favourites",
        title = R.string.bottom_bar_title_favourites,
        icon = Icons.Outlined.StarOutline,
        selectedIcon = Icons.Default.Star
    )

    object History : MasterScreens(
        route = "history",
        title = R.string.bottom_bar_title_history,
        icon = Icons.Outlined.Call,
        selectedIcon = Icons.Default.Call
    )

    object Catalog : MasterScreens(
        route = "catalog",
        title = R.string.bottom_bar_title_catalog,
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Default.MenuBook,
    )

    object Settings : MasterScreens(
        route = "settings",
        title = R.string.bottom_bar_title_settings,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Default.Person,
    )
}

val  masterScreensList = listOf(MasterScreens.Favourites ,MasterScreens.History, MasterScreens.Catalog, MasterScreens.Settings)
