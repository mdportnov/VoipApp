package ru.mephi.voip.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.model.Account
import ru.mephi.voip.data.DeviceTheme
import ru.mephi.voip.data.LoginStatus
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.data.SettingsRepository
import ru.mephi.voip.ui.MasterScreens

class SettingsViewModel(): ViewModel(), KoinComponent {
    private val settingsRepo by inject<SettingsRepository>()
    private val phoneManager by inject<PhoneManager>()

    private val _isSipEnabled = MutableStateFlow(false)
    val isSipEnabled = _isSipEnabled.asStateFlow()
    private val _isCallScreenAlwaysEnabled = MutableStateFlow(false)
    val isCallScreenAlwaysEnabled = _isCallScreenAlwaysEnabled.asStateFlow()
    private val _isBackgroundModeEnabled = MutableStateFlow(false)
    val isBackgroundModeEnabled = _isBackgroundModeEnabled.asStateFlow()
    private val _deviceTheme = MutableStateFlow(DeviceTheme.SYSTEM)
    val deviceTheme = _deviceTheme.asStateFlow()
    private val _startScreen = MutableStateFlow<MasterScreens>(MasterScreens.Catalog)
    val startScreen = _startScreen.asStateFlow()

    private val _isAccManagerOpen = MutableStateFlow(false)
    val isAccManagerOpen = _isAccManagerOpen.asStateFlow()

    private val _isLoginOpen = MutableStateFlow(false)
    val isLoginOpen = _isLoginOpen.asStateFlow()
    private val _isLoginUiLocked = MutableStateFlow(false)
    val isLoginUiLocked = _isLoginUiLocked.asStateFlow()
    private val _loginErrorMsg = MutableStateFlow("")
    val loginErrorMsg = _loginErrorMsg.asStateFlow()

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

    private var loginJob: Job? = null

    fun startNewAccountLogin(
        login: String = "",
        password: String = ""
    ) {
        if (_isLoginUiLocked.value) return
        loginJob?.cancel()
        _isLoginUiLocked.value = true
        if (login.isEmpty() || password.isEmpty()) {
            _loginErrorMsg.value = "Номер или пароль не может быть пустым!"
            _isLoginUiLocked.value = false
            return
        }
        if (login.toIntOrNull() == null) {
            _loginErrorMsg.value = "В номере допустимы только цифры!"
            _isLoginUiLocked.value = false
            return
        }
        if (phoneManager.accountsList.value.map { it.login }.contains(login)) {
            _loginErrorMsg.value = "В этот аккаунт уже выполнен вход!"
            _isLoginUiLocked.value = false
            return
        }
        phoneManager.startNewAccountLogin(Account(login = login, password = password))
        loginJob = CoroutineScope(Dispatchers.IO).launch {
            phoneManager.loginStatus.collect { status ->
                when (status) {
                    LoginStatus.LOGIN_FAILURE -> {
                        _loginErrorMsg.value = "Не удалось войти в акакунт"
                        _isLoginUiLocked.value = false
                        loginJob?.cancel()
                    }
                    LoginStatus.LOGIN_IN_PROGRESS -> {
                        clearErrorMsg()
                        _isLoginUiLocked.value = true
                    }
                    LoginStatus.LOGIN_SUCCESSFUL -> {
                        clearErrorMsg()
                        _isLoginUiLocked.value = false
                        closeLoginDialog()
                        loginJob?.cancel()
                    }
                    LoginStatus.DATA_FETCH_FAILURE -> {
                        _loginErrorMsg.value = "Не удалось получить данные о пользователе"
                        _isLoginUiLocked.value = false
                        loginJob?.cancel()
                    }
                }
            }
        }
    }

    fun clearErrorMsg() {
        _loginErrorMsg.value = ""
    }

    fun openAccManagerDialog() {
        viewModelScope.launch {
            closeLoginDialog()
            delay(100)
            _isAccManagerOpen.value = true
        }
    }

    fun closeAccManagerDialog() {
        _isAccManagerOpen.value = false
    }

    fun openLoginDialog() {
        viewModelScope.launch {
            closeAccManagerDialog()
            delay(100)
            _isLoginOpen.value = true
        }
    }

    fun closeLoginDialog() {
        if (!_isLoginUiLocked.value) {
            clearErrorMsg()
            _isLoginOpen.value = false
        }
    }

    fun isScreenReady() = ::initialStartScreen.isInitialized
}
