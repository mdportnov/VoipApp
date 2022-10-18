package ru.mephi.voip.ui.screens.settings.params.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.sip.PhoneStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.screens.settings.params.SettingsParam
import ru.mephi.voip.vm.SettingsViewModel

@Composable
internal fun BackgroundModeParam(
    phoneStatus: PhoneStatus,
    enableBackgroundMode: (Boolean) -> Unit,
    settingsVM: SettingsViewModel = get()
) {
    val scope = rememberCoroutineScope()
    val isBackgroundModeEnabled by settingsVM.isBackgroundModeEnabled.collectAsState()
    SettingsParam(
        title = stringResource(R.string.param_background_work),
        description = stringResource(R.string.param_background_work_description),
        trailingIcon = {
            Switch(
                checked = isBackgroundModeEnabled,
                onCheckedChange = {
                    scope.launch {
                        if (isBackgroundModeEnabled != it) {
                            enableBackgroundMode(it)
                        }
                    }
                },
                modifier = Modifier.padding(start = 16.dp),
                enabled = when (phoneStatus) {
                    PhoneStatus.RESTARTING,
                    PhoneStatus.SHUTTING_DOWN,
                    PhoneStatus.STARTING_UP -> false
                    else -> true
                }
            )
        },
        isLocked = when (phoneStatus) {
            PhoneStatus.RESTARTING,
            PhoneStatus.SHUTTING_DOWN,
            PhoneStatus.STARTING_UP -> true
            else -> false
        },
        onClick = {
            scope.launch {
                enableBackgroundMode(!isBackgroundModeEnabled)
            }
        }
    )
}
