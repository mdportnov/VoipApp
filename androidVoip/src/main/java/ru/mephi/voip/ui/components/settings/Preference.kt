package ru.mephi.voip.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun Preference(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = LocalContentColor.current,
    disabledIconTint: Color = iconTint
        .copy(alpha = ContentAlpha.disabled)
        .compositeOver(MaterialTheme.colorScheme.surface),
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledTitleColor: Color = titleColor
        .copy(alpha = ContentAlpha.disabled)
        .compositeOver(MaterialTheme.colorScheme.surface),
    subtitle: String? = null,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledSubtitleColor: Color = subtitleColor
        .copy(alpha = ContentAlpha.disabled)
        .compositeOver(MaterialTheme.colorScheme.surface),
    enable: Boolean = true,
    action: @Composable (() -> Unit)? = null,
    divider: @Composable (() -> Unit)? = {
        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
    },
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(56.dp, 100.dp)
                .padding(horizontal = 4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enable) iconTint else disabledIconTint,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = title,
                    color = if (enable) titleColor else disabledTitleColor,
                )
                subtitle?.let {
                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enable) subtitleColor else disabledSubtitleColor,
                    )
                }
            }
            action?.run { this() }
        }
        divider?.run { this() }
    }
}
