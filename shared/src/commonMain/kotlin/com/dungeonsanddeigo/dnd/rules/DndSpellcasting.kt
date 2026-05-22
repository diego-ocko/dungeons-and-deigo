package com.dungeonsanddeigo.dnd.rules

import com.dungeonsanddeigo.model.DndBaseStats
import com.dungeonsanddeigo.model.DndMainInfo
import com.dungeonsanddeigo.model.DungeonsAndDragons

data class SpellcasterInfo(
    val className: String,
    val rawClassName: String,
    val subClassName: String?,
    val level: Int,
    val abilityAbbr: String,
    val abilityMod: Int,
    val spellMod: Int,
    val spellDC: Int,
    val needsPreparation: Boolean,
    val ritualOnly: Boolean = false,
    val isCustom: Boolean = false
)

fun abilityModFromStats(ability: String, stats: DndBaseStats): Int = when (ability) {
    "Str" -> stats.strValue?.let { calcModifier(it) } ?: 0
    "Dex" -> stats.dexValue?.let { calcModifier(it) } ?: 0
    "Con" -> stats.conValue?.let { calcModifier(it) } ?: 0
    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
    else -> 0
}

fun resolveSpellcasters(
    mainInfo: DndMainInfo?,
    stats: DndBaseStats,
    proficiency: Int,
    customAbilityLookup: (className: String) -> String?
): List<SpellcasterInfo> {
    val casters = mutableListOf<SpellcasterInfo>()

    fun addIfCaster(
        className: String?,
        subClassName: String?,
        level: Int,
        displayNameOverride: String? = null
    ) {
        if (className == null) return

        val ability = DungeonsAndDragons.spellcastingAbilityFor(className, subClassName)
        val isCustomClass = className !in DungeonsAndDragons.defaultClasses

        val resolvedAbility = ability ?: if (isCustomClass) {
            val custom = customAbilityLookup(className)
            if (custom == null || custom == "__none__") return
            custom
        } else return

        val abilityMod = abilityModFromStats(resolvedAbility, stats)
        val displayName = displayNameOverride ?: if (subClassName != null && subClassName in DungeonsAndDragons.subclassSpellcasting) {
            "$className ($subClassName)"
        } else className

        casters.add(
            SpellcasterInfo(
                className = displayName,
                rawClassName = className,
                subClassName = subClassName,
                level = level,
                abilityAbbr = resolvedAbility,
                abilityMod = abilityMod,
                spellMod = abilityMod + proficiency,
                spellDC = abilityMod + proficiency + 8,
                needsPreparation = className in DungeonsAndDragons.preparedCasters,
                ritualOnly = subClassName != null && subClassName in DungeonsAndDragons.ritualOnlySubclasses,
                isCustom = isCustomClass && ability == null
            )
        )
    }

    addIfCaster(mainInfo?.mainClass, mainInfo?.mainSubClass, mainInfo?.mainClassLevel ?: 1)
    addIfCaster(mainInfo?.secondaryClass, mainInfo?.secondarySubClass, mainInfo?.secondaryClassLevel ?: 1)

    return casters
}
