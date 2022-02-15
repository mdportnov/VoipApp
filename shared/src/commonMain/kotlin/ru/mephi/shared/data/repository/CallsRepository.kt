package ru.mephi.shared.data.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import ru.mephi.shared.data.database.interfaces.ICallRecordsDao
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.network.ApiHelper
import ru.mephi.shared.data.network.Resource

class CallsRepository(
    private val dao: ICallRecordsDao,
    private val api: ApiHelper
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
        val time: Long = Clock.System.now().epochSeconds
//        val currentMoment: Instant = Clock.System.now()
//        val datetimeInUtc: LocalDateTime =
//            currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
//        val time = datetimeInUtc.toInstant(TimeZone.currentSystemDefault()).epochSeconds

        if (sipName == null) {
            CoroutineScope(Dispatchers.Default).launch {
                when (val resource = api.getInfoByPhone(sipNumber)) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if (resource.data.isNullOrEmpty())
                            dao.insertAll(CallRecord(null, sipNumber, null, status, time))
                        else
                            dao.insertAll(
                                CallRecord(
                                    null,
                                    sipNumber,
                                    resource.data[0].display_name,
                                    status,
                                    time
                                )
                            )
                    }
                    else -> dao.insertAll(CallRecord(null, sipNumber, null, status, time))
                }
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
    }
}