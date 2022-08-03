package ru.mephi.voip.ui.profile

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.model.FavouriteRecord
import ru.mephi.shared.data.network.GET_PROFILE_PIC_URL_BY_SIP
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.AbtoViewModel
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.utils.getCurrentUserNumber

class ProfileViewModel(
    app: Application,
    private val accountRepository: AccountStatusRepository,
    private val settingsRepository: PreferenceRepository,
    private val favoritesDB: FavouritesDB
) : AbtoViewModel(app), KoinComponent {
    val newLogin: MutableState<String> = mutableStateOf("")
    val newPassword: MutableState<String> = mutableStateOf("")

    val recordsFlow get() = favoritesDB.getAllFavourites()

    private val _expandedMenuId = MutableStateFlow(-1)
    val expandedMenuId: StateFlow<Int> get() = _expandedMenuId

    fun onFavouriteClicked(cardId: Int) {
        _expandedMenuId.value = cardId
    }

    fun onNewAccountInputChange(
        login: String = newLogin.value,
        password: String = newPassword.value
    ) {
        newLogin.value = login
        newPassword.value = password
    }

    val imageUrl: String
        get() = GET_PROFILE_PIC_URL_BY_SIP + accountRepository.getUserNumber()

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

    val accountList: StateFlow<List<Account>>
        get() = accountRepository.accountList

    fun deleteFromFavourite(record: FavouriteRecord) = favoritesDB.deleteRecords(record)
}