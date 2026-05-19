package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndBaseStats

interface DndBaseStatsRepository {
    fun getByCharacterId(characterId: Long): DndBaseStats?
    fun save(stats: DndBaseStats)
}
