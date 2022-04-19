package ru.mephi.voip.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ru.mephi.voip.R

@Composable
fun SettingsTopBar(navController: NavController) {
    val hapticFeedback = LocalHapticFeedback.current

    TopAppBar(backgroundColor = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                modifier = Modifier,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.popBackStack()
                }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
            Text(
                text = "Настройки", style = TextStyle(color = Color.Black, fontSize = 20.sp),
            )
            IconButton(onClick = { }) {
                Image(
                    painter = painterResource(id = R.drawable.logo_mephi),
                    contentDescription = "лого",
                )
            }
        }
    }
}

