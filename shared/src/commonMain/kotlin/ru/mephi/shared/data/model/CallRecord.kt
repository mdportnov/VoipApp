package ru.mephi.shared.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class CallRecord(
    val id: Long? = null,
    val sipNumber: String,
    val sipName: String?,
    val status: CallStatus,
    val time: Long = Clock.System.now().toEpochMilliseconds()
)
