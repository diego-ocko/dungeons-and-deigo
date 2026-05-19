package com.dungeonsanddeigo.repository

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndInventoryItem

class AndroidDndInventoryRepository(context: Context) : DndInventoryRepository {
    private val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndInventoryItemQueries

    override fun getByCategory(characterId: Long, category: String): List<DndInventoryItem> {
        return queries.selectByCharacterIdAndCategory(characterId, category).executeAsList().map {
            DndInventoryItem(it.id, it.characterId, it.category, it.name, it.description, it.quantity.toInt(), it.equipped != 0L)
        }
    }

    override fun save(item: DndInventoryItem): Long {
        return if (item.id == 0L) {
            queries.insert(item.characterId, item.category, item.name, item.description, item.quantity.toLong(), if (item.equipped) 1L else 0L)
            queries.lastInsertId().executeAsOne()
        } else {
            queries.update(item.name, item.description, item.quantity.toLong(), if (item.equipped) 1L else 0L, item.id)
            item.id
        }
    }

    override fun delete(itemId: Long) { queries.deleteById(itemId) }
}
