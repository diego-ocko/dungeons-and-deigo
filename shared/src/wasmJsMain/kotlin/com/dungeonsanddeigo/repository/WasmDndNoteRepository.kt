package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndNote
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndNoteRepository {
    private fun key(characterId: Long) = "dnd_notes_$characterId"
    private val counterKey = "dnd_notes_counter"

    private fun nextId(): Long {
        val id = (localStorage.getItem(counterKey)?.toLongOrNull() ?: 0) + 1
        localStorage.setItem(counterKey, id.toString())
        return id
    }

    fun getByCharacterId(characterId: Long): List<DndNote> {
        val json = localStorage.getItem(key(characterId)) ?: return emptyList()
        return Json.decodeFromString<List<DndNote>>(json)
    }

    fun save(note: DndNote): Long {
        val list = getByCharacterId(note.characterId).toMutableList()
        return if (note.id == 0L) {
            val id = nextId()
            list.add(note.copy(id = id))
            localStorage.setItem(key(note.characterId), Json.encodeToString(list))
            id
        } else {
            val index = list.indexOfFirst { it.id == note.id }
            if (index >= 0) list[index] = note else list.add(note)
            localStorage.setItem(key(note.characterId), Json.encodeToString(list))
            note.id
        }
    }

    fun delete(characterId: Long, noteId: Long) {
        val list = getByCharacterId(characterId).filter { it.id != noteId }
        localStorage.setItem(key(characterId), Json.encodeToString(list))
    }
}
