package ru.mephi.shared.data.database.dto

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import ru.mephi.shared.data.model.Appointment

@Serializable
class AppointmentKodeIn(
    override val id: String = "",
    val appointment_id: String = "",
    val subscriber_id: String = "",
    val EmpGUID: String = "",
    val appointment: String = "",
    val lastname: String = "",
    val firstname: String = "",
    val patronymic: String = "",
    val fullname: String = "",
    val fio: String = "",
    val line: String = "",
    val lineShown: String = "",
    val email: String = "",
    val room: String = ""
) : Metadata {
    constructor(
        appointment_id: String,
        subscriber_id: String,
        EmpGUID: String,
        appointment: String,
        lastname: String,
        firstname: String,
        patronymic: String,
        fullname: String,
        fio: String,
        line: String,
        lineShown: String,
        email: String,
        room: String,
    ) : this(
        subscriber_id, appointment_id, subscriber_id, EmpGUID, appointment, lastname, firstname,
        patronymic, fullname, fio, line, lineShown, email, room
    )
}

val Appointment.toKodeIn: AppointmentKodeIn
    get() {
        return AppointmentKodeIn(
            this.appointmentId,
            this.subscriberId,
            this.EmpGUID,
            this.appointment,
            this.lastname,
            this.firstname,
            this.patronymic,
            this.fullName,
            this.fio,
            this.line,
            this.lineShown,
            this.email,
            this.room,
        )
    }

val AppointmentKodeIn.fromKodein: Appointment
    get() {
        return Appointment(
            this.appointment_id,
            this.subscriber_id,
            this.EmpGUID,
            this.appointment,
            this.lastname,
            this.firstname,
            this.patronymic,
            this.fullname,
            this.fio,
            this.line,
            this.lineShown,
            this.email,
            this.room,
        )
    }