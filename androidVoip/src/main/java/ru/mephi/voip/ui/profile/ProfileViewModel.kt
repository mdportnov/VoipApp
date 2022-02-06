package ru.mephi.voip.ui.profile

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.koin.core.component.KoinComponent
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.Account
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.call.AbtoViewModel
import ru.mephi.voip.call.getAccountsList
import ru.mephi.voip.call.getActiveAccount
import ru.mephi.voip.call.saveAccounts
import ru.mephi.voip.ui.SharedViewModel
import ru.mephi.voip.ui.utils.getCurrentUserNumber

class ProfileViewModel(
    app: Application,
    override var sp: SharedPreferences,
    private val sharedVM: SharedViewModel,
//    private val sipService: MySipService
) : AbtoViewModel(app, sp), KoinComponent {

    private val _accountsList: MutableLiveData<List<Account>> = MutableLiveData()
    val accountList: LiveData<List<Account>> = _accountsList

    val newLogin: MutableState<String> = mutableStateOf("")
    val newPassword: MutableState<String> = mutableStateOf("")

    fun onNewAccountInputChange(
        login: String = newLogin.value,
        password: String = newPassword.value
    ) {
        newLogin.value = login
        newPassword.value = password
    }

    fun getImageUrl() = KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + phone.getCurrentUserNumber()

    fun addNewAccount() {
        val list = getAccountsList()

        list.forEach { it.isActive = false }

        list.add(
            Account(
                newLogin.value,
                newPassword.value,
                true
            )
        )

        sharedVM.fetchAccountsCount()

        saveAccounts(list)
        retryRegistration()
    }

    fun removeAccount(account: Account) {
        val list = getAccountsList()
        list.removeAll { it.login == account.login }

        if (getActiveAccount() == account) // если активный аккаунт удаляется
            phone.unregister()

        sharedVM.fetchAccountsCount()
        saveAccounts(list)
    }

    fun retryRegistration() {
        sharedVM.fetchStatus(AccountStatus.CHANGING)
        getActiveAccount()?.let {
            updateActiveAccount(it)
        }
    }

    fun updateActiveAccount(account: Account): String {
        val list = getAccountsList()
        sharedVM.fetchStatus(AccountStatus.CHANGING)

        list.forEach { it.isActive = false }
        list.forEach {
            if (account.login == it.login)
                it.isActive = true
        }

        saveAccounts(list)

        val acc = getActiveAccount()
        val username = acc?.login
        val password = acc?.password

        phone.config.addAccount(
            appContext.getString(R.string.sip_domain),
            "",
            username, password, null, "",
            300,
            true
        )

        phone.config.registerTimeout = 3000
        phone.restartSip()

        _accountsList.postValue(list)
        sharedVM.fetchAccountsCount()

        return acc!!.login
    }
}