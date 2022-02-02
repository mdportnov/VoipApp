package ru.mephi.shared.data.repository

import io.ktor.utils.io.errors.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.dto.toKodeIn
import ru.mephi.shared.data.database.interfaces.ISearchRecordsDao
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.ApiHelper
import ru.mephi.shared.data.network.EmptyUnitException
import ru.mephi.shared.data.network.Resource

class CatalogRepository(
    private val api: ApiHelper,
    private val dao: ISearchRecordsDao<SearchRecord>,
    private val catalogDao: CatalogDao
) {
    suspend fun getInfoByPhone(num: String): Flow<Resource<NameItem>> = flow {
        emit(Resource.Loading())

        try {
            val name = api.getInfoByPhone(num)
            if (name?.display_name.isNullOrEmpty())
                emit(Resource.Error.EmptyError())
            else
                emit(Resource.Success(name))
        } catch (exception: Exception) {
            emit(Resource.Error.UndefinedError(message = exception.message ?: "Error Occurred!"))
        }
    }

    suspend fun getUnitsByName(query: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())

        val children = api.getUnitsByName(query).map { it }

        if (children.isNullOrEmpty())
            emit(Resource.Error.NotFoundError("По запросу \"${query}\" ничего не найдено"))
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
            emit(Resource.Error.NotFoundError("По запросу \"${query}\" ничего не найдено"))
        else
            emit(Resource.Success(usersUnit))
    }

    suspend fun getUnitsByCodeStr(codeStr: String): Flow<Resource<List<UnitM>>> = flow {
        emit(Resource.Loading())

        val units = catalogDao.getUnitByCodeStr(codeStr)

        emit(Resource.Loading(units))

        try {
            val remoteUnits = api.getUnitByCodeStr(codeStr)
            units?.forEach {
                catalogDao.deleteByCodeStr(it.code_str)
            }
            remoteUnits.forEach {
                catalogDao.add(it.toKodeIn)
            }
        }
//        catch (e: Http) {
//            emit(Resource.Error.UndefinedError("Oops, smth went wrong", units))
//        }
        catch (e: IOException) { // e.g. without internet case
            emit(Resource.Error.UndefinedError(e.message ?: "Error Occurred!"))
        } catch (e: EmptyUnitException) {
            emit(Resource.Error.EmptyError(e.message))
        }

        val newUnits = catalogDao.getUnitByCodeStr(codeStr)
        if (newUnits.isNullOrEmpty())
            emit(Resource.Error.EmptyError("Пустой пункт"))
        else
            emit(Resource.Success(newUnits))
    }

    fun getSearchRecords() = dao.getAll()

    fun containsSearchRecord(searchRecord: SearchRecord) = dao.isExists(searchRecord.name)

    fun addSearchRecord(record: SearchRecord) = dao.insertAll(record)

    fun deleteAllSearchRecords() = dao.deleteAll()
}