package ru.mephi.shared.data.database

import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus

class CallsDB(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    fun getAllCallRecords() =
        dbQuery.getAllCalls { id, sipNumber, sipName, status, time ->
            CallRecord(
                id,
                sipNumber,
                sipName,
                CallStatus.valueOf(status),
                time
            )
        }

    fun insertAll(vararg record: CallRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.insertCall(
                    it.id,
                    it.sipNumber,
                    it.sipName ?: "",
                    it.status.toString(),
                    it.time
                )
            }
        }
    }

    fun deleteRecords(vararg record: CallRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.deleteCallById(it.id)
            }
        }
    }

    fun deleteAll() {
        dbQuery.deleteAllCalls()
    }
}