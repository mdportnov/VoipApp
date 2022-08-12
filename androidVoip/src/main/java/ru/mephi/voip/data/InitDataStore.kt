package ru.mephi.voip.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class InitDataStore(context: Context) {

    private val name = "isInitRequired"

    private val Context.selectionConfig: DataStore<Preferences> by preferencesDataStore("${context.packageName}_$name")

    private val isInitRequired = intPreferencesKey(name)

    private val dataStore = context.selectionConfig

    val selected: Flow<Int> = dataStore.data.map { it[isInitRequired] ?: InitRequirement.INIT_REQUIRED.code }

    suspend fun setInitRequirement(init: InitRequirement) {
        dataStore.edit { it[isInitRequired] = init.code }
    }

}

enum class InitRequirement(val code: Int) {
    NOT_READY(0x0), SKIP_INIT(0x1), INIT_REQUIRED(0x2);

    companion object {
        fun fromInt(code: Int) = values().firstOrNull { it.code == code }
    }
}