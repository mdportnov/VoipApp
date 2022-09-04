package ru.mephi.voip.data

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.FavouriteRecord


// TODO: design flaw
class CatalogRepository : KoinComponent {
    private val searchDB: SearchDB by inject()
    private val favoritesDB: FavouritesDB by inject()
    private val catalogDao: CatalogDao by inject()

    fun deleteAllCatalogCache() {
        catalogDao.deleteAll()
    }

    fun deleteAllSearchRecords() = searchDB.deleteAll()

    fun addToFavourites(record: Appointment): SavingResult {
        return if (record.line.isNullOrEmpty()) {
            SavingResult.EMPTY_LINE
        } else
            if (favoritesDB.isExists(record.line!!)) {
                SavingResult.ALREADY_SAVED
            } else {
                favoritesDB.addFavourite(
                    FavouriteRecord(sipName = record.fio, sipNumber = record.line!!)
                )
                SavingResult.SUCCESS
            }
    }
}