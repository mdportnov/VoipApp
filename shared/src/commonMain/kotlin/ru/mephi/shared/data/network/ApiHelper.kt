package ru.mephi.shared.data.network

class ApiHelper {
    private val apiService: KtorApiService = KtorApiService()
    suspend fun getInfoByPhone(num: String) = apiService.getInfoByPhone(num)
    suspend fun getUnitsByName(name: String) = apiService.getUnitsByName(name)
    suspend fun getUsersByName(name: String) = apiService.getUsersByName(name)
    suspend fun getUnitByCodeStr(codeStr: String) = apiService.getUnitByCodeStr(codeStr)
}