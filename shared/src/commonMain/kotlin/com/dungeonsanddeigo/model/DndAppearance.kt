package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndAppearance(
    val characterId: Long = 0,
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val eyeColor: String = "",
    val skinColor: String = "",
    val hairColor: String = "",
    val description: String = ""
)
