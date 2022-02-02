package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NameItem(
    val display_name: String,
    val display_name_latin: String,
    val appointment: String?,
    val code_str: String,
    val name: String,
    val shortname: String
)
