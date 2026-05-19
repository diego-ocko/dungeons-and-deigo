package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndMainInfo(
    val characterId: Long = 0,
    val mainClass: String? = null,
    val mainSubClass: String? = null,
    val mainClassLevel: Int? = null,
    val secondaryClass: String? = null,
    val secondarySubClass: String? = null,
    val secondaryClassLevel: Int? = null,
    val race: String? = null,
    val subRace: String? = null,
    val origin: String? = null,
    val alignment: String? = null
)
