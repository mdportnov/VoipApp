package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable
import ru.mephi.shared.vm.SearchType

@Serializable
data class SearchRecord(
    val id: Long? = null,
    val searchStr: String,
    val searchType: SearchType
)