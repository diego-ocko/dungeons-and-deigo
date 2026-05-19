package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndMainInfo

interface DndMainInfoRepository {
    fun getByCharacterId(characterId: Long): DndMainInfo?
    fun save(info: DndMainInfo)
}
