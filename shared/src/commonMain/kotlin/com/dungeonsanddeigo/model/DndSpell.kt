package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndSpell(
    val id: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val originClass: String = "",
    val originLevel: String = "",
    val circle: String = "Cantrip",
    val school: String = "",
    val castingTime: String = "",
    val duration: String = "",
    val range: String = "",
    val canBeRitual: Boolean = false,
    val needsConcentration: Boolean = false,
    val hasVerbal: Boolean = false,
    val hasSomatic: Boolean = false,
    val hasMaterial: Boolean = false,
    val materialComponents: String = "",
    val description: String = "",
    val higherCircles: String = "",
    val isAttack: Boolean = false,
    val needsSavingThrow: Boolean = false,
    val attackType: String = "",
    val savingThrowAbility: String = "",
    val attackDamageDice: String = "",
    val attackDamageType: String = "",
    val isPrepared: Boolean = false,
    val tags: List<String> = emptyList()
) {
    companion object {
        val circles = listOf("Cantrip", "Circle 1", "Circle 2", "Circle 3", "Circle 4", "Circle 5", "Circle 6", "Circle 7", "Circle 8", "Circle 9")

        val schools = listOf("Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation")

        val originLevels = (1..20).map { it.toString() } + "Learned by Scroll"

        val attackTypes = listOf("Roll for Attack", "Saving Throw")

        val savingThrowAbilities = listOf("Str", "Dex", "Con", "Int", "Wis", "Cha")

        val damageTypes = listOf("Acid", "Bludgeoning", "Cold", "Fire", "Force", "Lightning", "Necrotic", "Piercing", "Poison", "Psychic", "Radiant", "Slashing", "Thunder")
    }
}
