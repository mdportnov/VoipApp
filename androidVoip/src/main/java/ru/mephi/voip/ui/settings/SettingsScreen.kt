package ru.mephi.voip.ui.settings

import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.inject
import ru.mephi.voip.utils.rememberStateWithLifecycle

@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel by inject()
    val uiState by rememberStateWithLifecycle(viewModel.uiState)

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            SettingsTopBar(navController = navController)
        },
        content = {
            Settings(
                uiState = uiState,
                scaffoldState = scaffoldState,
                onDeviceThemeChange = viewModel::setDeviceTheme,
                onStartScreenChange = viewModel::setStartScreen,
                onSipEnableChange = viewModel::enableSip,
                onBackgroundModeEnableChange = viewModel::enableBackgroundMode,
                onCallScreenAlwaysEnableChange = viewModel::enableCallScreenAlways,
                deleteAllCatalogCache = viewModel::deleteAllCatalogCache,
                deleteAllSearchRecords = viewModel::deleteAllSearchRecords,
                deleteAllFavouritesRecords = viewModel::deleteAllFavouritesRecords
            )
        })
}