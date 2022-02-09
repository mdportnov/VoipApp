package ru.mephi.voip.ui.profile

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.koin.core.component.KoinComponent
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.shared.data.repository.CatalogRepository
import ru.mephi.voip.ui.call.AbtoViewModel
import ru.mephi.voip.data.AccountStatusRepository

class ProfileViewModel(
    app: Application,
    override var sp: SharedPreferences,
    val catalogRepository: CatalogRepository,
    val accountRepository: AccountStatusRepository
) : AbtoViewModel(app, sp), KoinComponent {

    val newLogin: MutableState<String> = mutableStateOf("")
    val newPassword: MutableState<String> = mutableStateOf("")

    fun onNewAccountInputChange(
        login: String = newLogin.value,
        password: String = newPassword.value
    ) {
        newLogin.value = login
        newPassword.value = password
    }

    fun getImageUrl() =
        KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + accountRepository.getUserNumber()

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
        accountRepository.toggleSipStatus()
    }
}