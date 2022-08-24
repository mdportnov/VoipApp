@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.settings.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.ui.home.homeScreensList
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SelectionButtonItem
import ru.mephi.voip.ui.home.screens.settings.screens.common.items.SettingsTopBar
import ru.mephi.voip.ui.settings.SettingsViewModel

@Composable
internal fun StartSelectionScreen(
    goBack: () -> Unit,
    sVM: SettingsViewModel = get()
) {
    val uiState = sVM.uiState.collectAsState()
    Scaffold(
        topBar = { SettingsTopBar(title = "Стартовый экран", goBack = goBack) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column {
                val selected = uiState.value.startScreen
                for (screen in homeScreensList) {
                    val isSelected = selected == screen
                    SelectionButtonItem(
                        isSelected = isSelected,
                        onClick = { if (!isSelected) sVM.setStartScreen(screen) },
                        title = stringResource(id = screen.title)
                    )
                }
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
                    text = "Стартовый экран - окно, которое будет отображаться по умолчанию при входе в приложение. По умолчанию - каталог.",
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
