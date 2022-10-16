package ru.mephi.voip.ui.screens.settings.etc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.voip.BuildConfig

@Composable
internal fun AboutApp() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = "Версия ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nРазработано НИЯУ МИФИ, Управление информатизацией",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
        )
    }
}