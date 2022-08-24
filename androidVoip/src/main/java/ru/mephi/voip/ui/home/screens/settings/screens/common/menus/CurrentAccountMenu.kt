package ru.mephi.voip.ui.home.screens.settings.screens.common.menus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import ru.mephi.voip.ui.home.screens.settings.screens.common.dialogs.HelpDialog

@Composable
fun CurrentAccountMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    openLogin: () -> Unit
) {
    var openHelp by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() }
    ) {
        DropdownMenuItem(
            text = { Text("Помощь") },
            onClick = { onDismiss(); openHelp = true },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Help, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Добавить") },
            onClick = { onDismiss(); openLogin() },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        )
    }
    if (openHelp) {
        HelpDialog { openHelp = false }
    }
}