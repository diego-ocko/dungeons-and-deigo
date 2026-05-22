package com.dungeonsanddeigo.util

val namedColors = mapOf(
    "Black" to Triple(0, 0, 0), "White" to Triple(255, 255, 255),
    "Red" to Triple(255, 0, 0), "Dark Red" to Triple(139, 0, 0),
    "Brown" to Triple(139, 69, 19), "Light Brown" to Triple(181, 137, 80),
    "Tan" to Triple(210, 180, 140), "Beige" to Triple(245, 245, 220),
    "Peach" to Triple(255, 218, 185), "Olive" to Triple(128, 128, 0),
    "Green" to Triple(0, 128, 0), "Dark Green" to Triple(0, 100, 0),
    "Blue" to Triple(0, 0, 255), "Light Blue" to Triple(173, 216, 230),
    "Dark Blue" to Triple(0, 0, 139), "Purple" to Triple(128, 0, 128),
    "Violet" to Triple(238, 130, 238), "Pink" to Triple(255, 192, 203),
    "Orange" to Triple(255, 165, 0), "Yellow" to Triple(255, 255, 0),
    "Gold" to Triple(255, 215, 0), "Silver" to Triple(192, 192, 192),
    "Gray" to Triple(128, 128, 128), "Dark Gray" to Triple(64, 64, 64),
    "Light Gray" to Triple(211, 211, 211), "Copper" to Triple(184, 115, 51),
    "Auburn" to Triple(165, 42, 42), "Blonde" to Triple(250, 240, 190),
    "Platinum" to Triple(229, 228, 226), "Ivory" to Triple(255, 255, 240),
    "Ebony" to Triple(33, 36, 33), "Hazel" to Triple(142, 118, 58),
    "Amber" to Triple(255, 191, 0), "Teal" to Triple(0, 128, 128)
)

fun hexToColorName(hex: String): String {
    val r = hex.substring(1, 3).toInt(16)
    val g = hex.substring(3, 5).toInt(16)
    val b = hex.substring(5, 7).toInt(16)
    var closest = "Black"
    var minDist = Int.MAX_VALUE
    namedColors.forEach { (name, rgb) ->
        val dist = (r - rgb.first) * (r - rgb.first) + (g - rgb.second) * (g - rgb.second) + (b - rgb.third) * (b - rgb.third)
        if (dist < minDist) { minDist = dist; closest = name }
    }
    return closest
}

fun colorNameToHex(name: String): String? {
    val rgb = namedColors[name] ?: return null
    return "#${rgb.first.toString(16).padStart(2, '0')}${rgb.second.toString(16).padStart(2, '0')}${rgb.third.toString(16).padStart(2, '0')}"
}
