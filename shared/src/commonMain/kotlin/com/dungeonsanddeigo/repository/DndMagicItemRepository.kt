package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndMagicItem

interface DndMagicItemRepository {
    fun getByCharacterId(characterId: Long): List<DndMagicItem>
    fun save(item: DndMagicItem): Long
    fun delete(itemId: Long)
}
