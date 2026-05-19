package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndFeature(
    val id: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val source: String = "",
    val sourceClass: String? = null,
    val sourceClassLevel: Int? = null,
    val sourceOrigin: String? = null,
    val sourceRace: String? = null,
    val sourceSubRace: String? = null,
    val sourceCustom: String? = null,
    val type: String = "",
    val description: String = "",
    val maxQuantity: Int? = null,
    val reloadRule: String? = null,
    val currentUsages: Int = 0,
    val tags: List<String> = emptyList()
) {
    companion object {
        val sources = listOf("Class", "Origin", "Race", "Custom")
        val types = listOf("Idiom", "Tool Proficiency", "Weapon/Armor Proficiency", "Feature", "Rechargable Feature")
        val reloadRules = listOf("Unlimited", "Short Rest", "Long Rest", "Day")
    }
}
