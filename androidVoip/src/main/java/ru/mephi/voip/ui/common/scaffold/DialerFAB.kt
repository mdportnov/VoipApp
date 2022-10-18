package ru.mephi.voip.ui.common.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun DialerFAB(
    openDialPad: () -> Unit
) {
    FloatingActionButton(onClick = openDialPad) {
        Icon(imageVector = Icons.Default.Dialpad, contentDescription = null)
    }
}