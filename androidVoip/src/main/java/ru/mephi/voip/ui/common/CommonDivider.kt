package ru.mephi.voip.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommonDivider() {
    Divider(
        color = TopAppBarDefaults.smallTopAppBarColors().containerColor(
            scrollFraction = 1.0f
        ).value,
        thickness = 0.8.dp,
        modifier = Modifier.fillMaxWidth()
    )
}