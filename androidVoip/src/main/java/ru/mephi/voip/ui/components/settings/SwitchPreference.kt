package ru.mephi.voip.ui.components.settings

import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ru.mephi.voip.utils.ColorAccent

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    enable: Boolean = true,
    checked: Boolean,
    divider: @Composable (() -> Unit)? = {
        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
    },
    onCheckedChange: (Boolean) -> Unit,
) {
    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        subtitle = subtitle,
        enable = enable,
        action = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enable,
                colors = SwitchDefaults.colors(checkedThumbColor = ColorAccent)
            )
        },
        divider = divider,
    )
}