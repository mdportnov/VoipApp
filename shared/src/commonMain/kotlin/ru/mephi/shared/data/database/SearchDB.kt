package ru.mephi.shared.data.database

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.vm.SearchType

class SearchDB : KoinComponent {
    private val databaseDriverFactory: DatabaseDriverFactory by inject()
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    fun getAll(): List<SearchRecord> =
        dbQuery.getAllSearches { id, searchStr, searchType ->
            SearchRecord(id, searchStr, SearchType.valueOf(searchType))
        }.executeAsList()

    fun insert(record: SearchRecord) {
        dbQuery.insertAllSearches(record.id, record.searchStr, record.searchType.name)
    }

    fun delete(record: SearchRecord) {
        dbQuery.deleteSearch(record.searchStr, record.searchType.name)
    }

    fun deleteAll() {
        dbQuery.deleteAllSearches()
    }

    fun isExists(
        searchStr: String,
        searchType: SearchType
    ): Boolean {
        return dbQuery.isExistsSearch(searchStr, searchType.name).executeAsOne()
    }
}