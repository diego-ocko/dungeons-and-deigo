package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndArmor(
    val id: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val type: String = "",
    val baseAC: Int = 10,
    val acModifier: String = "none",
    val minimumStrength: Int = 0,
    val hasSneakDisadvantage: Boolean = false,
    val weight: Double = 0.0,
    val price: Int = 0,
    val priceCurrency: String = "pg",
    val isEquipped: Boolean = false,
    val tags: List<String> = emptyList(),
    val additionalFeatures: String = ""
) {
    companion object {
        val types = listOf("Light Armor", "Medium Armor", "Heavy Armor", "Shield", "Clothes")
        val acModifiers = listOf("none", "Dex", "Dex (Max: 2)")
        val currencies = listOf("pc", "ps", "pe", "pg", "pp")
    }
}
