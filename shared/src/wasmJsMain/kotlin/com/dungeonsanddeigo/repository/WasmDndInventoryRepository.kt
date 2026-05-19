package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndInventoryItem
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndInventoryRepository : DndInventoryRepository {
    private fun key(characterId: Long) = "dnd_inventory_$characterId"
    private val counterKey = "dnd_inventory_counter"

    private fun nextId(): Long {
        val id = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, id.toString())
        return id
    }

    private fun getAll(characterId: Long): List<DndInventoryItem> {
        val json = localStorage.getItem(key(characterId)) ?: return emptyList()
        return Json.decodeFromString<List<DndInventoryItem>>(json)
    }

    private fun saveAll(characterId: Long, items: List<DndInventoryItem>) {
        localStorage.setItem(key(characterId), Json.encodeToString(items))
    }

    override fun getByCategory(characterId: Long, category: String): List<DndInventoryItem> {
        return getAll(characterId).filter { it.category == category }
    }

    override fun save(item: DndInventoryItem): Long {
        val list = getAll(item.characterId).toMutableList()
        return if (item.id == 0L) {
            val id = nextId()
            list.add(item.copy(id = id))
            saveAll(item.characterId, list)
            id
        } else {
            val index = list.indexOfFirst { it.id == item.id }
            if (index >= 0) list[index] = item else list.add(item)
            saveAll(item.characterId, list)
            item.id
        }
    }

    override fun delete(itemId: Long) {
        for (i in 0 until localStorage.length) {
            val k = localStorage.key(i) ?: continue
            if (!k.startsWith("dnd_inventory_") || k == "dnd_inventory_counter") continue
            val list = Json.decodeFromString<List<DndInventoryItem>>(localStorage.getItem(k) ?: continue)
            val filtered = list.filter { it.id != itemId }
            if (filtered.size != list.size) {
                localStorage.setItem(k, Json.encodeToString(filtered))
                return
            }
        }
    }
}
