package ru.mephi.voip.ui.home.screens.settings.screens.common.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsTopBar(
    title: String,
    goBack: () -> Unit
) {
    SmallTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { goBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}