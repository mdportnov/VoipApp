package ru.mephi.shared.data.network

import io.ktor.client.request.*
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.UnitM

class KtorApiService : BaseApiService {
    private var httpClient = KtorClientBuilder.createHttpClient()
    override suspend fun getUnitByCodeStr(codeStr: String): List<UnitM> {
        val units: List<UnitM>? = httpClient.get {
            url {
                path("get_units_mobile_catalog.json")
                parameter("filter_code_str", codeStr)
            }
        }

        if (units.isNullOrEmpty())
            throw EmptyUnitException()
        else
            return units
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

class EmptyUnitException : Throwable() {
    override val message: String
        get() = "Empty unit"
}