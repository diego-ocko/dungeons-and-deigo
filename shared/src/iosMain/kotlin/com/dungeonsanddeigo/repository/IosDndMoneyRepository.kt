package com.dungeonsanddeigo.repository

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndMoney

class IosDndMoneyRepository : DndMoneyRepository {
    private val driver = NativeSqliteDriver(AppDatabase.Schema, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndMoneyQueries

    override fun getByCharacterId(characterId: Long): DndMoney? {
        val r = queries.selectByCharacterId(characterId).executeAsOneOrNull() ?: return null
        return DndMoney(r.characterId, r.copper.toInt(), r.silver.toInt(), r.electrum.toInt(), r.gold.toInt(), r.platinum.toInt(), r.lifestyle)
    }

    override fun save(money: DndMoney) {
        val existing = queries.selectByCharacterId(money.characterId).executeAsOneOrNull()
        if (existing != null) {
            queries.update(money.copper.toLong(), money.silver.toLong(), money.electrum.toLong(), money.gold.toLong(), money.platinum.toLong(), money.lifestyle, money.characterId)
        } else {
            queries.insert(money.characterId, money.copper.toLong(), money.silver.toLong(), money.electrum.toLong(), money.gold.toLong(), money.platinum.toLong(), money.lifestyle)
        }
    }
}
