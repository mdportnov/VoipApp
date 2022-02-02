package ru.mephi.shared.data.database.interfaces

import com.squareup.sqldelight.Query
import ru.mephi.shared.data.model.CallRecord

interface ICallRecordsDao {
    fun getAllCallRecords(): Query<CallRecord>

    fun insertAll(vararg record: CallRecord)

    fun deleteRecords(vararg record: CallRecord)

    fun deleteAll()
}