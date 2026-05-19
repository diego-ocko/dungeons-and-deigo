package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndWeapon

interface DndWeaponRepository {
    fun getByCharacterId(characterId: Long): List<DndWeapon>
    fun save(weapon: DndWeapon): Long
    fun delete(weaponId: Long)
}
