package ru.mephi.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavouriteRecord(
    val id: Long? = null,
    val sipName: String,
    val sipNumber: String,
    val created_at: Long
)