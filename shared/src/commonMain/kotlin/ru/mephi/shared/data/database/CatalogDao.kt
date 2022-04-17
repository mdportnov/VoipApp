package ru.mephi.shared.data.database

import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.database.dto.UnitMKodeIn
import ru.mephi.shared.data.database.dto.fromKodein
import ru.mephi.shared.data.model.UnitM

class CatalogDao : KoinComponent {
    private val kodeinDB: DB by inject()
    fun checkByCodeStr(code_str: String) =
        kodeinDB.find<UnitMKodeIn>().byId(code_str).isValid()

    fun add(unitM: UnitMKodeIn) {
        kodeinDB.put(kodeinDB.keyFrom(unitM), unitM)
    }

    fun deleteByCodeStr(code_str: String) {
        kodeinDB.deleteAll(kodeinDB.find<UnitMKodeIn>().byIndex("code_str", code_str))
    }

    fun deleteAll() {
        kodeinDB.deleteAll(kodeinDB.find<UnitMKodeIn>().all())
    }

    fun getAll(): List<Unit> {
        return kodeinDB.find<UnitMKodeIn>().all().useModels { it.toList() }.map { it.fromKodein }
    }

    fun getUnitByCodeStr(code_str: String): UnitM? {
        return try {
            kodeinDB.find<UnitMKodeIn>().byId(code_str).model().fromKodein
        } catch (e: Exception) {
            return null
        }
    }
}