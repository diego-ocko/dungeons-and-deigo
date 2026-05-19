package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndSkills

interface DndSkillsRepository {
    fun getByCharacterId(characterId: Long): DndSkills?
    fun save(skills: DndSkills)
}
