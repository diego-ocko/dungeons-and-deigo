package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndMoney

interface DndMoneyRepository {
    fun getByCharacterId(characterId: Long): DndMoney?
    fun save(money: DndMoney)
}
