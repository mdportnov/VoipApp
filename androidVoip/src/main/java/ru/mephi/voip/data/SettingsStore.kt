package ru.mephi.voip.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.mephi.shared.appContext
import ru.mephi.voip.R
import java.io.IOException

interface SettingsStore {
    fun isSipEnabled(): Boolean
    fun toggleSipStatus()
    fun isBackgroundWorkEnabled(): Boolean
    fun isCallScreenAlways(): Boolean
}

class SharedPrefsSettings : SettingsStore {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)

    override fun isSipEnabled(): Boolean =
        prefs.getBoolean(appContext.getString(R.string.sp_sip_enabled), false)

    override fun toggleSipStatus() {
        val key = appContext.getString(R.string.sp_sip_enabled)
        val newValue = !prefs.getBoolean(key, false)
        prefs.edit().putBoolean(key, newValue).apply()
    }

    override fun isBackgroundWorkEnabled(): Boolean =
        prefs.getBoolean(appContext.getString(R.string.sp_background_enabled), false)

    override fun isCallScreenAlways(): Boolean =
        prefs.getBoolean(appContext.getString(R.string.call_screen_always_settings), false)
}

suspend inline fun <reified T> DataStore<Preferences>.getFromLocalStorage(
    PreferencesKey: Preferences.Key<T>, crossinline func: T.() -> Unit
) {
    data.catch {
        if (it is IOException) {
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map {
        it[PreferencesKey]
    }.collect {
        it?.let {
            func.invoke(it as T)
        }
    }
}

class DataStoreSettings(val context: Context) : SettingsStore {
    private object PreferencesKeys {
        val isSipEnabled =
            stringPreferencesKey(appContext.getString(R.string.sp_sip_enabled))
        val isBackgroundWorkEnabled =
            booleanPreferencesKey(appContext.getString(R.string.sp_background_enabled))
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val Context.dataStore by preferencesDataStore(
        name = context.packageName + "_preferences", produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(
                    context, context.packageName + "_preferences"
                )
            )
        }
    )

    override fun isSipEnabled(): Boolean {
        return runBlocking {
            context.dataStore.data.map {
                it[PreferencesKeys.isSipEnabled] ?: false
            }.first() as Boolean
        }
    }

    override fun toggleSipStatus() {
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.isSipEnabled] = (!isSipEnabled()).toString()
            }
        }
    }

    override fun isBackgroundWorkEnabled(): Boolean {
        return true
    }

    override fun isCallScreenAlways(): Boolean {
        return true
    }

}