package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.Character

interface CharacterRepository {
    fun getAll(): List<Character>
    fun insert(character: Character): Long
}
