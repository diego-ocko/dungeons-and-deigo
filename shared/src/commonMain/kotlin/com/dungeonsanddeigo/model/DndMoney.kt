package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndMoney(
    val characterId: Long = 0,
    val copper: Int = 0,
    val silver: Int = 0,
    val electrum: Int = 0,
    val gold: Int = 0,
    val platinum: Int = 0,
    val lifestyle: String = ""
) {
    companion object {
        val lifestyles = listOf("Wretched", "Squalid", "Poor", "Modest", "Comfortable", "Wealthy", "Aristocratic")
    }
}
