package ru.mephi.voip.ui.home.screens.settings.screens.common.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 4.dp, end = 14.dp)
                .size(30.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight(450),
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.NavigateNext,
                tint = MaterialTheme.colorScheme.outline,
                contentDescription = null
            )
        }
    }
}