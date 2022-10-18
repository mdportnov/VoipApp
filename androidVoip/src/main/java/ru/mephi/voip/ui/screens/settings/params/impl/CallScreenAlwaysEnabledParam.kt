package ru.mephi.voip.ui.screens.settings.params.impl

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.R
import ru.mephi.voip.ui.screens.settings.params.SettingsParam
import ru.mephi.voip.vm.SettingsViewModel

@Composable
internal fun CallScreenAlwaysEnabledParam(
    enableCallScreenAlways: (Boolean) -> Unit,
    settingsVM: SettingsViewModel = get()
) {
    val context = LocalContext.current
    val isCallScreenAlwaysEnabled by settingsVM.isCallScreenAlwaysEnabled.collectAsState()
    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(context)) {
            enableCallScreenAlways(true)
        }
    }
    val onClick = { it: Boolean ->
        if (!Settings.canDrawOverlays(context) && it) {
            startForResult.launch(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ))
        } else {
            enableCallScreenAlways(it)
        }
    }
    SettingsParam(
        title = stringResource(R.string.param_call_screen_always_enabled),
        description = stringResource(R.string.param_call_screen_always_enabled_description),
        trailingIcon = {
            Switch(
                checked = isCallScreenAlwaysEnabled,
                onCheckedChange = {
                    if (isCallScreenAlwaysEnabled != it) {
                        onClick(it)
                    }
                },
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        onClick = {
            onClick(!isCallScreenAlwaysEnabled)
        }
    )
}
