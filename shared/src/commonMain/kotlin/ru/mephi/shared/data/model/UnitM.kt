package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnitM(
    var code_str: String = "",
    val name: String = "",
    val fullname: String = "",
    var shortname: String = "",
    var parent_code: String = "",
    var parent_name: String = "",
    val appointment_num: String = "",
    val child_num: String = "",
    val children: List<UnitM> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    var scrollPosition: Int = 0
) : CatalogItem()