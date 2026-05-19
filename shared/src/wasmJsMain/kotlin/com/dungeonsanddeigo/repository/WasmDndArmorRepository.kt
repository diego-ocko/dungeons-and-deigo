package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndArmor
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndArmorRepository : DndArmorRepository {
    private fun key(characterId: Long) = "dnd_armor_$characterId"
    private val counterKey = "dnd_armor_counter"

    private fun nextId(): Long {
        val id = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, id.toString())
        return id
    }

    override fun getByCharacterId(characterId: Long): List<DndArmor> {
        val json = localStorage.getItem(key(characterId)) ?: return emptyList()
        return Json.decodeFromString<List<DndArmor>>(json)
    }

    override fun save(armor: DndArmor): Long {
        val list = getByCharacterId(armor.characterId).toMutableList()
        return if (armor.id == 0L) {
            val id = nextId()
            list.add(armor.copy(id = id))
            localStorage.setItem(key(armor.characterId), Json.encodeToString(list))
            id
        } else {
            val index = list.indexOfFirst { it.id == armor.id }
            if (index >= 0) list[index] = armor else list.add(armor)
            localStorage.setItem(key(armor.characterId), Json.encodeToString(list))
            armor.id
        }
    }

    override fun delete(armorId: Long) {
        for (i in 0 until localStorage.length) {
            val k = localStorage.key(i) ?: continue
            if (!k.startsWith("dnd_armor_") || k == "dnd_armor_counter") continue
            val list = Json.decodeFromString<List<DndArmor>>(localStorage.getItem(k) ?: continue)
            val filtered = list.filter { it.id != armorId }
            if (filtered.size != list.size) {
                localStorage.setItem(k, Json.encodeToString(filtered))
                return
            }
        }
    }
}
