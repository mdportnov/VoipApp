package ru.mephi.shared.vm

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.repo.VoIPServiceRepository

class DetailedInfoViewModel : MainIoExecutor(), KoinComponent {

    private val lVM: LoggerViewModel by inject()
    private val vsRepo: VoIPServiceRepository by inject()
    private val cDao: CatalogDao by inject()

    val detailedInfo = MutableStateFlow(Appointment())

    private var job: Job = launch(ioDispatcher) { }

    fun loadDetailedInfo(
        sip: String = "",
        appointment: Appointment = Appointment()
    ) {
        job.cancel()
        detailedInfo.value = appointment
        val realSip = when {
            sip.isNotEmpty() -> sip.trim()
            appointment.line.isNotEmpty() -> appointment.line.trim()
            appointment.lineShown.isNotEmpty() -> appointment.lineShown.trim()
            else -> ""
        }

        if (realSip.isEmpty()) {
            lVM.e("SIP is empty, aborting to load detailed info!")
            return
        }

        job = launch(ioDispatcher) {
            if (cDao.isUserExistsBySIP(realSip)) {
                detailedInfo.value = cDao.getUserBySIP(realSip)
                return@launch
            }
            vsRepo.getUserByPhone(realSip).onEach { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        resource.data?.let {
                            cDao.addUser(it)
                            detailedInfo.value = it
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