package ru.mephi.voip.ui.profile

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.AbtoViewModel
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.utils.getCurrentUserNumber

class ProfileViewModel(
    app: Application,
    val accountRepository: AccountStatusRepository,
    private val settingsRepository: PreferenceRepository
) : AbtoViewModel(app), KoinComponent {
    val newLogin: MutableState<String> = mutableStateOf("")
    val newPassword: MutableState<String> = mutableStateOf("")

    fun onNewAccountInputChange(
        login: String = newLogin.value,
        password: String = newPassword.value
    ) {
        newLogin.value = login
        newPassword.value = password
    }

    val imageUrl: String
        get() = KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + accountRepository.getUserNumber()

    fun addNewAccount() {
        accountRepository.addNewAccount(
            newLogin = newLogin.value,
            newPassword = newPassword.value
        )
    }

    fun removeAccount(account: Account) {
        accountRepository.removeAccount(account)
    }

    fun retryRegistration() {
        accountRepository.retryRegistration()
    }

    fun updateActiveAccount(account: Account): String {
        return accountRepository.updateActiveAccount(account)
    }

    fun toggleSipStatus() {
        viewModelScope.launch {
            settingsRepository.toggleSip()
        }
    }

    fun getCurrentUserNumber(): String? {
        return phone.getCurrentUserNumber()
    }
}