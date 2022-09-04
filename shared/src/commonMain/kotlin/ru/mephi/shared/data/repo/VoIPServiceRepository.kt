package ru.mephi.shared.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.Resource
import ru.mephi.shared.data.network.VoIPServiceApiImpl
import ru.mephi.shared.data.network.exception.*

class VoIPServiceRepository {

    suspend fun getUserByPhone(SIP: String): Flow<Resource<Appointment>> = flow {
        emit(Resource.Loading())
        try {
            when (val resource = VoIPServiceApiImpl.getUserByPhone(SIP)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(UndefinedException()))
                }
                is Resource.Error.NotFoundError -> {
                    emit(Resource.Error.NotFoundError(NotFoundException(SIP)))
                }
                is Resource.Error.ServerNotRespondError -> {
                    emit(Resource.Error.ServerNotRespondError(ServerNotRespondException()))
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    resource.data?.let { unit ->
                        if (unit.appointments.isEmpty()) {
                            emit(Resource.Error.EmptyError(EmptyUnitException()))
                        } else {
                            emit(Resource.Success(unit.appointments[0]))
                        }
                    } ?: run {
                        emit(Resource.Error.EmptyError(EmptyUnitException()))
                    }
                }
            }
        } catch (exception: Exception) { }
    }

    suspend fun getUsersByName(searchName: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())
        try {
            when (val resource = VoIPServiceApiImpl.getUsersByName(searchName)) {
                is Resource.Error.NetworkError -> {
                    emit(Resource.Error.NetworkError(NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(UndefinedException()))
                }
                is Resource.Error.NotFoundError -> {
                    emit(Resource.Error.NotFoundError(NotFoundException(searchName)))
                }
                is Resource.Error.ServerNotRespondError -> {
                    emit(Resource.Error.ServerNotRespondError(ServerNotRespondException()))
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    resource.data?.let { unit ->
                        if (unit.appointments.isEmpty()) {
                            emit(Resource.Error.EmptyError(EmptyUnitException()))
                        } else {
                            emit(Resource.Success(
                                UnitM(
                                    shortname = searchName,
                                    appointments = unit.appointments
                                )
                            ))
                        }
                    } ?: run {
                        emit(Resource.Error.EmptyError(EmptyUnitException()))
                    }
                }
            }
        } catch (exception: Exception) { }
    }

    suspend fun getUnitsByName(unitName: String): Flow<Resource<List<UnitM>>> = flow {
        emit(Resource.Loading())
        try {
            when (val resource = VoIPServiceApiImpl.getUnitsByName(unitName)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(UndefinedException()))
                }
                is Resource.Error.NotFoundError<*> -> {
                    emit(Resource.Error.NotFoundError(NotFoundException(unitName)))
                }
                is Resource.Error.ServerNotRespondError -> {
                    emit(Resource.Error.ServerNotRespondError(ServerNotRespondException()))
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    resource.data?.let { units ->
                        emit(Resource.Success(units))
                    } ?: run {
                        emit(Resource.Error.EmptyError(EmptyUnitException()))
                    }
                }
            }
        } catch (exception: Exception) { }
    }

    suspend fun getUnitByCodeStr(codeStr: String): Flow<Resource<UnitM>> = flow {
        emit(Resource.Loading())
        try {
            when (val resource = VoIPServiceApiImpl.getUnitByCodeStr(codeStr)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(UndefinedException()))
                }
                is Resource.Error.NotFoundError -> {
                    emit(Resource.Error.NotFoundError())
                }
                is Resource.Error.ServerNotRespondError -> emit(Resource.Error.ServerNotRespondError())
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    resource.data?.let { unit ->
                        if (unit.code_str.isEmpty()) {
                            emit(Resource.Error.EmptyError(EmptyUnitException()))
                        } else {
                            emit(Resource.Success(unit))
                        }
                    } ?: run {
                        emit(Resource.Error.EmptyError(EmptyUnitException()))
                    }
                }
            }
        } catch (exception: Exception) { }
    }

    suspend fun getInfoByPhone(SIP: String): Flow<Resource<List<NameItem>>> = flow {
        emit(Resource.Loading())
        try {
            when (val resource = VoIPServiceApiImpl.getInfoByPhone(SIP)) {
                is Resource.Error.NetworkError<*> -> {
                    emit(Resource.Error.NetworkError(NetworkException()))
                }
                is Resource.Error.EmptyError<*> -> {
                    emit(Resource.Error.EmptyError(EmptyUnitException()))
                }
                is Resource.Error.UndefinedError<*> -> {
                    emit(Resource.Error.UndefinedError(UndefinedException()))
                }
                is Resource.Error.NotFoundError -> {
                    emit(Resource.Error.NotFoundError())
                }
                is Resource.Error.ServerNotRespondError -> emit(Resource.Error.ServerNotRespondError())
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success<*> -> {
                    resource.data?.let { lst ->
                        if (lst.isEmpty()) {
                            emit(Resource.Error.EmptyError(EmptyUnitException()))
                        } else {
                            emit(Resource.Success(lst))
                        }
                    } ?: run {
                        emit(Resource.Error.EmptyError(EmptyUnitException()))
                    }
                }
            }
        } catch (exception: Exception) { }
    }

}