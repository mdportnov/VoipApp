package ru.mephi.shared.vm

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.repository.CallsRepository

open class CallerViewModel : MainIoExecutor(), KoinComponent {
    private val repository: CallsRepository by inject()

    val callHistory = repository.getAllCallRecords()

    fun addRecord(record: CallRecord) = repository.addRecord(record)

    fun addRecord(sipNumber: String, sipName: String? = null, status: CallStatus) =
        repository.addRecord(sipNumber, sipName, status)

    fun deleteAllRecords() = repository.deleteAllRecords()

    fun getAllCallRecords() = repository.getAllCallRecords().executeAsList()

    fun deleteRecord(record: CallRecord) = repository.deleteRecord(record)
}