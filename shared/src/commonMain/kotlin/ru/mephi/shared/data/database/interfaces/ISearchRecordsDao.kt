package ru.mephi.shared.data.database.interfaces

interface ISearchRecordsDao<T> {
    fun getAll(): List<T>

    fun insertAll(vararg record: T)

    fun deleteRecords(vararg record: T)

    fun deleteAll()

    fun isExists(name: String): Boolean
}