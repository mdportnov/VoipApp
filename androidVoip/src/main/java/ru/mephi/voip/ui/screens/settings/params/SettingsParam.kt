package ru.mephi.voip.ui.screens.settings.params

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsParam(
    title: String,
    description: String = "",
    isLocked: Boolean = false,
    onClick: (() -> Unit),
    trailingIcon: @Composable (() -> Unit)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isLocked
            ) { onClick() }
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
                )
            }
        }
        trailingIcon()
    }
}