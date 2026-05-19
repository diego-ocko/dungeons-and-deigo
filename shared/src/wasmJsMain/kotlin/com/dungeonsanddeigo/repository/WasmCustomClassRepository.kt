package com.dungeonsanddeigo.repository

import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WasmCustomClassRepository {
    private val key = "dnd_custom_classes"

    fun getAll(): List<String> {
        val json = localStorage.getItem(key) ?: return emptyList()
        return Json.decodeFromString<List<String>>(json)
    }

    fun add(className: String) {
        val list = getAll().toMutableList()
        if (className !in list) {
            list.add(className)
            localStorage.setItem(key, Json.encodeToString(list))
        }
    }
}
