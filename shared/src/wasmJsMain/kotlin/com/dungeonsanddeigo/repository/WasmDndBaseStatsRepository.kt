package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndBaseStats
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndBaseStatsRepository : DndBaseStatsRepository {
    private fun key(characterId: Long) = "dnd_base_stats_$characterId"

    override fun getByCharacterId(characterId: Long): DndBaseStats? {
        val json = localStorage.getItem(key(characterId)) ?: return null
        return Json.decodeFromString<DndBaseStats>(json)
    }

    override fun save(stats: DndBaseStats) {
        localStorage.setItem(key(stats.characterId), Json.encodeToString(stats))
    }
}
