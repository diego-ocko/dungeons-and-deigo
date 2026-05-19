package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndConsumable(
    val id: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val type: String = "",
    val quantity: Int = 1,
    val effect: String = "",
    val price: Int = 0,
    val priceCurrency: String = "pg",
    val weight: Double = 0.0,
    val tags: List<String> = emptyList()
) {
    companion object {
        val types = listOf("Healing Potion", "Magic Potion", "Food", "Ammunition", "Other")
        val currencies = listOf("pc", "ps", "pe", "pg", "pp")
    }
}
