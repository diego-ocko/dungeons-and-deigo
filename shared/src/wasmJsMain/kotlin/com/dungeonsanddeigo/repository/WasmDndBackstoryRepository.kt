package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndBackstory
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndBackstoryRepository {
    private fun key(characterId: Long) = "dnd_backstory_$characterId"

    fun getByCharacterId(characterId: Long): DndBackstory? {
        val json = localStorage.getItem(key(characterId)) ?: return null
        return Json.decodeFromString<DndBackstory>(json)
    }

    fun save(backstory: DndBackstory) {
        localStorage.setItem(key(backstory.characterId), Json.encodeToString(backstory))
    }
}
