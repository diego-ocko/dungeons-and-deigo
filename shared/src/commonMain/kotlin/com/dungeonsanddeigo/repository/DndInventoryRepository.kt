package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndInventoryItem

interface DndInventoryRepository {
    fun getByCategory(characterId: Long, category: String): List<DndInventoryItem>
    fun save(item: DndInventoryItem): Long
    fun delete(itemId: Long)
}
