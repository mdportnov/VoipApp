package ru.mephi.voip.ui.settings

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mephi.voip.ui.home.Screens

class PreferenceRepository(val context: Context) {
    private val Context.dataStore by preferencesDataStore(
        name = context.packageName + "_preferences",
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(
                    context, context.packageName + "_preferences"
                )
            )
        })

    val isSipEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[PreferenceKeys.IS_SIP_ENABLED] ?: false }

    val isCallScreenAlwaysEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            (prefs[PreferenceKeys.IS_CALL_SCREEN_ALWAYS_ENABLED] ?: false) && android.provider.Settings.canDrawOverlays(context)
        }

    val isBackgroundModeEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PreferenceKeys.IS_BACKGROUND_MODE_ENABLED] ?: true
        }

    val deviceTheme: Flow<DeviceTheme> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SELECTED_DEVICE_THEME]?.let { DeviceTheme.valueOf(it) } ?: DeviceTheme.SYSTEM
    }

    val startScreen: Flow<Screens> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SELECTED_START_SCREEN]?.let {
            when (it) {
                Screens.Dialer.route-> Screens.Dialer
                Screens.Catalog.route -> Screens.Catalog
                Screens.Settings.route -> Screens.Settings
                else -> Screens.Catalog
            }
        } ?: Screens.Catalog
    }

    suspend fun setDeviceTheme(deviceTheme: DeviceTheme) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SELECTED_DEVICE_THEME] = deviceTheme.name
        }
    }

    suspend fun setStartScreen(startScreen: Screens) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SELECTED_START_SCREEN] = startScreen.route
        }
    }

    suspend fun enableSip(enable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_SIP_ENABLED] = enable
        }
    }

    suspend fun toggleSip() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_SIP_ENABLED] = !(prefs[PreferenceKeys.IS_SIP_ENABLED] ?: false)
        }
    }

    suspend fun enableBackgroundMode(enable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_BACKGROUND_MODE_ENABLED] = enable
        }
    }

    suspend fun enableCallScreenAlways(enable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_CALL_SCREEN_ALWAYS_ENABLED] = enable
        }
    }
}

private object PreferenceKeys {
    val IS_SIP_ENABLED = booleanPreferencesKey("is_sip_enabled")
    val IS_BACKGROUND_MODE_ENABLED = booleanPreferencesKey("is_background_mode_enabled")
    val IS_CALL_SCREEN_ALWAYS_ENABLED = booleanPreferencesKey("is_call_screen_always_enabled")
    val SELECTED_START_SCREEN = stringPreferencesKey("selected_start_screen")
    val SELECTED_DEVICE_THEME = stringPreferencesKey("selected_device_theme")
}
