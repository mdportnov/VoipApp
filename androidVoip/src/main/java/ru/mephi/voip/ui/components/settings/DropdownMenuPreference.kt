package ru.mephi.voip.ui.components.settings

import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun DropdownMenuPreference(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    enable: Boolean = true,
    options: List<String>,
    divider: @Composable (() -> Unit)? = {
        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
    },
    onOptionSelected: (index: Int) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        subtitle = subtitle,
        enable = enable,
        action = {
            if (enable) {
                DropdownMenu(
                    expanded = expanded,
                    offset = DpOffset(x = 48.dp, y = 0.dp),
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                onOptionSelected(index)
                                expanded = false
                            },
                        )
                    }
                }
            }
        },
        divider = divider,
        onClick = {
            if (enable) {
                expanded = true
            }
        },
    )
}