package ru.mephi.shared.data.database.interfaces

import ru.mephi.shared.data.model.SearchRecord

interface ISearchRecordsDao {
    fun getAll(): List<SearchRecord>

    fun insertAll(vararg record: SearchRecord)

    fun deleteRecords(vararg record: SearchRecord)

    fun deleteAll()

    fun isExists(name: String): Boolean
}