package ru.mephi.shared.data.database

import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels
import ru.mephi.shared.data.database.dto.AppointmentKodeIn
import ru.mephi.shared.data.database.dto.UnitMKodeIn
import ru.mephi.shared.data.database.dto.fromKodein
import ru.mephi.shared.data.database.dto.toKodeIn
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.LoggerViewModel

class CatalogDao(
    private val lVM: LoggerViewModel,
    private val kodeinDB: DB
) {

    fun addUnit(unitM: UnitM) {
        kodeinDB.put(kodeinDB.keyFrom(unitM), unitM.toKodeIn)
    }

    fun getUnitByCodeStr(codeStr: String): UnitM {
        kodeinDB.find<UnitMKodeIn>().byId(codeStr).useModels { it.toList() }.map { it.fromKodein }.let {
            if (it.isEmpty()) {
                lVM.e("Failed to found unit by codeStr! Use checkByCodeStr() function to check the existence!")
                return UnitM()
            } else {
                return it[0]
            }
        }
    }

    fun isUnitExistsByCodeStr(code_str: String): Boolean {
        return kodeinDB.find<UnitMKodeIn>().byId(code_str).isValid()
    }

    fun getUserBySIP(SIP: String): Appointment {
        kodeinDB.find<AppointmentKodeIn>().byIndex("line", SIP).useModels { it.toList() }.map { it.fromKodein }.let {
            if (it.isEmpty()) {
                lVM.e("Failed to found user by SIP! Use isUserExistsBySIP() function to check the existence!")
                return Appointment()
            } else {
                return it[0]
            }
        }
    }

    fun addUser(appointment: Appointment) {
        kodeinDB.put(kodeinDB.keyFrom(appointment), appointment.toKodeIn)
    }

    fun isUserExistsBySIP(SIP: String): Boolean {
        return kodeinDB.find<AppointmentKodeIn>().byIndex("line", SIP).isValid()
    }

    fun deleteAll() {
        kodeinDB.deleteAll(kodeinDB.find<UnitMKodeIn>().all())
    }

}