package ru.mephi.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.dto.toKodeIn
import ru.mephi.shared.data.database.interfaces.ISearchRecordsDao
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.*

class CatalogRepository : KoinComponent {
    private val api: ApiHelper by inject()
    private val dao: ISearchRecordsDao<SearchRecord> by inject()
    private val catalogDao: CatalogDao by inject()

    suspend fun getInfoByPhone(num: String): Flow<Resource<NameItem>> = flow {
        emit(Resource.Loading())

        try {
            val name = api.getInfoByPhone(num)
            if (name?.display_name.isNullOrEmpty())
                emit(Resource.Error.EmptyError())
            else
                emit(Resource.Success(name))
        } catch (exception: Exception) {
            emit(
                Resource.Error.UndefinedError(
                    exception = Exception(
                        exception.message ?: "Error Occurred!"
                    )
                )
            )
        }
    }

    suspend fun getUnitsByName(query: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())

        val children = api.getUnitsByName(query).map { it }

        if (children.isNullOrEmpty())
            emit(Resource.Error.NotFoundError(exception = NotFoundException(query)))
        else
            emit(
                Resource.Success(
                    UnitM(
                        "", query, query, shortname = query,
                        "", "", children, null
                    )
                )
            )
    }

    suspend fun getUsersByName(query: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())
        val usersUnit = api.getUsersByName(query)
        usersUnit.shortname = query

        if (usersUnit.appointments.isNullOrEmpty())
            emit(Resource.Error.NotFoundError(NotFoundException(query)))
        else
            emit(Resource.Success(usersUnit))
    }

    suspend fun getUnitByCodeStr(codeStr: String): Flow<Resource<List<UnitM>?>> = flow {
        emit(Resource.Loading())

        val units = catalogDao.getUnitByCodeStr(codeStr)

        emit(Resource.Loading(units))
        val resource = api.getUnitByCodeStr(codeStr)

        when (resource) {
            is Resource.Error.NetworkError<*> -> {
                emit(Resource.Error.NetworkError(exception = NetworkException()))
            }
            is Resource.Error.EmptyError<*> -> {
                emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
            }
            is Resource.Error.UndefinedError<*> -> {
                emit(Resource.Error.UndefinedError(exception = Exception("Oops. Error!")))
            }

            else -> {}
        }
        if (resource is Resource.Success<*>)
            if (!(resource.data as List<UnitM>).isNullOrEmpty()) {
                units?.forEach {
                    catalogDao.deleteByCodeStr(it.code_str)
                }
                resource.data.forEach {
                    catalogDao.add(it.toKodeIn)
                }
            }

        val newUnits = catalogDao.getUnitByCodeStr(codeStr)
        when {
            newUnits == null -> emit(Resource.Error.NetworkError(exception = NetworkException()))
            newUnits.isEmpty() -> emit(Resource.Error.EmptyError(exception = EmptyUnitException()))
            else -> emit(Resource.Success(newUnits))
        }
    }

    fun getSearchRecords() = dao.getAll()

    fun containsSearchRecord(searchRecord: SearchRecord) = dao.isExists(searchRecord.name)

    fun addSearchRecord(record: SearchRecord) = dao.insertAll(record)

    fun deleteAllSearchRecords() = dao.deleteAll()
}