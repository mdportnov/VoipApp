package ru.mephi.voip.ui.home.screens.settings.screens.common.items

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import ru.mephi.voip.ui.home.screens.settings.screens.common.menus.CurrentAccountMenu

@Composable
fun AccountMore(
    openLogin: () -> Unit
) {
    var openMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { openMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
        CurrentAccountMenu(
            expanded = openMenu,
            onDismiss = { openMenu = false },
            openLogin = openLogin
        )
    }
}