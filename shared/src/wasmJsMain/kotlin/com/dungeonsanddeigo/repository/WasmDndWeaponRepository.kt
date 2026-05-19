package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndWeapon
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndWeaponRepository : DndWeaponRepository {
    private fun key(characterId: Long) = "dnd_weapon_$characterId"
    private val counterKey = "dnd_weapon_counter"

    private fun nextId(): Long {
        val id = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, id.toString())
        return id
    }

    private fun getAll(characterId: Long): List<DndWeapon> {
        val json = localStorage.getItem(key(characterId)) ?: return emptyList()
        return Json.decodeFromString<List<DndWeapon>>(json)
    }

    private fun saveAll(characterId: Long, items: List<DndWeapon>) {
        localStorage.setItem(key(characterId), Json.encodeToString(items))
    }

    override fun getByCharacterId(characterId: Long): List<DndWeapon> = getAll(characterId)

    override fun save(weapon: DndWeapon): Long {
        val list = getAll(weapon.characterId).toMutableList()
        return if (weapon.id == 0L) {
            val id = nextId()
            list.add(weapon.copy(id = id))
            saveAll(weapon.characterId, list)
            id
        } else {
            val index = list.indexOfFirst { it.id == weapon.id }
            if (index >= 0) list[index] = weapon else list.add(weapon)
            saveAll(weapon.characterId, list)
            weapon.id
        }
    }

    override fun delete(weaponId: Long) {
        for (i in 0 until localStorage.length) {
            val k = localStorage.key(i) ?: continue
            if (!k.startsWith("dnd_weapon_") || k == counterKey) continue
            val list = Json.decodeFromString<List<DndWeapon>>(localStorage.getItem(k) ?: continue)
            val filtered = list.filter { it.id != weaponId }
            if (filtered.size != list.size) {
                localStorage.setItem(k, Json.encodeToString(filtered))
                return
            }
        }
    }
}
