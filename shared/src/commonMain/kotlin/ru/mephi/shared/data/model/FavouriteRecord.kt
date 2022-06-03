package ru.mephi.shared.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class FavouriteRecord(
    val id: Long? = null,
    val sipName: String,
    val sipNumber: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)