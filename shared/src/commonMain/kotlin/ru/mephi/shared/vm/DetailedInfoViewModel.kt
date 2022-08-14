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
    private var fixDetailedInfo = Appointment()
    val status = MutableStateFlow(DetailedInfoStatus.LOADING)

    private var job: Job = launch(ioDispatcher) { }

    fun loadDetailedInfo(
        sip: String = "",
        appointment: Appointment = Appointment()
    ) {
        job.cancel()
        status.value = DetailedInfoStatus.LOADING
        fixDetailedInfo = appointment
        val realSip = when {
            sip.isNotEmpty() -> sip.trim()
            appointment.line.isNotEmpty() -> appointment.line.trim()
            appointment.lineShown.isNotEmpty() -> appointment.lineShown.trim()
            else -> ""
        }

        if (realSip.isEmpty()) {
            status.value = DetailedInfoStatus.BAD_RESULT
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
                    is Resource.Loading -> {
                        status.value = DetailedInfoStatus.LOADING
                    }
                    is Resource.Success -> {
                        resource.data?.let {
                            if (fixDetailedInfo.email.isNotEmpty()) {
                                it.email = fixDetailedInfo.email
                            }
                            cDao.addUser(it)
                            detailedInfo.value = it
                            status.value = DetailedInfoStatus.OK
                        } ?: run {
                            detailedInfo.value = fixDetailedInfo
                        }
                    }
                    is Resource.Error.EmptyError -> { status.value = DetailedInfoStatus.BAD_RESULT }
                    is Resource.Error.NotFoundError -> { status.value = DetailedInfoStatus.BAD_RESULT }
                    is Resource.Error.NetworkError -> { status.value = DetailedInfoStatus.NETWORK_FAILURE }
                    is Resource.Error.ServerNotRespondError -> { status.value = DetailedInfoStatus.NETWORK_FAILURE }
                    is Resource.Error.UndefinedError -> { status.value = DetailedInfoStatus.NETWORK_FAILURE }
                }
            }.launchIn(this)
        }
    }

}

enum class DetailedInfoStatus {
    OK, LOADING, BAD_RESULT, NETWORK_FAILURE
}