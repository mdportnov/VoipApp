package ru.mephi.voip.ui.home.screens.catalog.search

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.ColorGray

@Composable
fun IconSwitch(
    firstIcon: ImageVector, secondIcon: ImageVector,
    state: Boolean, onCheckedChange: () -> Unit
) {
    Row {
        IconButton(onClick = { onCheckedChange() }) {
            Icon(
                firstIcon,
                contentDescription = "",
                tint = if (state) ColorAccent else ColorGray
            )
        }
        IconButton(onClick = { onCheckedChange() }) {
            Icon(
                secondIcon, contentDescription = "",
                tint = if (!state) ColorAccent else ColorGray
            )
        }
    }
}