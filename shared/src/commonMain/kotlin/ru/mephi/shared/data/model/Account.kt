package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val login: String = "",
    val password: String = "",
    val displayedName: String = "",
    var isActive: Boolean = false
)