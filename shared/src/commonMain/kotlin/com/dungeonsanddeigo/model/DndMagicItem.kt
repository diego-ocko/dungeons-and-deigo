package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndMagicItem(
    val id: Long = 0,
    val characterId: Long = 0,
    val name: String = "",
    val needSynch: Boolean = false,
    val isSynched: Boolean = false,
    val effect: String = "",
    val weight: Double = 0.0,
    val price: Int = 0,
    val priceCurrency: String = "pg",
    val tags: List<String> = emptyList()
) {
    companion object {
        val currencies = listOf("pc", "ps", "pe", "pg", "pp")
    }
}
