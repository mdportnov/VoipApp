package ru.mephi.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PositionInfo(
    @SerialName("appointment_name")
    val appointmentName: String,
    @SerialName("unit_code_str")
    val unitCodeStr: String,
    @SerialName("unit_name")
    val unitName: String,
    @SerialName("unit_fullname")
    val unitFullName: String,
    @SerialName("unit_shortname")
    val unitShortname: String,
    @SerialName("room")
    val room: String = ""
)
