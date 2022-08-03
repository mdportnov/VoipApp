package ru.mephi.shared.data.network

import io.ktor.client.request.*
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.exception.EmptyUnitException
import ru.mephi.shared.data.network.exception.ForbiddenException
import ru.mephi.shared.data.network.exception.NetworkException
import ru.mephi.shared.data.network.exception.NotFoundException

object VoIPServiceApiImpl : VoIPServiceApi {

    override suspend fun getUnitByCodeStr(codeStr: String): Resource<UnitM> {
        try {
            val units: List<UnitM> =
                VoIPServiceClient.get {
                    url {
                        path("get_units_mobile_catalog.json")
                        parameter("filter_code_str", codeStr)
                    }
                }
            if (units.isEmpty())
                return Resource.Error.EmptyError(exception = EmptyUnitException())
            return Resource.Success(data = units[0])
        } catch (exception: Throwable) {
            return when (exception) {
                is NetworkException -> Resource.Error.NetworkError(exception = exception)
                is NotFoundException -> Resource.Error.NotFoundError(exception = exception)
                is ForbiddenException -> Resource.Error.ServerNotRespondError(exception = exception)
                is EmptyUnitException -> Resource.Error.EmptyError(exception = exception)
                else -> Resource.Error.UndefinedError()
            }
        }
    }

    override suspend fun getUsersByName(filterLike: String): Resource<UnitM> {
        try {
            val unitOfUsers: UnitM = VoIPServiceClient.get {
                url {
                    path("get_subscribers_mobile.json")
                    parameter("filter_lastname", "LIKE|%$filterLike%")
                }
            } ?: return Resource.Error.NetworkError(exception = NotFoundException(filterLike))
            if (unitOfUsers.appointments.isEmpty()) {
                return Resource.Error.EmptyError(exception = NotFoundException(filterLike))
            }
            return Resource.Success(data = unitOfUsers)
        } catch (exception: Throwable) {
            return when (exception) {
                is NetworkException -> Resource.Error.NetworkError(exception = exception)
                is NotFoundException -> Resource.Error.NotFoundError(exception = exception)
                is ForbiddenException -> Resource.Error.ServerNotRespondError(exception = exception)
                is EmptyUnitException -> Resource.Error.EmptyError(exception = exception)
                else -> Resource.Error.UndefinedError()
            }
        }
    }

    override suspend fun getUnitsByName(filterLike: String): Resource<List<UnitM>> {
        try {
            val units: List<UnitM> = VoIPServiceClient.get {
                url {
                    path("get_units_mobile_find.json")
                    parameter("filter_fullname", "LIKE|%$filterLike%")
                }
            } ?: return Resource.Error.NetworkError(exception = NetworkException())

            if (units.isEmpty())
                return Resource.Error.NotFoundError(exception = NotFoundException(filterLike))

            return Resource.Success(data = units)
        } catch (exception: Throwable) {
            return when (exception) {
                is NetworkException -> Resource.Error.NetworkError(exception = exception)
                is NotFoundException -> Resource.Error.NotFoundError(exception = exception)
                is ForbiddenException -> Resource.Error.ServerNotRespondError(exception = exception)
                is EmptyUnitException -> Resource.Error.EmptyError(exception = exception)
                else -> Resource.Error.UndefinedError()
            }
        }
    }

    override suspend fun getInfoByPhone(phone: String): Resource<List<NameItem>> {
        try {
            val nameItems: List<NameItem> = VoIPServiceClient.get {
                url {
                    path("get_displayname.json")
                    parameter("line", phone)
                }
            } ?: return Resource.Error.NetworkError(exception = NetworkException())

            if (nameItems.isEmpty())
                return Resource.Error.NotFoundError(exception = NotFoundException(query = phone))
            return Resource.Success(data = nameItems)
        } catch (exception: Throwable) {
            return when (exception) {
                is NetworkException -> Resource.Error.NetworkError(exception = exception)
                is NotFoundException -> Resource.Error.NotFoundError(exception = exception)
                is ForbiddenException -> Resource.Error.ServerNotRespondError(exception = exception)
                is EmptyUnitException -> Resource.Error.EmptyError(exception = exception)
                else -> Resource.Error.UndefinedError()
            }
        }
    }

    override suspend fun getUserByPhone(phone: String): Resource<UnitM> {
        try {
            val userUnit: UnitM = VoIPServiceClient.get {
                url {
                    path("get_subscribers_mobile_by_phone.json")
                    parameter("filter_extension", phone)
                }
            } ?: return Resource.Error.NetworkError(exception = NetworkException())
            if (userUnit.appointments.isEmpty())
                return Resource.Error.NotFoundError(exception = NotFoundException(query = phone))
            return Resource.Success(data = userUnit)
        } catch (exception: Throwable) {
            return when (exception) {
                is NetworkException -> Resource.Error.NetworkError(exception = exception)
                is NotFoundException -> Resource.Error.NotFoundError(exception = exception)
                is ForbiddenException -> Resource.Error.ServerNotRespondError(exception = exception)
                is EmptyUnitException -> Resource.Error.EmptyError(exception = exception)
                else -> Resource.Error.UndefinedError()
            }
        }
    }
}









