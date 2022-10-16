package ru.mephi.voip.ui.screens.settings.params

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.R
import ru.mephi.voip.ui.MasterScreens
import ru.mephi.voip.ui.screens.settings.menus.InitialScreenMenu
import ru.mephi.voip.vm.SettingsViewModel


@Composable
internal fun InitialScreenParam(
    setStartScreen: (MasterScreens) -> Unit,
    settingsVM: SettingsViewModel = get()
) {
    val startScreen by settingsVM.startScreen.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    SettingsParam(
        title = stringResource(R.string.param_initial_screen),
        description = stringResource(R.string.param_initial_screen_description, stringResource(startScreen.title)),
        trailingIcon = {
            Box(modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = when (expanded) {
                        true -> Icons.Default.ExpandLess
                        false -> Icons.Default.ExpandMore
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
                InitialScreenMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    onClick = { setStartScreen(it) }
                )
            }
        },
        onClick = { expanded = !expanded }
    )
}