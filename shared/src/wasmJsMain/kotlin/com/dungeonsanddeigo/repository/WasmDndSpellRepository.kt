package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndSpell
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndSpellRepository {
    private fun key(characterId: Long) = "dnd_spells_$characterId"
    private val counterKey = "dnd_spells_counter"
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private fun nextId(): Long {
        val id = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, id.toString())
        return id
    }

    fun getByCharacterId(characterId: Long): List<DndSpell> {
        val jsonStr = localStorage.getItem(key(characterId)) ?: return emptyList()
        return try { json.decodeFromString<List<DndSpell>>(jsonStr) } catch (_: Exception) { emptyList() }
    }

    fun save(spell: DndSpell): Long {
        val list = getByCharacterId(spell.characterId).toMutableList()
        return if (spell.id == 0L) {
            val id = nextId()
            list.add(spell.copy(id = id))
            localStorage.setItem(key(spell.characterId), json.encodeToString(list))
            id
        } else {
            val index = list.indexOfFirst { it.id == spell.id }
            if (index >= 0) list[index] = spell else list.add(spell)
            localStorage.setItem(key(spell.characterId), json.encodeToString(list))
            spell.id
        }
    }

    fun delete(characterId: Long, spellId: Long) {
        val list = getByCharacterId(characterId).filter { it.id != spellId }
        localStorage.setItem(key(characterId), Json.encodeToString(list))
    }
}
