package com.dungeonsanddeigo.dnd.rules

import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.model.DndBaseStats

data class ArmorClassResult(
    val totalAC: Int,
    val lacksArmorProficiency: Boolean,
    val lacksShieldProficiency: Boolean,
    val lacksStrength: Boolean,
    val hasSneakDisadvantage: Boolean,
    val effectiveSpeed: Int
)

fun calculateArmorClass(
    armors: List<DndArmor>,
    stats: DndBaseStats,
    armorProficiencies: Set<String>
): ArmorClassResult {
    val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0
    val noArmorAC = stats.emptyArmorClass ?: 10
    val charStrength = stats.strValue ?: 0
    val charSpeed = stats.speed ?: 0

    val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
    val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
    val shieldAC = equippedShield?.baseAC ?: 0

    val totalAC = if (equippedArmor == null) {
        noArmorAC + shieldAC
    } else {
        val modBonus = when (equippedArmor.acModifier) {
            "Dex" -> dexMod
            "Dex (Max: 2)" -> minOf(dexMod, 2)
            else -> 0
        }
        equippedArmor.baseAC + modBonus + shieldAC
    }

    val armorProfKey = when (equippedArmor?.type) {
        "Light Armor" -> "Light"
        "Medium Armor" -> "Medium"
        "Heavy Armor" -> "Heavy"
        else -> null
    }
    val lacksArmorProf = equippedArmor != null && armorProfKey != null && armorProfKey !in armorProficiencies
    val lacksShieldProf = equippedShield != null && "Shields" !in armorProficiencies
    val lacksStrength = equippedArmor != null && equippedArmor.minimumStrength > 0 && charStrength < equippedArmor.minimumStrength
    val hasSneakDisadv = equippedArmor?.hasSneakDisadvantage == true
    val effectiveSpeed = if (lacksStrength) charSpeed - 3 else charSpeed

    return ArmorClassResult(
        totalAC = totalAC,
        lacksArmorProficiency = lacksArmorProf,
        lacksShieldProficiency = lacksShieldProf,
        lacksStrength = lacksStrength,
        hasSneakDisadvantage = hasSneakDisadv,
        effectiveSpeed = effectiveSpeed
    )
}
