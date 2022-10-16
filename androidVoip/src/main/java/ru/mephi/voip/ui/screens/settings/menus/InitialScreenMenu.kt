package ru.mephi.voip.ui.screens.settings.menus

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.mephi.voip.ui.MasterScreens
import ru.mephi.voip.ui.masterScreensList

@Composable
internal fun InitialScreenMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (MasterScreens) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        for (variant in masterScreensList) {
            DropdownMenuItem(
                text = { Text(text = stringResource(variant.title)) },
                onClick = {
                    onDismissRequest()
                    onClick(variant)
                },
                leadingIcon = { Icon(imageVector = variant.icon, contentDescription = null) }
            )
        }
    }
}