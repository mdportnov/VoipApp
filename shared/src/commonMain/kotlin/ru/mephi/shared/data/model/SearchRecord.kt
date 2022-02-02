package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
class SearchRecord(
    val id: Long?,
    val name: String,
    val type: SearchType
)