package com.dungeonsanddeigo.dnd.rules

import com.dungeonsanddeigo.model.DndBaseStats
import com.dungeonsanddeigo.model.DndWeapon

data class AttackRow(
    val name: String,
    val range: String,
    val testMod: Int,
    val damage: String,
    val notes: String
)

fun calculateAttackRows(
    weapon: DndWeapon,
    stats: DndBaseStats,
    proficiency: Int,
    hasProficiency: Boolean
): List<AttackRow> {
    val strMod = stats.strValue?.let { calcModifier(it) } ?: 0
    val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0
    val isRanged = weapon.range && !weapon.thrown

    val rows = mutableListOf<AttackRow>()

    fun addRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
        val mod = if (useDex) dexMod else strMod
        val atkMod = mod + (if (hasProficiency) proficiency else 0)
        val statLabel = if (weapon.finesse) " (${if (useDex) "Dex" else "Str"})" else ""
        val handLabel = if (twoHanded) " [2H]" else ""
        val thrownLabel = if (thrown) " [Thrown]" else ""
        val name = "${weapon.name}$statLabel$handLabel$thrownLabel"
        val range = if (isRanged || thrown) "${weapon.rangeDistance}/${weapon.rangeLongDistance}m" else "Melee"
        val dice = if (twoHanded) weapon.versatileDice else weapon.damageDice
        val damage = if (isRanged || thrown) {
            "$dice ${weapon.damageType}"
        } else {
            val modSign = if (mod >= 0) "+" else ""
            "$dice$modSign$mod ${weapon.damageType}"
        }
        val icons = buildList {
            if (weapon.silver) add("Silver")
            if (weapon.heavy) add("Heavy")
            if (weapon.light) add("Light")
            if (weapon.reach) add("Reach")
            if (weapon.ammunition) add("Ammo")
            if (weapon.loading) add("Loading")
        }
        val notes = buildList {
            if (icons.isNotEmpty()) add(icons.joinToString(", "))
            if (weapon.additionalFeatures.isNotEmpty()) {
                val feat = if (weapon.additionalFeatures.length > 50)
                    weapon.additionalFeatures.take(50) + "..." else weapon.additionalFeatures
                add(feat)
            }
        }.joinToString(" | ")

        rows.add(AttackRow(name, range, atkMod, damage, notes))
    }

    if (isRanged) {
        addRow(useDex = true)
    } else if (weapon.finesse) {
        addRow(useDex = false)
        if (weapon.versatile) addRow(useDex = false, twoHanded = true)
        addRow(useDex = true)
        if (weapon.versatile) addRow(useDex = true, twoHanded = true)
        if (weapon.thrown) {
            addRow(useDex = false, thrown = true)
            addRow(useDex = true, thrown = true)
        }
    } else {
        addRow(useDex = false)
        if (weapon.versatile) addRow(useDex = false, twoHanded = true)
        if (weapon.thrown) addRow(useDex = false, thrown = true)
    }

    return rows
}
