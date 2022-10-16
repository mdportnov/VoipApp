package ru.mephi.voip.data

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.voip.R
import ru.mephi.voip.ui.MasterScreens
import ru.mephi.voip.utils.launchMailClientIntent

class SettingsRepository(
    private val context: Context,
    private val catalogVM: CatalogViewModel,
    private val favoriteDB: FavouritesDB
) {
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
            prefs[PreferenceKeys.IS_CALL_SCREEN_ALWAYS_ENABLED] ?: false
        }

    val isBackgroundModeEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PreferenceKeys.IS_BACKGROUND_MODE_ENABLED] ?: false
        }

    val deviceTheme: Flow<DeviceTheme> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SELECTED_DEVICE_THEME]?.let { DeviceTheme.valueOf(it) } ?: DeviceTheme.SYSTEM
    }

    val startScreen: Flow<MasterScreens> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SELECTED_START_SCREEN]?.let {
            when (it) {
                MasterScreens.History.route-> MasterScreens.History
                MasterScreens.Catalog.route -> MasterScreens.Catalog
                MasterScreens.Settings.route -> MasterScreens.Settings
                MasterScreens.Favourites.route -> MasterScreens.Favourites
                else -> MasterScreens.Catalog
            }
        } ?: MasterScreens.Catalog
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun setDeviceTheme(deviceTheme: DeviceTheme) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferenceKeys.SELECTED_DEVICE_THEME] = deviceTheme.name
            }
        }
    }

    fun setStartScreen(startScreen: MasterScreens) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferenceKeys.SELECTED_START_SCREEN] = startScreen.route
            }
        }
    }

    fun enableSip(enable: Boolean) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferenceKeys.IS_SIP_ENABLED] = enable
            }
        }
    }

    fun enableBackgroundMode(enable: Boolean) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferenceKeys.IS_BACKGROUND_MODE_ENABLED] = enable
            }
        }
    }

    fun enableCallScreenAlways(enable: Boolean) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferenceKeys.IS_CALL_SCREEN_ALWAYS_ENABLED] = enable
            }
        }
    }

    fun clearSearchHistory() = catalogVM::clearSearchHistory

    fun clearCatalogCache() { }

    fun clearFavourites() { }

    fun sendFeedback() = context.launchMailClientIntent(context.getString(R.string.support_email))

}

enum class DeviceTheme(val description: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
}

private object PreferenceKeys {
    val IS_SIP_ENABLED = booleanPreferencesKey("is_sip_enabled")
    val IS_BACKGROUND_MODE_ENABLED = booleanPreferencesKey("is_background_mode_enabled")
    val IS_CALL_SCREEN_ALWAYS_ENABLED = booleanPreferencesKey("is_call_screen_always_enabled")
    val SELECTED_START_SCREEN = stringPreferencesKey("selected_start_screen")
    val SELECTED_DEVICE_THEME = stringPreferencesKey("selected_device_theme")
}
