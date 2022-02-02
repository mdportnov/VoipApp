package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(val login: String, val password: String, var isActive: Boolean)