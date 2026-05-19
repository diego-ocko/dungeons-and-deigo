package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class Character(val id: Long = 0, val name: String, val sheetModelName: String, val imageBase64: String? = null) {
    val sheetModel: SheetModels
        get() = availableSheetModels.first { it.name == sheetModelName }
}
