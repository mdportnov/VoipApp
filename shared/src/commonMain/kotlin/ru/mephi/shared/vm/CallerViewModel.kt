package ru.mephi.shared.vm

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.repo.CallsRepository

open class CallerViewModel : MainIoExecutor(), KoinComponent {
    private val repository: CallsRepository by inject()

    val callHistory = repository.getAllCallRecords()

    fun addRecord(record: CallRecord) = repository.addRecord(record)

    fun addRecord(sipNumber: String, sipName: String? = null, status: CallStatus, duration: Long) =
        repository.addRecord(sipNumber, sipName, status, duration)

    fun deleteRecord(record: CallRecord) = repository.deleteRecord(record)

    fun deleteAllRecords() = repository.deleteAllRecords()

    fun getAllCallsBySipNumber(sipNumber: String) = repository.getAllCallsBySipNumber(sipNumber)

    fun getAllCallRecords() = repository.getAllCallRecords().executeAsList()

    fun getAllRecordsFlow() = repository.getAllCallRecords().asFlow().mapToList()

    private val _expandedCardIdsList = MutableStateFlow(listOf<Int>())
    val expandedCardIdsList: StateFlow<List<Int>> get() = _expandedCardIdsList

    fun onCardArrowClicked(cardId: Int) {
        _expandedCardIdsList.value = _expandedCardIdsList.value.toMutableList().also { list ->
            if (list.contains(cardId)) list.remove(cardId) else list.add(cardId)
        }
    }

}