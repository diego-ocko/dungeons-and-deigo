package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndSkills
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndSkillsRepository : DndSkillsRepository {
    private fun key(characterId: Long) = "dnd_skills_$characterId"

    override fun getByCharacterId(characterId: Long): DndSkills? {
        val json = localStorage.getItem(key(characterId)) ?: return null
        return Json.decodeFromString<DndSkills>(json)
    }

    override fun save(skills: DndSkills) {
        localStorage.setItem(key(skills.characterId), Json.encodeToString(skills))
    }
}
