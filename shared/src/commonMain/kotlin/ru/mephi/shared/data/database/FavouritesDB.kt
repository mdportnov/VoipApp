package ru.mephi.shared.data.database

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.AppDatabase
import ru.mephi.shared.data.model.FavouriteRecord

class FavouritesDB : KoinComponent {
    private val databaseDriverFactory: DatabaseDriverFactory by inject()
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    fun getAllFavourites(): List<FavouriteRecord> =
        dbQuery.getAllFavorites { id, sipNumber, sipName, created_at ->
            FavouriteRecord(id, sipNumber, sipName, created_at)
        }.executeAsList()

    fun addFavourite(record: FavouriteRecord) {
        dbQuery.addFavourite(record.id, record.sipNumber, record.sipName, record.created_at)
    }

    fun deleteRecords(vararg record: FavouriteRecord) {
        dbQuery.transaction {
            record.forEach {
                dbQuery.deleteFavouriteById(it.id)
            }
        }
    }

    fun deleteAll() {
        dbQuery.deleteAllFavorites()
    }

    fun isExists(number: String) = dbQuery.isExistsFavourite(number).executeAsOne()
}