package com.dungeonsanddeigo.model

import kotlinx.serialization.Serializable

@Serializable
data class DndSkillEntry(
    val isTrained: Boolean = false,
    val modifier: String = "",
    val additionalValue: Int = 0,
    val totalValue: Int = 0
)

@Serializable
data class DndSkills(
    val characterId: Long = 0,
    val acrobatics: DndSkillEntry = DndSkillEntry(),
    val animalHandling: DndSkillEntry = DndSkillEntry(),
    val arcana: DndSkillEntry = DndSkillEntry(),
    val athletics: DndSkillEntry = DndSkillEntry(),
    val deception: DndSkillEntry = DndSkillEntry(),
    val history: DndSkillEntry = DndSkillEntry(),
    val insight: DndSkillEntry = DndSkillEntry(),
    val intimidation: DndSkillEntry = DndSkillEntry(),
    val investigation: DndSkillEntry = DndSkillEntry(),
    val medicine: DndSkillEntry = DndSkillEntry(),
    val nature: DndSkillEntry = DndSkillEntry(),
    val perception: DndSkillEntry = DndSkillEntry(),
    val performance: DndSkillEntry = DndSkillEntry(),
    val persuasion: DndSkillEntry = DndSkillEntry(),
    val religion: DndSkillEntry = DndSkillEntry(),
    val sleightOfHand: DndSkillEntry = DndSkillEntry(),
    val stealth: DndSkillEntry = DndSkillEntry(),
    val survival: DndSkillEntry = DndSkillEntry()
) {
    companion object {
        val skillNames = listOf(
            "Acrobatics", "Animal Handling", "Arcana", "Athletics",
            "Deception", "History", "Insight", "Intimidation",
            "Investigation", "Medicine", "Nature", "Perception",
            "Performance", "Persuasion", "Religion", "Sleight of Hand",
            "Stealth", "Survival"
        )

        val defaultModifiers = mapOf(
            "Acrobatics" to "Dex", "Animal Handling" to "Wis", "Arcana" to "Int",
            "Athletics" to "Str", "Deception" to "Cha", "History" to "Int",
            "Insight" to "Wis", "Intimidation" to "Cha", "Investigation" to "Int",
            "Medicine" to "Wis", "Nature" to "Int", "Perception" to "Wis",
            "Performance" to "Cha", "Persuasion" to "Cha", "Religion" to "Int",
            "Sleight of Hand" to "Dex", "Stealth" to "Dex", "Survival" to "Wis"
        )
    }

    fun getEntry(skillName: String): DndSkillEntry = when (skillName) {
        "Acrobatics" -> acrobatics
        "Animal Handling" -> animalHandling
        "Arcana" -> arcana
        "Athletics" -> athletics
        "Deception" -> deception
        "History" -> history
        "Insight" -> insight
        "Intimidation" -> intimidation
        "Investigation" -> investigation
        "Medicine" -> medicine
        "Nature" -> nature
        "Perception" -> perception
        "Performance" -> performance
        "Persuasion" -> persuasion
        "Religion" -> religion
        "Sleight of Hand" -> sleightOfHand
        "Stealth" -> stealth
        "Survival" -> survival
        else -> DndSkillEntry()
    }

    fun withEntry(skillName: String, entry: DndSkillEntry): DndSkills = when (skillName) {
        "Acrobatics" -> copy(acrobatics = entry)
        "Animal Handling" -> copy(animalHandling = entry)
        "Arcana" -> copy(arcana = entry)
        "Athletics" -> copy(athletics = entry)
        "Deception" -> copy(deception = entry)
        "History" -> copy(history = entry)
        "Insight" -> copy(insight = entry)
        "Intimidation" -> copy(intimidation = entry)
        "Investigation" -> copy(investigation = entry)
        "Medicine" -> copy(medicine = entry)
        "Nature" -> copy(nature = entry)
        "Perception" -> copy(perception = entry)
        "Performance" -> copy(performance = entry)
        "Persuasion" -> copy(persuasion = entry)
        "Religion" -> copy(religion = entry)
        "Sleight of Hand" -> copy(sleightOfHand = entry)
        "Stealth" -> copy(stealth = entry)
        "Survival" -> copy(survival = entry)
        else -> this
    }
}
