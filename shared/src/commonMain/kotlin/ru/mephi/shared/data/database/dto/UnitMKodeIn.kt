package ru.mephi.shared.data.database.dto

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import ru.mephi.shared.data.model.UnitM

@Serializable
class UnitMKodeIn(
    override val id: String = "",
    val code_str: String = "",
    val name: String = "",
    val fullname: String = "",
    var shortname: String = "",
    var parent_code: String = "",
    var parent_name: String = "",
    val appointment_num: String = "",
    val child_num: String = "",
    val children: List<UnitMKodeIn> = emptyList(),
    val appointments: List<AppointmentKodeIn> = emptyList()
) : Metadata {
    constructor(
        code_str: String,
        name: String,
        fullname: String,
        shortname: String,
        parent_code: String,
        parent_name: String,
        appointment_num: String,
        child_num: String,
        children: List<UnitMKodeIn>,
        appointments: List<AppointmentKodeIn>
    ) : this(
        code_str, code_str, name, fullname, shortname, parent_code,
        parent_name, appointment_num, child_num, children, appointments
    )
}

val UnitM.toKodeIn: UnitMKodeIn
    get() {
        val children = this.children.map { it.toKodeIn }
        val appointments = this.appointments.map { it.toKodeIn }
        return UnitMKodeIn(
            this.code_str, this.name, this.fullname, this.shortname, this.parent_code,
            this.parent_name, this.appointment_num, this.child_num, children, appointments
        )
    }

val UnitMKodeIn.fromKodein: UnitM
    get() {
        val children = this.children.map { it.fromKodein }
        val appointments = this.appointments.map { it.fromKodein }
        return UnitM(
            this.code_str, this.name, this.fullname, this.shortname, this.parent_code,
            this.parent_name, this.appointment_num, this.child_num, children, appointments
        )
    }