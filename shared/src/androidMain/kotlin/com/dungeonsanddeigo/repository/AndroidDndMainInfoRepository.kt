package com.dungeonsanddeigo.repository

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndMainInfo

class AndroidDndMainInfoRepository(context: Context) : DndMainInfoRepository {
    private val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndMainInfoQueries

    override fun getByCharacterId(characterId: Long): DndMainInfo? {
        val result = queries.selectByCharacterId(characterId).executeAsOneOrNull() ?: return null
        return DndMainInfo(
            characterId = result.characterId,
            mainClass = result.mainClass,
            mainSubClass = result.mainSubClass,
            mainClassLevel = result.mainClassLevel?.toInt(),
            secondaryClass = result.secondaryClass,
            secondarySubClass = result.secondarySubClass,
            secondaryClassLevel = result.secondaryClassLevel?.toInt(),
            race = result.race,
            subRace = result.subRace,
            origin = result.origin,
            alignment = result.alignment
        )
    }

    override fun save(info: DndMainInfo) {
        val existing = queries.selectByCharacterId(info.characterId).executeAsOneOrNull()
        if (existing != null) {
            queries.update(
                info.mainClass, info.mainSubClass, info.mainClassLevel?.toLong(),
                info.secondaryClass, info.secondarySubClass, info.secondaryClassLevel?.toLong(),
                info.race, info.subRace, info.origin, info.alignment, info.characterId
            )
        } else {
            queries.insert(
                info.characterId, info.mainClass, info.mainSubClass, info.mainClassLevel?.toLong(),
                info.secondaryClass, info.secondarySubClass, info.secondaryClassLevel?.toLong(),
                info.race, info.subRace, info.origin, info.alignment
            )
        }
    }
}
