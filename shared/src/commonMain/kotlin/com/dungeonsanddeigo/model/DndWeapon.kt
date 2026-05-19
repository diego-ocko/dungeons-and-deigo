package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndWeapon(
    val id: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val category: String = "",
    val weaponType: String = "",
    val damageDice: String = "",
    val damageType: String = "",
    val ammunition: Boolean = false,
    val finesse: Boolean = false,
    val heavy: Boolean = false,
    val light: Boolean = false,
    val loading: Boolean = false,
    val range: Boolean = false,
    val rangeDistance: Int = 0,
    val rangeLongDistance: Int = 0,
    val reach: Boolean = false,
    val special: Boolean = false,
    val specialDescription: String = "",
    val thrown: Boolean = false,
    val twoHanded: Boolean = false,
    val versatile: Boolean = false,
    val versatileDice: String = "",
    val silver: Boolean = false,
    val additionalFeatures: String = "",
    val weight: Double = 0.0,
    val price: Int = 0,
    val priceCurrency: String = "pg",
    val tags: List<String> = emptyList(),
    val isEquipped: Boolean = false
) {
    companion object {
        val categories = listOf("Simple", "Martial", "Others")
        val damageTypes = listOf("Bludgeoning", "Piercing", "Slashing")
        val weaponTypes = listOf("Club", "Dagger", "Greatclub", "Handaxe", "Javelin", "Light Hammer", "Mace", "Quarterstaff", "Sickle", "Spear", "Crossbow (Light)", "Dart", "Shortbow", "Sling", "Battleaxe", "Flail", "Glaive", "Greataxe", "Greatsword", "Halberd", "Lance", "Longsword", "Maul", "Morningstar", "Pike", "Rapier", "Scimitar", "Shortsword", "Trident", "War Pick", "Warhammer", "Whip", "Blowgun", "Crossbow (Hand)", "Crossbow (Heavy)", "Longbow", "Net")
        val currencies = listOf("pc", "ps", "pe", "pg", "pp")
    }
}
