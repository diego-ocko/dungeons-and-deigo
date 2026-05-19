package com.dungeonsanddeigo.repository

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.Character

class AndroidCharacterRepository(context: Context) : CharacterRepository {
    private val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.characterEntityQueries

    override fun getAll(): List<Character> =
        queries.selectAll().executeAsList().map { Character(it.id, it.name, it.sheetModelName, it.imageBase64) }

    override fun insert(character: Character): Long {
        queries.insert(character.name, character.sheetModelName, character.imageBase64)
        return queries.lastInsertId().executeAsOne()
    }
}
