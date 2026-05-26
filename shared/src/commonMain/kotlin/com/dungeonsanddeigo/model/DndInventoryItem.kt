package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndInventoryItem(
    val id: Long = 0,
    val characterId: Long = 0,
    val category: String = "",
    val name: String = "",
    val description: String = "",
    val quantity: Int = 1,
    val equipped: Boolean = false,
    val weight: Double = 0.0,
    val price: Int = 0,
    val priceCurrency: String = "pg",
    val tags: List<String> = emptyList()
) {
    companion object {
        val categories = listOf("Armor", "Weapons", "Magic Items", "Potions Ammo and Ration", "Key Items, Loot and others")
    }
}
