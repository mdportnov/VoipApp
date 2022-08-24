@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens

import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SelectionButtonItem
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsTopBar
import ru.mephi.voip.ui.settings.SettingsViewModel

@Composable
internal fun BackgroundWorkScreen(
    goBack: () -> Unit,
    sVM: SettingsViewModel = get()
) {
    val context = LocalContext.current
    val uiState = sVM.uiState.collectAsState()
    val pm = context.getSystemService(POWER_SERVICE) as PowerManager
    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        sVM.enableBackgroundMode(pm.isIgnoringBatteryOptimizations(context.packageName))
    }
    val requestIntent = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}")
    )
    Scaffold(
        topBar = { SettingsTopBar(title = "Работа в фоне", goBack = goBack) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column {
                val selected = uiState.value.isBackgroundModeEnabled
                SelectionButtonItem(
                    isSelected = selected,
                    onClick = { if (!selected) {
                        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                            startForResult.launch(requestIntent)
                        }
                        sVM.enableBackgroundMode(true)
                    } },
                    title = "Разрешить"
                )
                SelectionButtonItem(
                    isSelected = !selected,
                    onClick = { if (selected) sVM.enableBackgroundMode(false) },
                    title = "Запретить"
                )
                Divider(
                    color = TopAppBarDefaults.smallTopAppBarColors().containerColor(
                        scrollFraction = 1.0f
                    ).value,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 10.dp)
                )
                Text(
                    text = "Работа в фоне позволяет приложению получать вызовы даже после его закрытия. Может незначительно увеличить расход заряда аккумулятора. Рекомендуется включить для стабильной работы приложения. Выключено по умолчанию.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    }
    BackHandler(true) {
        goBack()
    }
}
