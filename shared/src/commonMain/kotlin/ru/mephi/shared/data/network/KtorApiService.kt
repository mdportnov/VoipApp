package ru.mephi.shared.data.network

import io.ktor.client.request.*
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.UnitM
import kotlin.coroutines.cancellation.CancellationException

class KtorApiService : BaseApiService {
    private var httpClient = KtorClientBuilder.createHttpClient()

    override suspend fun getUnitByCodeStr(codeStr: String): Resource<List<UnitM>?> {
        try {
            val units: List<UnitM> = httpClient.get {
                url {
                    path("get_units_mobile_catalog.json")
                    parameter("filter_code_str", codeStr)
                }
            } ?: return Resource.Error.NetworkError(exception = NetworkException())

            if (units.isEmpty())
                return Resource.Error.EmptyError(exception = EmptyUnitException())
            return Resource.Success(data = units)
        } catch (e: Throwable) {
            return Resource.Error.NetworkError(exception = NetworkException())
        }
    }

    override suspend fun getUsersByName(filterLike: String): UnitM {
        return httpClient.get {
            url {
                path("get_subscribers_mobile.json")
                parameter("filter_lastname", "LIKE|%$filterLike%")
            }
        }
    }

    override suspend fun getUnitsByName(filterLike: String): List<UnitM> {
        return httpClient.get {
            url {
                path("get_units_mobile_find.json")
                parameter("filter_fullname", "LIKE|%$filterLike%")
            }
        }
    }

    override suspend fun getInfoByPhone(phone: String): NameItem? {
        return try {
            httpClient.get<List<NameItem>> {
                url {
                    path("get_displayname.json")
                    parameter("line", phone)
                }
            }[0]
        } catch (e: Exception) {
            null
        }
    }
}

class EmptyUnitException : Exception() {
    override val message: String
        get() = "Пустой пункт"
}

class NetworkException : Exception() {
    override val message: String
        get() = "Интернет-соединение отсутствует"
}

class NotFoundException(val query: String) : Exception() {
    override val message: String
        get() = "По запросу \"${query}\" ничего не найдено"
}