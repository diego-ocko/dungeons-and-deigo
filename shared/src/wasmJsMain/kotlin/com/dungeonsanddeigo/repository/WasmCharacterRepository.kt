package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.Character
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmCharacterRepository : CharacterRepository {
    private val key = "characters"
    private val counterKey = "characters_counter"

    override fun getAll(): List<Character> {
        val json = localStorage.getItem(key) ?: return emptyList()
        return Json.decodeFromString<List<Character>>(json)
    }

    override fun insert(character: Character): Long {
        val counter = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, counter.toString())
        val charWithId = character.copy(id = counter)
        val list = getAll().toMutableList()
        list.add(charWithId)
        localStorage.setItem(key, Json.encodeToString(list))
        return counter
    }

    fun delete(characterId: Long) {
        val list = getAll().filter { it.id != characterId }
        localStorage.setItem(key, Json.encodeToString(list))

        // Remove all related data
        val keysToRemove = mutableListOf(
            "dnd_appearance_$characterId",
            "dnd_armor_$characterId",
            "dnd_backstory_$characterId",
            "dnd_base_stats_$characterId",
            "dnd_consumable_$characterId",
            "dnd_features_$characterId",
            "dnd_inventory_$characterId",
            "dnd_magic_item_$characterId",
            "dnd_main_info_$characterId",
            "dnd_money_$characterId",
            "dnd_notes_$characterId",
            "dnd_skills_$characterId",
            "dnd_spells_$characterId",
            "dnd_weapon_$characterId",
            "dnd_playing_life_$characterId",
            "dnd_playing_templife_$characterId",
            "dnd_playing_deathsave_$characterId",
            "dnd_playing_deathsuccess_$characterId",
            "dnd_playing_deathfail_$characterId",
            "dnd_playing_status_$characterId",
            "dnd_hitdice_main_$characterId",
            "dnd_hitdice_sec_$characterId",
            "char_image_$characterId",
            "char_name_$characterId"
        )
        keysToRemove.forEach { localStorage.removeItem(it) }

        // Remove spell slots and custom ability keys (pattern-based)
        val toRemove = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            val k = localStorage.key(i) ?: continue
            if (k.contains("_${characterId}_") || k.endsWith("_$characterId")) {
                toRemove.add(k)
            }
        }
        toRemove.forEach { localStorage.removeItem(it) }
    }
}
