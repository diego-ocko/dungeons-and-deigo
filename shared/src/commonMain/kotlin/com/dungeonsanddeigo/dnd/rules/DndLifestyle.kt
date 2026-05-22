package com.dungeonsanddeigo.dnd.rules

data class LifestyleCost(val amount: Int, val currency: String, val perDay: Boolean)

fun lifestyleCostFor(lifestyle: String): LifestyleCost? = when (lifestyle) {
    "Wretched" -> LifestyleCost(0, "", false)
    "Squalid" -> LifestyleCost(1, "ps", true)
    "Poor" -> LifestyleCost(2, "ps", true)
    "Modest" -> LifestyleCost(1, "pg", true)
    "Comfortable" -> LifestyleCost(2, "pg", true)
    "Wealthy" -> LifestyleCost(4, "pg", true)
    "Aristocratic" -> LifestyleCost(10, "pg", true)
    else -> null
}
