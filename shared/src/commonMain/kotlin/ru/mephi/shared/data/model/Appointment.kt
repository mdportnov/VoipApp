package ru.mephi.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    @SerialName("appointment_id")
    val appointmentId: String = "",
    @SerialName("subscriber_id")
    val subscriberId: String = "",
    val EmpGUID: String = "",
    val appointment: String = "",
    val lastname: String = "",
    val firstname: String = "",
    val patronymic: String = "",
    @SerialName("fullname")
    val fullName: String = "",
    val fio: String = "",
    val line: String = "",
    @SerialName("line_shown")
    val lineShown: String = "",
    val email: String = "",
    val room: String = "",
    @SerialName("appointments")
    val positions: List<PositionInfo> = emptyList()
) : CatalogItem()
