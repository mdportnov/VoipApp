package ru.mephi.voip.ui.detailed

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
    object Info : Screens(
        route = "info_screen",
        title = R.string.info,
        icon = Icons.Outlined.Info,
        selectedIcon = Icons.Default.Info
    )

    object History : Screens(
        route = "history_screen",
        title = R.string.history,
        icon = Icons.Outlined.History,
        selectedIcon = Icons.Default.History
    )
}

val detailedInfoScreensList = Screens::class.sealedSubclasses.mapNotNull { it.objectInstance }
