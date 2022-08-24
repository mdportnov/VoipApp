@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ru.mephi.voip.R
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsTopBar

@Composable
internal fun AboutAppScreen(
    goBack: () -> Unit
) {
    Scaffold(
        topBar = { SettingsTopBar(title = "О приложении", goBack = goBack) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null)
            }
        }
    }
    BackHandler(true) {
        goBack()
    }
}