package ru.mephi.shared.data.database.dto

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import ru.mephi.shared.data.model.Appointment

@Serializable
class AppointmentKodein(
    override val id: String,
    val appointment_id: String,
    val subscriber_id: String,
    val EmpGUID: String? = null,
    val appointment: String? = null,
    val lastname: String,
    val firstname: String,
    val patronymic: String? = null,
    val fullname: String,
    val fio: String,
    val line: String? = null,
    val lineShown: String? = null,
    val email: String? = null,
    val room: String? = null
) : Metadata {
    constructor(
        appointment_id: String,
        subscriber_id: String,
        EmpGUID: String? = null,
        appointment: String? = null,
        lastname: String,
        firstname: String,
        patronymic: String? = null,
        fullname: String,
        fio: String,
        line: String? = null,
        lineShown: String? = null,
        email: String? = null,
        room: String? = null,
    ) : this(
        subscriber_id, appointment_id, subscriber_id, EmpGUID, appointment, lastname, firstname,
        patronymic, fullname, fio, line, lineShown, email, room
    )
}

val Appointment.toKodeIn: AppointmentKodein
    get() {
        return AppointmentKodein(
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

val AppointmentKodein.fromKodein: Appointment
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