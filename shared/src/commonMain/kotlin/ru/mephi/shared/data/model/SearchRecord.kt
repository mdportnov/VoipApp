package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchRecord(
    val id: Long? = null,
    val name: String,
    val type: SearchType
)