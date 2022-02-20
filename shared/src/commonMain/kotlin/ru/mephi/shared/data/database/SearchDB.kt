package ru.mephi.shared.data.database

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.SearchType

class SearchDB : KoinComponent {
    private val databaseDriverFactory: DatabaseDriverFactory by inject()
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    fun getAll(): List<SearchRecord> =
        dbQuery.getAllSearches { id, name, type ->
            SearchRecord(id, name, SearchType.valueOf(type))
        }.executeAsList()

    fun insert(record: SearchRecord) {
        dbQuery.insertAllSearches(record.id, record.name, record.type.name)
    }

    fun deleteRecords(vararg record: SearchRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.deleteSearchById(it.id)
            }
        }
    }

    fun deleteAll() {
        dbQuery.deleteAllSearches()
    }

    fun isExists(name: String) =
        dbQuery.isExistsSearch(name).executeAsOne()
}