package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndConsumable

interface DndConsumableRepository {
    fun getByCharacterId(characterId: Long): List<DndConsumable>
    fun save(item: DndConsumable): Long
    fun delete(itemId: Long)
}
