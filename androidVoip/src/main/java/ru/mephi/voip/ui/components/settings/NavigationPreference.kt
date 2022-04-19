package ru.mephi.voip.ui.components.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun NavigationPreference(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    enable: Boolean = true,
    divider: @Composable (() -> Unit)? = {
        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
    },
    onClick: () -> Unit,
) {
    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        subtitle = subtitle,
        enable = enable,
        action = {
            Icon(Icons.Default.NavigateNext, contentDescription = null)
        },
        divider = divider,
        onClick = onClick,
    )
}