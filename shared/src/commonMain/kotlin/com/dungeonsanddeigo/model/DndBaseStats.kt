package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndBaseStats(
    val characterId: Long = 0,
    val strValue: Int? = null,
    val strMod: Int? = null,
    val dexValue: Int? = null,
    val dexMod: Int? = null,
    val conValue: Int? = null,
    val conMod: Int? = null,
    val intValue: Int? = null,
    val intMod: Int? = null,
    val wisValue: Int? = null,
    val wisMod: Int? = null,
    val chaValue: Int? = null,
    val chaMod: Int? = null,
    val proficiency: Int? = null,
    val maxLife: Int? = null,
    val vision: Int? = null,
    val hasDarkVision: Boolean = false,
    val speed: Int? = null,
    val hasStrRes: Boolean = false,
    val hasDexRes: Boolean = false,
    val hasConRes: Boolean = false,
    val hasIntRes: Boolean = false,
    val hasWisRes: Boolean = false,
    val hasChaRes: Boolean = false,
    val emptyArmorClass: Int? = null,
    val disarmedDice: String = "Normal"
) {
    companion object {
        val disarmedDiceOptions = listOf("Normal", "1d4", "1d6", "1d8", "1d10")
    }
}
