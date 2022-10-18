package ru.mephi.voip.ui.screens.settings.params.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.mephi.voip.R
import ru.mephi.voip.ui.screens.settings.dialogs.ConfirmationDialog
import ru.mephi.voip.ui.screens.settings.params.SettingsParam

@Composable
internal fun ClearSearchHistoryParam(
    clearSearchHistory: () -> Unit
) {
    var dialog by remember { mutableStateOf(false) }
    SettingsParam(
        title = stringResource(R.string.param_clear_search_history),
        description = stringResource(R.string.param_clear_search_history_description),
        trailingIcon = {
            Box(modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        },
        onClick = { }
    )
    if (dialog) {
        ConfirmationDialog(
            onDismissRequest = { dialog = false },
            onConfirm = {
                dialog = false
                clearSearchHistory()
            },
            text = stringResource(R.string.param_clear_search_history_confirmation)
        )
    }
}