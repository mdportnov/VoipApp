package ru.mephi.shared.data.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import ru.mephi.shared.base.MainIoExecutor
import ru.mephi.shared.data.database.interfaces.ICallRecordsDao
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.network.ApiHelper
import ru.mephi.shared.data.network.KtorApiService

class CallsRepository(
    private val dao: ICallRecordsDao,
) {
    private fun logAllCalls() {
        CoroutineScope(Dispatchers.Default).launch {
            dao.getAllCallRecords().asFlow().mapToList().collect {
                println(it.joinToString("\n"))
            }
        }
    }

    fun getAllCallRecords() = dao.getAllCallRecords()

    fun addRecord(sipNumber: String, sipName: String? = null, status: CallStatus) {
        val time = Clock.System.now().epochSeconds
        println("Adding call record $sipNumber: $time")
        var nameItem: NameItem?
        if (sipName == null) {
            CoroutineScope(Dispatchers.Default).launch {
//                nameItem = api.getInfoByPhone(sipNumber)
                dao.insertAll(
                    CallRecord(null, sipNumber, null, status, time)
                )
            }
        } else {
            dao.insertAll(
                CallRecord(null, sipNumber, sipName, status, time)
            )
        }
    }

    fun addRecord(record: CallRecord) = dao.insertAll(record)

    fun deleteAllRecords() = dao.deleteAll()

    fun deleteRecord(record: CallRecord) {
        dao.deleteRecords(record)
        println("After deletion: ")
        logAllCalls()
    }
}