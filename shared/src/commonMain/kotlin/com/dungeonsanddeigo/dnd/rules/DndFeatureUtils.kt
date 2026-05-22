package com.dungeonsanddeigo.dnd.rules

import com.dungeonsanddeigo.model.DndFeature

fun featureTypeSortOrder(f: DndFeature): Int = when (f.type) {
    "Weapon/Armor Proficiency" -> 0
    "Tool Proficiency" -> 1
    "Idiom" -> 2
    "Rechargable Feature" -> 3
    "Feature" -> 4
    else -> 5
}

fun featureSourceSortOrder(f: DndFeature): Int = when (f.source) {
    "Race" -> 0
    "Origin" -> 1
    "Custom" -> 2
    "Class" -> 3
    else -> 4
}

fun featureSourceLabel(f: DndFeature, tDnd: (String, String) -> String, t: (String) -> String): String = when (f.source) {
    "Class" -> "${tDnd("class", f.sourceClass ?: "")} Lv.${f.sourceClassLevel ?: "?"}"
    "Origin" -> f.sourceOrigin ?: t("main.origin")
    "Race" -> listOfNotNull(f.sourceRace?.let { tDnd("race", it) }, f.sourceSubRace?.let { tDnd("subrace", it) }).joinToString(" / ").ifEmpty { t("main.race") }
    "Custom" -> f.sourceCustom ?: t("features.source.custom")
    else -> f.source
}
