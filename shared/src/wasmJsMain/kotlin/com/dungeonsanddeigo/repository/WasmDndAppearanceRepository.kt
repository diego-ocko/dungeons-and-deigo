package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndAppearance
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndAppearanceRepository {
    private fun key(characterId: Long) = "dnd_appearance_$characterId"

    fun getByCharacterId(characterId: Long): DndAppearance? {
        val json = localStorage.getItem(key(characterId)) ?: return null
        return Json.decodeFromString<DndAppearance>(json)
    }

    fun save(appearance: DndAppearance) {
        localStorage.setItem(key(appearance.characterId), Json.encodeToString(appearance))
    }
}
