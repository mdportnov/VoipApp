package ru.mephi.shared.data.network

import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.model.UnitM

interface BaseApiService {
    // При щелчке по юниту из списка и стартовой загрузке
    // get_units_mobile.json?api_key=ВАШКЛЮЧ&filter_code_str=01 536 00
    suspend fun getUnitByCodeStr(codeStr: String): Resource<UnitM>

    // get_subscribers_mobile.json?api_key=ВАШКЛЮЧ&filter_lastname=LIKE|%Трут%
    suspend fun getUsersByName(filterLike: String): Resource<UnitM>

    // get_units_mobile.json?api_key=ВАШКЛЮЧ&filter_fullname=LIKE|%информ%
    suspend fun getUnitsByName(filterLike: String): Resource<List<UnitM>>

    // get_displayname.json?api_key=КЛЮЧ&line=9295
    suspend fun getInfoByPhone(phone: String): Resource<List<NameItem>>

    // get_subscribers_mobile_by_phone.json?api_key=КЛЮЧ&filter_extension=9295
    suspend fun getUserByPhone(phone: String): Resource<UnitM>
}