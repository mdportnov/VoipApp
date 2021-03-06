package ru.mephi.voip.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ru.mephi.voip.R
import ru.mephi.voip.ui.components.settings.*
import ru.mephi.voip.ui.navigation.Screen
import ru.mephi.voip.utils.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
import ru.mephi.voip.utils.PACKAGE_NAME
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
internal fun Settings(
    uiState: SettingsUiState,
    scaffoldState: ScaffoldState,
    onDeviceThemeChange: (DeviceTheme) -> Unit,
    onStartScreenChange: (Screen) -> Unit,
    onSipEnableChange: (Boolean) -> Unit,
    onBackgroundModeEnableChange: (Boolean) -> Unit,
    onCallScreenAlwaysEnableChange: (Boolean) -> Unit,
    deleteAllCatalogCache: () -> Unit,
    deleteAllSearchRecords: () -> Unit,
    deleteAllFavouritesRecords: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    onCallScreenAlwaysEnableChange(Settings.canDrawOverlays(context))

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        SectionTitle(stringResource(id = R.string.main_settings))

        SwitchPreference(
            icon = Icons.Default.Sip,
            title = stringResource(id = if (uiState.isSipEnabled) R.string.disable_sip else R.string.enable_sip),
            checked = uiState.isSipEnabled,
            onCheckedChange = {
                onSipEnableChange(it)
            },
        )

        SwitchPreference(
            icon = Icons.Default.Cloud,
            title = stringResource(id = R.string.enable_background_mode),
            checked = uiState.isBackgroundModeEnabled,
            onCheckedChange = {
                onBackgroundModeEnableChange(it)
            },
        )

        SwitchPreference(
            icon = Icons.Default.Fullscreen,
            title = stringResource(id = R.string.enable_incoming_activity),
            subtitle = stringResource(R.string.enable_incoming_activity_subtitle),
            checked = uiState.isCallScreenAlwaysEnabled,
            onCheckedChange = {
//                if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$PACKAGE_NAME")
                )
                (context as Activity).startActivityForResult(
                    intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
                )
//                }
                onCallScreenAlwaysEnableChange(Settings.canDrawOverlays(context))
            },
        )

        val themeOptions = DeviceTheme.values().map { it.description }

        DropdownMenuPreference(
            icon = when (uiState.deviceTheme) {
                DeviceTheme.SYSTEM -> Icons.Default.Android
                DeviceTheme.LIGHT -> Icons.Default.LightMode
                DeviceTheme.DARK -> Icons.Default.DarkMode
            },
            title = stringResource(id = R.string.application_theme),
            subtitle = uiState.deviceTheme.description,
            options = themeOptions,
            enable = false
        ) { index ->
            onDeviceThemeChange(DeviceTheme.values()[index])
        }

        val startScreenOptions = listOf(Screen.Caller, Screen.Catalog, Screen.Profile)

        DropdownMenuPreference(
            icon = when (uiState.startScreen) {
                Screen.Caller -> Icons.Default.Call
                Screen.Catalog -> Icons.Default.Home
                Screen.Profile, Screen.Settings -> Icons.Default.Person
            },
            title = stringResource(id = R.string.start_screen),
            subtitle = uiState.startScreen.title,
            options = startScreenOptions.map { it.title },
        ) { index ->
            onStartScreenChange(startScreenOptions[index])
        }

        SectionTitle(stringResource(id = R.string.data_settings))

        Preference(
            icon = Icons.Default.DeleteSweep, title = stringResource(R.string.delete_search_history)
        ) {
            deleteAllSearchRecords()
            scope.launch {
                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                scaffoldState.snackbarHostState.showSnackbar("?????????????? ???????????? ??????????????")
            }
        }

        Preference(
            icon = Icons.Default.Delete, title = stringResource(R.string.delete_catalog_cache)
        ) {
            deleteAllCatalogCache()
            scope.launch {
                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                scaffoldState.snackbarHostState.showSnackbar("?????? ???????????????? ????????????")
            }
        }

        Preference(
            icon = Icons.Default.DeleteForever, title = stringResource(R.string.delete_favourites)
        ) {
            deleteAllFavouritesRecords()
            scope.launch {
                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                scaffoldState.snackbarHostState.showSnackbar("?????????????????? ???????????????? ??????????????")
            }
        }

        SectionTitle(stringResource(id = R.string.about))

        val email = stringResource(R.string.support_email)

        NavigationPreference(
            icon = Icons.Default.Send, title = stringResource(
                id = R.string.send_feedback
            ), subtitle = stringResource(R.string.send_feedback_subtitle)
        ) {
            context.launchMailClientIntent(email)
        }

        Preference(
            icon = Icons.Default.AutoFixHigh,
            title = stringResource(R.string.developed),
            subtitle = stringResource(R.string.developer_name)
        )

        VersionItem(
            versionName = uiState.versionName,
            versionCode = uiState.versionCode,
        )
    }
}