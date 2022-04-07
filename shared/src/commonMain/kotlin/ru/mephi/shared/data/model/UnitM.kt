package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UnitM(
    var code_str: String,
    val name: String,
    val fullname: String,
    var shortname: String,
    var parent_code: String? = null,
    var parent_name: String? = null,
    val children: List<UnitM>? = null,
    val appointments: List<Appointment>? = null,
    var scrollPosition: Int? = null
) : CatalogItem()