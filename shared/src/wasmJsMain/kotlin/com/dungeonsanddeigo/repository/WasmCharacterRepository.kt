package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.Character
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmCharacterRepository : CharacterRepository {
    private val key = "characters"
    private val counterKey = "characters_counter"

    override fun getAll(): List<Character> {
        val json = localStorage.getItem(key) ?: return emptyList()
        return Json.decodeFromString<List<Character>>(json)
    }

    override fun insert(character: Character): Long {
        val counter = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, counter.toString())
        val charWithId = character.copy(id = counter)
        val list = getAll().toMutableList()
        list.add(charWithId)
        localStorage.setItem(key, Json.encodeToString(list))
        return counter
    }
}
