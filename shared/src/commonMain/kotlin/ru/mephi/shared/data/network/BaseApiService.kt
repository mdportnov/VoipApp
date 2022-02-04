package ru.mephi.shared.data.network

import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.UnitM
import kotlin.coroutines.cancellation.CancellationException

interface BaseApiService {
    // При щелчке по юниту из списка и стартовой загрузке
    // get_units_mobile.json?api_key=ВАШКЛЮЧ&filter_code_str=01 536 00
//    @Throws(NetworkException::class, EmptyUnitException::class, CancellationException::class)
    suspend fun getUnitByCodeStr(codeStr: String): Resource<List<UnitM>?>

    // get_subscribers_mobile.json?api_key=ВАШКЛЮЧ&filter_lastname=LIKE|%Трут%
    suspend fun getUsersByName(filterLike: String): UnitM

    // get_units_mobile.json?api_key=ВАШКЛЮЧ&filter_fullname=LIKE|%информ%
    suspend fun getUnitsByName(filterLike: String): List<UnitM>

    // get_displayname.json?api_key=КЛЮЧ&line=9295
    suspend fun getInfoByPhone(phone: String): NameItem?
}