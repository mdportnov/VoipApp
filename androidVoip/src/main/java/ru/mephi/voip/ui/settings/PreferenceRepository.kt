package ru.mephi.voip.ui.settings

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.mephi.shared.appContext
import ru.mephi.voip.R
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

    object PreferenceKeys {
        val IS_SIP_ENABLED =
            booleanPreferencesKey(appContext.getString(R.string.setting_sip_enabled))
        val IS_BACKGROUND_MODE_ENABLED =
            booleanPreferencesKey(appContext.getString(R.string.setting_background_mode_enabled))
        val IS_CALL_SCREEN_ALWAYS_ENABLED =
            booleanPreferencesKey(appContext.getString(R.string.setting_call_screen_enabled))
        val START_SCREEN =
            stringPreferencesKey(appContext.getString(R.string.setting_start_destination))
        val DEVICE_THEME = stringPreferencesKey(appContext.getString(R.string.setting_device_theme))
    }

    val isSipEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[PreferenceKeys.IS_SIP_ENABLED] ?: false }

    val isCallScreenAlwaysEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PreferenceKeys.IS_CALL_SCREEN_ALWAYS_ENABLED] ?: false
        }

    val isBackgroundModeEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PreferenceKeys.IS_BACKGROUND_MODE_ENABLED] ?: false
        }

    val deviceTheme: Flow<DeviceTheme> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEVICE_THEME]?.let { DeviceTheme.valueOf(it) } ?: DeviceTheme.SYSTEM
    }

    val startScreen: Flow<Screens> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.START_SCREEN]?.let {
            when (it) {
                Screens.Dialer.route-> Screens.Dialer
                Screens.Catalog.route -> Screens.Catalog
                Screens.Profile.route -> Screens.Profile
                else -> Screens.Catalog
            }
        } ?: Screens.Catalog
    }

    suspend fun setDeviceTheme(deviceTheme: DeviceTheme) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DEVICE_THEME] = deviceTheme.name
        }
    }

    suspend fun setStartScreen(startScreen: Screens) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.START_SCREEN] = startScreen.route
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