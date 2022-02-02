package ru.mephi.shared.data.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.mephi.shared.data.database.interfaces.ICallRecordsDao
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus

class CallsRepository(private val dao: ICallRecordsDao) {
    init {
        dao.deleteAll()
        dao.insertAll(
            CallRecord(
                1, "8877", "Труттце А.А.", CallStatus.INCOMING, 1643050000
            ),
            CallRecord(
                3, "09025", "Portnov M.D.", CallStatus.INCOMING, 1643057784
            ),
            CallRecord(
                2, "09025", "Portnov M.D.", CallStatus.INCOMING, 1643054444
            ),
            CallRecord(
                4, "09025", "Portnov M.D.", CallStatus.INCOMING, 1643054444
            ),
        )

        CoroutineScope(Dispatchers.Default).launch {
            dao.getAllCallRecords().asFlow().mapToList().collect {
                println(it.joinToString("\n"))
            }
        }
    }

    fun getAllCallRecords() = dao.getAllCallRecords()

    fun addRecord(record: CallRecord) = dao.insertAll(record)

    fun deleteAllRecords() = dao.deleteAll()

    fun deleteRecord(record: CallRecord) = dao.deleteRecords(record)

    fun deleteRecords(vararg records: CallRecord) {
        dao.deleteRecords(*records)
    }
}