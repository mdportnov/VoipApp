package ru.mephi.shared.data.database.dto

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import ru.mephi.shared.data.model.UnitM

@Serializable
class UnitMKodeIn(
    override val id: String,
    val code_str: String,
    val name: String,
    val fullname: String,
    var shortname: String,
    var parent_code: String? = null,
    var parent_name: String? = null,
    val children: List<UnitMKodeIn>? = null,
    val appointments: List<AppointmentKodein>? = null
) : Metadata {
    constructor(
        code_str: String,
        name: String,
        fullname: String,
        shortname: String,
        parent_code: String? = null,
        parent_name: String? = null,
        children: List<UnitMKodeIn>? = null,
        appointments: List<AppointmentKodein>? = null
    ) : this(
        code_str, code_str, name, fullname, shortname,
        parent_code, parent_name, children, appointments
    )
}

val UnitM.toKodeIn: UnitMKodeIn
    get() {
        val children = this.children?.map { it.toKodeIn }
        val appointments = this.appointments?.map { it.toKodeIn }
        return UnitMKodeIn(
            this.code_str, this.name, this.fullname, this.shortname,
            this.parent_code, this.parent_name, children, appointments
        )
    }

val UnitMKodeIn.fromKodein: UnitM
    get() {
        val children = this.children?.map { it.fromKodein }
        val appointments = this.appointments?.map { it.fromKodein }
        return UnitM(
            this.code_str, this.name, this.fullname, this.shortname,
            this.parent_code, this.parent_name, children, appointments
        )
    }