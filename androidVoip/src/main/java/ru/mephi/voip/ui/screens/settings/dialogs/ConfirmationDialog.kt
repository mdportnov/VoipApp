package ru.mephi.voip.ui.screens.settings.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun ConfirmationDialog(
    title: String,
    text: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Да")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Нет")
            }
        }
    )
}
