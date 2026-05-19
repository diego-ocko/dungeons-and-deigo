package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndNote(
    val id: Long = 0,
    val characterId: Long = 0,
    val title: String = "",
    val timestamp: String = "",
    val session: String = "",
    val note: String = "",
    val tags: List<String> = emptyList()
)
