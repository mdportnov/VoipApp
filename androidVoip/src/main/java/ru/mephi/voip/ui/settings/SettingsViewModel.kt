package ru.mephi.voip.ui.settings

import android.app.Application
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.navigation.Screen

class SettingsViewModel constructor(
    application: Application,
    private val preferenceRepository: PreferenceRepository,
    private val catalogRepository: CatalogRepository,
    private val favoriteDB: FavouritesDB,
) : MainIoExecutor() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            versionName = application.packageManager.getPackageInfo(
                application.packageName,
                0
            ).versionName,
            versionCode = application.packageManager.getPackageInfo(application.packageName, 0)
                .let { PackageInfoCompat.getLongVersionCode(it) },
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        launch {
            preferenceRepository.deviceTheme.collect { deviceTheme ->
                _uiState.update {
                    it.copy(deviceTheme = deviceTheme)
                }
            }
        }

        launch {
            preferenceRepository.startScreen.collect { startScreen ->
                _uiState.update {
                    it.copy(startScreen = startScreen)
                }
            }
        }

        launch {
            preferenceRepository.isBackgroundModeEnabled.collect { enabled ->
                _uiState.update {
                    it.copy(isBackgroundModeEnabled = enabled)
                }
            }
        }

        launch {
            preferenceRepository.isCallScreenAlwaysEnabled.collect { enabled ->
                _uiState.update {
                    it.copy(isCallScreenAlwaysEnabled = enabled)
                }
            }
        }

        launch {
            preferenceRepository.isSipEnabled.collect { enabled ->
                _uiState.update {
                    it.copy(isSipEnabled = enabled)
                }
            }
        }
    }

    fun enableSip(enable: Boolean) {
        launch {
            preferenceRepository.enableSip(enable)
        }
    }

    fun enableBackgroundMode(enable: Boolean) {
        launch {
            preferenceRepository.enableBackgroundMode(enable)
        }
    }

    fun enableCallScreenAlways(enable: Boolean) {
        launch {
            preferenceRepository.enableCallScreenAlways(enable)
        }
    }

    fun setStartScreen(startScreen: Screen) {
        launch {
            preferenceRepository.setStartScreen(startScreen)
        }
    }

    fun setDeviceTheme(deviceTheme: DeviceTheme) {
        launch {
            preferenceRepository.setDeviceTheme(deviceTheme)
        }
    }

    fun deleteAllSearchRecords() {
        catalogRepository.deleteAllSearchRecords()
    }

    fun deleteAllCatalogCache() {
        catalogRepository.deleteAllCatalogCache()
    }

    fun deleteAllFavouritesRecords() {
        favoriteDB.deleteAll()
    }
}
