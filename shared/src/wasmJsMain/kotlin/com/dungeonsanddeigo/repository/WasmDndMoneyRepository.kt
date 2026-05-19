package com.dungeonsanddeigo.repository

import com.dungeonsanddeigo.model.DndMoney
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmDndMoneyRepository : DndMoneyRepository {
    private fun key(characterId: Long) = "dnd_money_$characterId"

    override fun getByCharacterId(characterId: Long): DndMoney? {
        val json = localStorage.getItem(key(characterId)) ?: return null
        return Json.decodeFromString<DndMoney>(json)
    }

    override fun save(money: DndMoney) {
        localStorage.setItem(key(money.characterId), Json.encodeToString(money))
    }
}
