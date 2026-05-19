package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndMainInfo
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndMainInfoRepository : DndMainInfoRepository {
    private fun key(characterId: Long) = "dnd_main_info_$characterId"

    override fun getByCharacterId(characterId: Long): DndMainInfo? {
        val json = localStorage.getItem(key(characterId)) ?: return null
        return Json.decodeFromString<DndMainInfo>(json)
    }

    override fun save(info: DndMainInfo) {
        localStorage.setItem(key(info.characterId), Json.encodeToString(info))
    }
}
