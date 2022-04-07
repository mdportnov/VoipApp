package ru.mephi.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    @SerialName("appointment_id")
    val appointmentId: String,
    @SerialName("subscriber_id")
    val subscriberId: String,
    val EmpGUID: String? = null,
    val appointment: String? = null,
    val lastname: String,
    val firstname: String,
    val patronymic: String? = null,
    @SerialName("fullname")
    val fullName: String,
    val fio: String,
    val line: String? = null,
    @SerialName("line_shown")
    val lineShown: String? = null,
    val email: String? = null,
    val room: String? = null,
    @SerialName("appointments")
    val positions: List<PositionInfo>? = null
) : CatalogItem()