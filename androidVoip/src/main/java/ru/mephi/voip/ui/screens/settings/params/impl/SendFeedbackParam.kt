package ru.mephi.voip.ui.screens.settings.params

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.mephi.voip.R

@Composable
internal fun SendFeedbackParam(
    sendFeedback: () -> Unit
) {
    SettingsParam(
        title = stringResource(R.string.param_send_feedback),
        description = stringResource(R.string.param_send_feedback_description),
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
        onClick = sendFeedback
    )
}