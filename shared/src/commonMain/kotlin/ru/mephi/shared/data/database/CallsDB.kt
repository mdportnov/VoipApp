package ru.mephi.shared.data.database

import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.database.interfaces.ICallRecordsDao
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus

class CallsDB(databaseDriverFactory: DatabaseDriverFactory) :
    ICallRecordsDao {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    override fun getAllCallRecords() =
        dbQuery.getAllCalls { id, sipNumber, sipName, status, time ->
            CallRecord(
                id,
                sipNumber,
                sipName,
                CallStatus.valueOf(status),
                time
            )
        }

    override fun insertAll(vararg record: CallRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.insertCall(
                    it.id,
                    it.sipName ?: "",
                    it.sipNumber,
                    it.status.toString(),
                    it.time
                )
            }
        }
    }

    override fun deleteRecords(vararg record: CallRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.deleteCallById(it.id)
            }
        }
    }

    override fun deleteAll() {
        dbQuery.deleteAllCalls()
    }

}