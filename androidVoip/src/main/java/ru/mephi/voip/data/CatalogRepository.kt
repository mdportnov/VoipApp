package ru.mephi.voip.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.database.dto.toKodeIn
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.KtorApiService
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.network.exception.EmptyUnitException
import ru.mephi.shared.data.network.exception.NetworkException
import ru.mephi.shared.data.network.exception.NotFoundException
import ru.mephi.shared.data.network.exception.UndefinedErrorException
import ru.mephi.voip.utils.isOnline
import java.util.*

class CatalogRepository : KoinComponent {
    private val api: KtorApiService by inject()
    private val searchDB: SearchDB by inject()
    private val catalogDao: CatalogDao by inject()

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
                    emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
                }
                is Resource.Success<*> -> {
                    val nameItem = (resource.data as List<NameItem>)[0]
                    if (nameItem.display_name.isEmpty())
                        emit(Resource.Error.EmptyError())
                    else
                        emit(Resource.Success(resource.data))
                }
                else -> {}
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
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
                    emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
                }
                is Resource.Success<*> -> {
                    val unitOfUsers = (resource.data as UnitM)
                    unitOfUsers.shortname = query
                    unitOfUsers.code_str = UUID.randomUUID().toString()
                    if (unitOfUsers.appointments.isNullOrEmpty())
                        emit(Resource.Error.NotFoundError(NotFoundException(query)))
                    else
                        emit(Resource.Success(unitOfUsers))
                }
                else -> {}
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
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
                    emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
                }
                is Resource.Error.NotFoundError<*> -> {
                    emit(Resource.Error.NotFoundError(exception = NotFoundException(query)))
                }
                is Resource.Success<*> -> {
                    val children = resource.data as List<UnitM>

                    emit(
                        Resource.Success(
                            UnitM(
                                UUID.randomUUID().toString(), query, query, shortname = query,
                                "", "", children, null
                            )
                        )
                    )
                }
                else -> {}
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
        }
    }

    suspend fun getUnitByCodeStr(codeStr: String): Flow<Resource<List<UnitM>?>> = flow {
        emit(Resource.Loading())

        val units = catalogDao.getUnitByCodeStr(codeStr)

        when (val resource = api.getUnitByCodeStr(codeStr)) {
            is Resource.Error.NetworkError<*> -> {
                emit(Resource.Error.NetworkError(exception = NetworkException()))
            }
            is Resource.Error.EmptyError<*> -> {
                emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
            }
            is Resource.Error.UndefinedError<*> -> {
                if (isOnline(appContext))
                    emit(Resource.Error.ServerNotRespondError(exception = UndefinedErrorException()))
                else
                    emit(Resource.Error.NetworkError(exception = NetworkException()))
            }
            is Resource.Success<*> -> {
                if (!(resource.data as List<UnitM>).isNullOrEmpty()) {
                    units?.forEach {
                        catalogDao.deleteByCodeStr(it.code_str)
                    }
                    resource.data!!.forEach {
                        catalogDao.add(it.toKodeIn)
                    }
                }
            }
            is Resource.Error.NotFoundError -> emit(Resource.Error.NotFoundError())
            is Resource.Error.ServerNotRespondError -> emit(Resource.Error.ServerNotRespondError())
            is Resource.Loading -> emit(Resource.Loading())
        }

        val newUnits = catalogDao.getUnitByCodeStr(codeStr)
        when {
            newUnits == null -> emit(Resource.Error.NetworkError(exception = NetworkException()))
            newUnits.isEmpty() -> emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
            else -> emit(Resource.Success(newUnits))
        }
    }

    suspend fun getUserByPhone(phone: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())

        try {
            when (val resource = api.getUserByPhone(phone)) {
                is Resource.Error.NetworkError -> {
                    emit(Resource.Error.NetworkError(exception = NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
                }
                is Resource.Error.NotFoundError<*> -> {
                    emit(Resource.Error.NotFoundError(exception = NotFoundException(phone)))
                }
                is Resource.Success<*> -> {
                    val unitOfUsers = (resource.data as UnitM)
                    unitOfUsers.shortname = phone
                    if (unitOfUsers.appointments.isNullOrEmpty())
                        emit(Resource.Error.NotFoundError(NotFoundException(phone)))
                    else
                        emit(Resource.Success(unitOfUsers))
                }
                else -> {}
            }
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(exception = UndefinedErrorException()))
        }
    }

    fun getSearchRecords() = searchDB.getAll()

    fun isExistsInDatabase(codeStr: String) = catalogDao.checkByCodeStr(code_str = codeStr)

    fun containsSearchRecord(searchRecord: SearchRecord) = searchDB.isExists(searchRecord.name)

    fun addSearchRecord(record: SearchRecord) = searchDB.insert(record)

    fun deleteAllSearchRecords() = searchDB.deleteAll()
}