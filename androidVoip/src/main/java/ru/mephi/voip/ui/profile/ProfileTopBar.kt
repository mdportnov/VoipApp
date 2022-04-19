package ru.mephi.voip.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import ru.mephi.voip.R

@Composable
fun ProfileTopBar(openSettings: () -> Unit) {
    val hapticFeedback = LocalHapticFeedback.current

    TopAppBar(backgroundColor = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { }) {
                Image(
                    painter = painterResource(id = R.drawable.logo_mephi),
                    contentDescription = null,
                )
            }
            Text(
                text = stringResource(id = R.string.toolbar_title_profile),
                style = TextStyle(color = Color.Black, fontSize = 20.sp),
            )
            IconButton(modifier = Modifier, onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                openSettings()
            }) {
                Icon(Icons.Filled.Settings, contentDescription = null)
            }
        }
    }
}

