package ru.mephi.shared.data.database

import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.database.interfaces.ISearchRecordsDao
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.SearchType

class SearchDB(databaseDriverFactory: DatabaseDriverFactory) :
    ISearchRecordsDao {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    override fun getAll(): List<SearchRecord> =
        dbQuery.getAllSearches { id, name, type ->
            SearchRecord(id, name, SearchType.valueOf(type))
        }.executeAsList()

    override fun insertAll(vararg record: SearchRecord) {
        dbQuery.transaction {
            record.forEach { dbQuery.insertAllSearches(it.id, it.name, it.type.name) }
        }
    }

    override fun deleteRecords(vararg record: SearchRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.deleteSearchById(it.id)
            }
        }
    }

    override fun deleteAll() {
        dbQuery.deleteAllSearches()
    }

    override fun isExists(name: String) =
        dbQuery.isExistsSearch(name).executeAsOne()
}