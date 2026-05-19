package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndFeature
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndFeaturesRepository : DndFeaturesRepository {
    private fun key(characterId: Long) = "dnd_features_$characterId"
    private val counterKey = "dnd_features_counter"
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private fun nextId(): Long {
        val id = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, id.toString())
        return id
    }

    override fun getByCharacterId(characterId: Long): List<DndFeature> {
        val jsonStr = localStorage.getItem(key(characterId)) ?: return emptyList()
        return try { json.decodeFromString<List<DndFeature>>(jsonStr) } catch (_: Exception) { emptyList() }
    }

    override fun save(feature: DndFeature): Long {
        val list = getByCharacterId(feature.characterId).toMutableList()
        val featureId: Long
        if (feature.id == 0L) {
            featureId = nextId()
            list.add(feature.copy(id = featureId))
        } else {
            featureId = feature.id
            val index = list.indexOfFirst { it.id == featureId }
            if (index >= 0) list[index] = feature else list.add(feature)
        }
        localStorage.setItem(key(feature.characterId), json.encodeToString(list))
        return featureId
    }

    override fun delete(featureId: Long) {
        // Need to scan all character keys — but we can optimize by requiring characterId
        // For now, iterate known keys
        for (i in 0 until localStorage.length) {
            val k = localStorage.key(i) ?: continue
            if (!k.startsWith("dnd_features_") || k == "dnd_features_counter") continue
            val list = json.decodeFromString<List<DndFeature>>(localStorage.getItem(k) ?: continue)
            val filtered = list.filter { it.id != featureId }
            if (filtered.size != list.size) {
                localStorage.setItem(k, json.encodeToString(filtered))
                return
            }
        }
    }
}
