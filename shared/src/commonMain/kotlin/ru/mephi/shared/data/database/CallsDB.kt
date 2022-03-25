package ru.mephi.shared.data.database

import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus

class CallsDB(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    fun getAllCallsBySipNumber(number: String) =
        dbQuery.getAllCallsBySipNumber(number) { id, sipNumber, sipName, status, time, duration ->
            CallRecord(
                id,
                sipNumber,
                sipName,
                CallStatus.valueOf(status),
                time, duration!!
            )
        }

    fun getAllCallRecords() =
        dbQuery.getAllCalls { id, sipNumber, sipName, status, time, duration ->
            CallRecord(
                id,
                sipNumber,
                sipName,
                CallStatus.valueOf(status),
                time, duration!!
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
                    it.time, it.duration
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