package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndFeature

interface DndFeaturesRepository {
    fun getByCharacterId(characterId: Long): List<DndFeature>
    fun save(feature: DndFeature): Long
    fun delete(featureId: Long)
}
