package ru.mephi.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.VoIPServiceRepository

class SavedAccountsViewModel : MainIoExecutor() {

    private val vsRepo: VoIPServiceRepository by inject()
    private val cDao: CatalogDao by inject()

    val sipList = MutableStateFlow(emptyList<String>())
    val accountsMap: MutableMap<String, MutableStateFlow<Appointment>> = mutableMapOf()
    val currentAccount = MutableStateFlow(Appointment())

    init {
        launch(ioDispatcher) {
            sipList.collect { lst ->
                accountsMap.clear()
                lst.forEach { v ->
                    MutableStateFlow(Appointment(
                        line = v,
                        lineShown = v
                    )).let {
                        accountsMap[v] = it
                        fetchAccountInfo(it)
                    }
                }
            }
        }
    }

    fun setCurrentAccount(
        SIP: String
    ) {
        currentAccount.value = Appointment(lineShown = SIP)
        fetchAccountInfo(currentAccount)
    }

    private fun fetchAccountInfo(app: MutableStateFlow<Appointment>) {
        launch(ioDispatcher) {
            if (cDao.isUserExistsBySIP(app.value.lineShown)) {
                app.value = cDao.getUserBySIP(app.value.lineShown)
                return@launch
            }
            vsRepo.getUserByPhone(app.value.lineShown).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        resource.data?.let {
                            app.value = it
                            cDao.addUser(it)
                        }
                    }
                    is Resource.Error.EmptyError -> { }
                    is Resource.Error.NotFoundError -> { }
                    is Resource.Error.NetworkError -> { }
                    is Resource.Error.ServerNotRespondError -> { }
                    is Resource.Error.UndefinedError -> { }
                }
            }.launchIn(this)
        }
    }

}
