package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndArmor

interface DndArmorRepository {
    fun getByCharacterId(characterId: Long): List<DndArmor>
    fun save(armor: DndArmor): Long
    fun delete(armorId: Long)
}
