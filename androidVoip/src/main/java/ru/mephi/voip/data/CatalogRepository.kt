package ru.mephi.voip.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.FavouriteRecord
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.network.VoIPServiceApiImpl
import ru.mephi.shared.data.network.exception.*


// TODO: design flaw
class CatalogRepository : KoinComponent {
    private val searchDB: SearchDB by inject()
    private val favoritesDB: FavouritesDB by inject()
    private val catalogDao: CatalogDao by inject()

    fun deleteAllCatalogCache() {
        catalogDao.deleteAll()
    }

    suspend fun getInfoByPhone(num: String): Flow<Resource<List<NameItem>>> = flow {
        emit(Resource.Loading())

        try {
            when (val resource = VoIPServiceApiImpl.getInfoByPhone(num)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(UndefinedException()))
                }
                is Resource.Error.NotFoundError -> emit(Resource.Error.NotFoundError())
                is Resource.Error.ServerNotRespondError -> emit(Resource.Error.ServerNotRespondError())
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    val nameItem = (resource.data as List<NameItem>)[0]
                    if (nameItem.display_name.isEmpty()) emit(Resource.Error.EmptyError())
                    else emit(Resource.Success(resource.data))
                }
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(UndefinedException()))
        }
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