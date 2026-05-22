package com.dungeonsanddeigo.dnd.rules

fun calcModifier(value: Int): Int = (value - 10) / 2 - (if (value < 10 && value % 2 != 0) 1 else 0)

fun formatModifier(value: Int?): String {
    if (value == null) return ""
    val mod = calcModifier(value)
    return if (mod >= 0) "+$mod" else "$mod"
}

fun calcProficiency(totalLevel: Int): Int = when {
    totalLevel <= 4 -> 2
    totalLevel <= 8 -> 3
    totalLevel <= 12 -> 4
    totalLevel <= 16 -> 5
    else -> 6
}
