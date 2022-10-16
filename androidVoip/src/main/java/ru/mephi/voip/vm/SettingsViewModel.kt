package ru.mephi.voip.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.mephi.voip.data.DeviceTheme
import ru.mephi.voip.data.SettingsRepository
import ru.mephi.voip.ui.MasterScreens

class SettingsViewModel(
    private val settingsRepo: SettingsRepository,
): ViewModel() {

    private val _isSipEnabled = MutableStateFlow(false)
    val isSipEnabled: StateFlow<Boolean> = _isSipEnabled
    private val _isCallScreenAlwaysEnabled = MutableStateFlow(false)
    val isCallScreenAlwaysEnabled: StateFlow<Boolean> = _isCallScreenAlwaysEnabled
    private val _isBackgroundModeEnabled = MutableStateFlow(false)
    val isBackgroundModeEnabled: StateFlow<Boolean> = MutableStateFlow(false)
    private val _deviceTheme = MutableStateFlow(DeviceTheme.SYSTEM)
    val deviceTheme: StateFlow<DeviceTheme> = _deviceTheme
    private val _startScreen = MutableStateFlow<MasterScreens>(MasterScreens.Catalog)
    val startScreen: StateFlow<MasterScreens> = _startScreen

    private val _isAccManagerOpen = MutableStateFlow(false)
    val isAccManagerOpen: StateFlow<Boolean> = _isAccManagerOpen
    private val _isLoginOpen = MutableStateFlow(false)
    val isLoginOpen: StateFlow<Boolean> = _isLoginOpen

    lateinit var initialStartScreen: MasterScreens

    init {
        viewModelScope.launch {
            settingsRepo.isSipEnabled.collect {
                _isSipEnabled.value = it
            }
        }
        viewModelScope.launch {
            settingsRepo.isCallScreenAlwaysEnabled.collect {
                _isCallScreenAlwaysEnabled.value = it
            }
        }
        viewModelScope.launch {
            settingsRepo.isBackgroundModeEnabled.collect {
                _isBackgroundModeEnabled.value = it
            }
        }
        viewModelScope.launch {
            settingsRepo.deviceTheme.collect {
                _deviceTheme.value = it
            }
        }
        viewModelScope.launch {
            settingsRepo.startScreen.collect {
                if (!isScreenReady()) {
                    initialStartScreen = it
                }
                _startScreen.value = it
            }
        }
    }

    fun openAccManagerDialog() {
        viewModelScope.launch {
            _isLoginOpen.value = false
            delay(100)
            _isAccManagerOpen.value = true
        }
    }

    fun closeAccManagerDialog() {
        _isAccManagerOpen.value = false
    }

    fun openLoginDialog() {
        viewModelScope.launch {
            _isAccManagerOpen.value = false
            delay(100)
            _isLoginOpen.value = true
        }
    }

    fun closeLoginDialog() {
        _isLoginOpen.value = false
    }

    fun isScreenReady() = ::initialStartScreen.isInitialized
}
