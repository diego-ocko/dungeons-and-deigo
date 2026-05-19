package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndBackstory(
    val characterId: Long = 0,
    val personalityTraits: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val defects: String = "",
    val habits: String = "",
    val hasFaction: Boolean = false,
    val factionName: String = "",
    val factionSymbol: String? = null,
    val factionBackstory: String = "",
    val characterBackstory: String = ""
)
