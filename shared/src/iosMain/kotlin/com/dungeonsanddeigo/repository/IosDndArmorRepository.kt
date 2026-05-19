package com.dungeonsanddeigo.repository

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndArmor

class IosDndArmorRepository : DndArmorRepository {
    private val driver = NativeSqliteDriver(AppDatabase.Schema, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndArmorQueries

    override fun getByCharacterId(characterId: Long): List<DndArmor> {
        return queries.selectByCharacterId(characterId).executeAsList().map {
            DndArmor(it.id, it.characterId, it.name, it.type, it.baseAC.toInt(), it.acModifier,
                it.minimumStrength.toInt(), it.hasSneakDisadvantage != 0L, it.weight,
                it.price.toInt(), it.priceCurrency, it.isEquipped != 0L,
                it.tags.split(",").filter { t -> t.isNotEmpty() }, it.additionalFeatures)
        }
    }

    override fun save(armor: DndArmor): Long {
        fun b(v: Boolean) = if (v) 1L else 0L
        return if (armor.id == 0L) {
            queries.insert(armor.characterId, armor.name, armor.type, armor.baseAC.toLong(), armor.acModifier,
                armor.minimumStrength.toLong(), b(armor.hasSneakDisadvantage), armor.weight,
                armor.price.toLong(), armor.priceCurrency, b(armor.isEquipped),
                armor.tags.joinToString(","), armor.additionalFeatures)
            queries.lastInsertId().executeAsOne()
        } else {
            queries.update(armor.name, armor.type, armor.baseAC.toLong(), armor.acModifier,
                armor.minimumStrength.toLong(), b(armor.hasSneakDisadvantage), armor.weight,
                armor.price.toLong(), armor.priceCurrency, b(armor.isEquipped),
                armor.tags.joinToString(","), armor.additionalFeatures, armor.id)
            armor.id
        }
    }

    override fun delete(armorId: Long) { queries.deleteById(armorId) }
}
