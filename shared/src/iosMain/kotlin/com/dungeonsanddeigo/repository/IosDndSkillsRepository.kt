package com.dungeonsanddeigo.repository

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndSkills
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IosDndSkillsRepository : DndSkillsRepository {
    private val driver = NativeSqliteDriver(AppDatabase.Schema, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndSkillsQueries

    override fun getByCharacterId(characterId: Long): DndSkills? {
        val result = queries.selectByCharacterId(characterId).executeAsOneOrNull() ?: return null
        return Json.decodeFromString<DndSkills>(result.skillsJson).copy(characterId = characterId)
    }

    override fun save(skills: DndSkills) {
        val json = Json.encodeToString(skills)
        val existing = queries.selectByCharacterId(skills.characterId).executeAsOneOrNull()
        if (existing != null) {
            queries.update(json, skills.characterId)
        } else {
            queries.insert(skills.characterId, json)
        }
    }
}
