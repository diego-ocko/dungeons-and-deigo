package com.dungeonsanddeigo.repository

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndBaseStats

class AndroidDndBaseStatsRepository(context: Context) : DndBaseStatsRepository {
    private val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndBaseStatsQueries

    override fun getByCharacterId(characterId: Long): DndBaseStats? {
        val r = queries.selectByCharacterId(characterId).executeAsOneOrNull() ?: return null
        return DndBaseStats(
            characterId = r.characterId,
            strValue = r.strValue?.toInt(), strMod = r.strMod?.toInt(),
            dexValue = r.dexValue?.toInt(), dexMod = r.dexMod?.toInt(),
            conValue = r.conValue?.toInt(), conMod = r.conMod?.toInt(),
            intValue = r.intValue?.toInt(), intMod = r.intMod?.toInt(),
            wisValue = r.wisValue?.toInt(), wisMod = r.wisMod?.toInt(),
            chaValue = r.chaValue?.toInt(), chaMod = r.chaMod?.toInt(),
            proficiency = r.proficiency?.toInt(),
            maxLife = r.maxLife?.toInt(),
            vision = r.vision?.toInt(),
            hasDarkVision = r.hasDarkVision != 0L,
            speed = r.speed?.toInt(),
            hasStrRes = r.hasStrRes != 0L, hasDexRes = r.hasDexRes != 0L,
            hasConRes = r.hasConRes != 0L, hasIntRes = r.hasIntRes != 0L,
            hasWisRes = r.hasWisRes != 0L, hasChaRes = r.hasChaRes != 0L,
            emptyArmorClass = r.emptyArmorClass?.toInt()
        )
    }

    override fun save(stats: DndBaseStats) {
        val existing = queries.selectByCharacterId(stats.characterId).executeAsOneOrNull()
        fun b(v: Boolean) = if (v) 1L else 0L
        if (existing != null) {
            queries.update(
                stats.strValue?.toLong(), stats.strMod?.toLong(),
                stats.dexValue?.toLong(), stats.dexMod?.toLong(),
                stats.conValue?.toLong(), stats.conMod?.toLong(),
                stats.intValue?.toLong(), stats.intMod?.toLong(),
                stats.wisValue?.toLong(), stats.wisMod?.toLong(),
                stats.chaValue?.toLong(), stats.chaMod?.toLong(),
                stats.proficiency?.toLong(), stats.maxLife?.toLong(),
                stats.vision?.toLong(), b(stats.hasDarkVision), stats.speed?.toLong(),
                b(stats.hasStrRes), b(stats.hasDexRes), b(stats.hasConRes),
                b(stats.hasIntRes), b(stats.hasWisRes), b(stats.hasChaRes),
                stats.emptyArmorClass?.toLong(),
                stats.characterId
            )
        } else {
            queries.insert(
                stats.characterId,
                stats.strValue?.toLong(), stats.strMod?.toLong(),
                stats.dexValue?.toLong(), stats.dexMod?.toLong(),
                stats.conValue?.toLong(), stats.conMod?.toLong(),
                stats.intValue?.toLong(), stats.intMod?.toLong(),
                stats.wisValue?.toLong(), stats.wisMod?.toLong(),
                stats.chaValue?.toLong(), stats.chaMod?.toLong(),
                stats.proficiency?.toLong(), stats.maxLife?.toLong(),
                stats.vision?.toLong(), b(stats.hasDarkVision), stats.speed?.toLong(),
                b(stats.hasStrRes), b(stats.hasDexRes), b(stats.hasConRes),
                b(stats.hasIntRes), b(stats.hasWisRes), b(stats.hasChaRes),
                stats.emptyArmorClass?.toLong()
            )
        }
    }
}
