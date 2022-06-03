package ru.mephi.voip.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.FavouritesDB
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.database.dto.toKodeIn
import ru.mephi.shared.data.model.*
import ru.mephi.shared.data.network.KtorApiService
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.network.exception.*
import ru.mephi.voip.ui.catalog.init_code_str
import java.util.*

class CatalogRepository : KoinComponent {
    private val api: KtorApiService by inject()
    private val searchDB: SearchDB by inject()
    private val favoritesDB: FavouritesDB by inject()
    private val catalogDao: CatalogDao by inject()

    fun deleteAllCatalogCache() {
        catalogDao.deleteAll()
    }

    suspend fun getInfoByPhone(num: String): Flow<Resource<List<NameItem>>> = flow {
        emit(Resource.Loading())

        try {
            when (val resource = api.getInfoByPhone(num)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(exception = NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(exception = UndefinedException()))
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
            emit(Resource.Error.UndefinedError(exception = UndefinedException()))
        }
    }

    suspend fun getUsersByName(query: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())

        try {
            when (val resource = api.getUsersByName(query)) {
                is Resource.Error.NetworkError -> {
                    emit(Resource.Error.NetworkError(exception = NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(exception = UndefinedException()))
                }
                is Resource.Error.NotFoundError -> emit(Resource.Error.NotFoundError())
                is Resource.Error.ServerNotRespondError -> emit(Resource.Error.ServerNotRespondError())
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    val unitOfUsers = (resource.data as UnitM)
                    unitOfUsers.shortname = query
                    unitOfUsers.code_str = UUID.randomUUID().toString()
                    if (unitOfUsers.appointments.isNullOrEmpty()) emit(
                        Resource.Error.NotFoundError(
                            NotFoundException(query)
                        )
                    )
                    else emit(Resource.Success(unitOfUsers))
                }
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedException()))
        }
    }

    suspend fun getUnitsByName(query: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())
        try {
            when (val resource = api.getUnitsByName(query)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(exception = NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(exception = UndefinedException()))
                }
                is Resource.Error.NotFoundError<*> -> {
                    emit(Resource.Error.NotFoundError(exception = NotFoundException(query)))
                }
                is Resource.Error.ServerNotRespondError -> {
                    emit(Resource.Error.ServerNotRespondError(exception = ServerNotRespondException()))
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    val children = resource.data as List<UnitM>

                    emit(
                        Resource.Success(
                            UnitM(
                                UUID.randomUUID().toString(),
                                query,
                                query,
                                shortname = query,
                                "",
                                "",
                                children,
                                null
                            )
                        )
                    )
                }
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedException()))
        }
    }

    suspend fun getUnitByCodeStr(codeStr: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())

        val unit = catalogDao.getUnitByCodeStr(codeStr)

        if (unit != null) {
            if (unit.code_str == init_code_str) {
                emit(Resource.Success(unit))
                return@flow
            }
        }

        when (val resource = api.getUnitByCodeStr(codeStr)) {
            is Resource.Error.NetworkError<*> -> emit(Resource.Loading())
            is Resource.Error.EmptyError<*> -> emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
            is Resource.Error.UndefinedError<*> -> emit(Resource.Error.UndefinedError(exception = UndefinedException()))
            is Resource.Error.NotFoundError -> emit(Resource.Error.NotFoundError())
            is Resource.Error.ServerNotRespondError -> emit(Resource.Error.ServerNotRespondError())
            is Resource.Loading -> emit(Resource.Loading())
            is Resource.Success<*> -> {
                (resource.data as UnitM).let {
                    catalogDao.add(it.toKodeIn)
                }
            }
        }

        when (val newUnits = catalogDao.getUnitByCodeStr(codeStr)) {
            null -> emit(Resource.Error.NetworkError(exception = NetworkException()))
            else -> emit(Resource.Success(newUnits))
        }
    }

    suspend fun getUserByPhone(phone: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())

        try {
            when (val resource = api.getUserByPhone(phone)) {
                is Resource.Error.NetworkError -> emit(Resource.Error.NetworkError(exception = NetworkException()))
                is Resource.Error.EmptyError<*> -> emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
                is Resource.Error.UndefinedError<*> -> emit(Resource.Error.UndefinedError(exception = UndefinedException()))
                is Resource.Error.NotFoundError<*> -> emit(
                    Resource.Error.NotFoundError(
                        exception = NotFoundException(phone)
                    )
                )
                is Resource.Success<*> -> {
                    val unitOfUsers = (resource.data as UnitM)
                    unitOfUsers.shortname = phone
                    if (unitOfUsers.appointments.isNullOrEmpty()) emit(
                        Resource.Error.NotFoundError(
                            NotFoundException(phone)
                        )
                    )
                    else emit(Resource.Success(unitOfUsers))
                }
                is Resource.Error.ServerNotRespondError -> emit(
                    Resource.Error.ServerNotRespondError(
                        exception = ServerNotRespondException()
                    )
                )
                is Resource.Loading -> emit(Resource.Loading())
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedException()))
        }
    }

    fun getSearchRecords() = searchDB.getAll()

    fun isExistsInDatabase(codeStr: String) = catalogDao.checkByCodeStr(code_str = codeStr)

    fun containsSearchRecord(searchRecord: SearchRecord) = searchDB.isExists(searchRecord.name)

    fun addSearchRecord(record: SearchRecord) = searchDB.insert(record)

    fun deleteAllSearchRecords() = searchDB.deleteAll()

    fun addToFavourite(record: Appointment): Boolean {
        record.line?.let { line ->
            if (!favoritesDB.isExists(line)) {
                favoritesDB.addFavourite(
                    FavouriteRecord(sipName = record.fio, sipNumber = line)
                )
                return true
            }
        }
        return false
    }
}