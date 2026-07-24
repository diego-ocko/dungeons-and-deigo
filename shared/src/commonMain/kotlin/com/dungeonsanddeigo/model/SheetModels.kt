package com.dungeonsanddeigo.model

abstract class SheetModels(val name: String) {
    abstract val tabs: List<String>
}

class DungeonsAndDragons : SheetModels("Dungeons & Dragons") {
    override val tabs = listOf("Playing", "Main", "Stats", "Features", "Magic", "Inventory", "Background", "Notes", "Export")

    companion object {
        val defaultClasses = listOf(
            "Artificer", "Barbarian", "Bard", "Cleric", "Druid", "Fighter",
            "Monk", "Paladin", "Ranger", "Rogue", "Sorcerer", "Warlock", "Wizard"
        )

        val defaultSubClasses: Map<String, List<String>> = mapOf(
            "Artificer" to listOf("Alchemist", "Armorer", "Artillerist", "Battle Smith"),
            "Barbarian" to listOf("Path of the Berserker", "Path of the Totem Warrior"),
            "Bard" to listOf("College of Lore", "College of Valor"),
            "Cleric" to listOf("Knowledge", "Life", "Light", "Nature", "Tempest", "Trickery", "War Domains"),
            "Druid" to listOf("Circle of the Land", "Circle of the Moon"),
            "Fighter" to listOf("Battle Master", "Champion", "Eldritch Knight"),
            "Monk" to listOf("Way of the Four Elements", "Way of the Open Hand", "Way of Shadow"),
            "Paladin" to listOf("Oath of Devotion", "Oath of the Ancients", "Oath of Vengeance"),
            "Ranger" to listOf("Beast Master", "Hunter"),
            "Rogue" to listOf("Arcane Trickster", "Assassin", "Thief"),
            "Sorcerer" to listOf("Draconic Bloodline", "Wild Magic"),
            "Warlock" to listOf("The Archfey", "The Fiend", "The Great Old One"),
            "Wizard" to listOf("School of Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation")
        )

        fun subClassesFor(className: String?): List<String> =
            defaultSubClasses[className] ?: emptyList()

        val defaultRaces = listOf(
            "Dwarf", "Elf", "Halfling", "Gnome", "Human",
            "Dragonborn", "Tiefling", "Half-Elf", "Half-Orc"
        )

        val defaultSubRaces: Map<String, List<String>> = mapOf(
            "Dwarf" to listOf("Hill", "Mountain", "Duergar"),
            "Elf" to listOf("High", "Wood", "Drow", "Eladrin", "Sea", "Shadar-kai", "Moon", "Sun"),
            "Halfling" to listOf("Lightfoot", "Stout"),
            "Gnome" to listOf("Forest", "Rock", "Deep"),
            "Human" to listOf("Standard", "Variant"),
            "Dragonborn" to listOf("Black", "Blue", "Brass", "Bronze", "Copper", "Gold", "Green", "Red", "Silver", "White", "Ravenite"),
            "Tiefling" to listOf("Infernal", "Asmodeus", "Baalzebul", "Dispater", "Fierna", "Glasya", "Levistus", "Mammon", "Mephistopheles", "Zariel", "Variant"),
            "Half-Elf" to listOf("Standard", "Half-Drow", "Half-High", "Half-Wood", "Half-Aquatic"),
            "Half-Orc" to listOf("Standard")
        )

        fun subRacesFor(raceName: String?): List<String> =
            defaultSubRaces[raceName] ?: emptyList()

        val defaultAlignments = listOf(
            "Lawful Good", "Neutral Good", "Chaotic Good",
            "Lawful Neutral", "Neutral Neutral", "Chaotic Neutral",
            "Lawful Evil", "Neutral Evil", "Chaotic Evil"
        )

        val hitDice: Map<String, String> = mapOf(
            "Artificer" to "d8", "Barbarian" to "d12", "Bard" to "d8",
            "Cleric" to "d8", "Druid" to "d8", "Fighter" to "d10",
            "Monk" to "d8", "Paladin" to "d10", "Ranger" to "d10",
            "Rogue" to "d8", "Sorcerer" to "d6", "Warlock" to "d8", "Wizard" to "d6"
        )

        fun hitDieFor(className: String?): String = hitDice[className] ?: "d8"

        // Spell slots per level (full casters)
        val fullCasterSlots: Map<Int, List<Int>> = mapOf(
            1 to listOf(2), 2 to listOf(3), 3 to listOf(4, 2), 4 to listOf(4, 3),
            5 to listOf(4, 3, 2), 6 to listOf(4, 3, 3), 7 to listOf(4, 3, 3, 1),
            8 to listOf(4, 3, 3, 2), 9 to listOf(4, 3, 3, 3, 1), 10 to listOf(4, 3, 3, 3, 2),
            11 to listOf(4, 3, 3, 3, 2, 1), 12 to listOf(4, 3, 3, 3, 2, 1),
            13 to listOf(4, 3, 3, 3, 2, 1, 1), 14 to listOf(4, 3, 3, 3, 2, 1, 1),
            15 to listOf(4, 3, 3, 3, 2, 1, 1, 1), 16 to listOf(4, 3, 3, 3, 2, 1, 1, 1),
            17 to listOf(4, 3, 3, 3, 2, 1, 1, 1, 1), 18 to listOf(4, 3, 3, 3, 3, 1, 1, 1, 1),
            19 to listOf(4, 3, 3, 3, 3, 2, 1, 1, 1), 20 to listOf(4, 3, 3, 3, 3, 2, 2, 1, 1)
        )

        // Half casters (Paladin, Ranger, Artificer)
        val halfCasterSlots: Map<Int, List<Int>> = mapOf(
            1 to listOf(), 2 to listOf(2), 3 to listOf(3), 4 to listOf(3),
            5 to listOf(4, 2), 6 to listOf(4, 2), 7 to listOf(4, 3), 8 to listOf(4, 3),
            9 to listOf(4, 3, 2), 10 to listOf(4, 3, 2), 11 to listOf(4, 3, 3), 12 to listOf(4, 3, 3),
            13 to listOf(4, 3, 3, 1), 14 to listOf(4, 3, 3, 1), 15 to listOf(4, 3, 3, 2), 16 to listOf(4, 3, 3, 2),
            17 to listOf(4, 3, 3, 3, 1), 18 to listOf(4, 3, 3, 3, 1), 19 to listOf(4, 3, 3, 3, 2), 20 to listOf(4, 3, 3, 3, 2)
        )

        // Third casters (Eldritch Knight, Arcane Trickster)
        val thirdCasterSlots: Map<Int, List<Int>> = mapOf(
            1 to listOf(), 2 to listOf(), 3 to listOf(2), 4 to listOf(3),
            5 to listOf(3), 6 to listOf(3), 7 to listOf(4, 2), 8 to listOf(4, 2),
            9 to listOf(4, 2), 10 to listOf(4, 3), 11 to listOf(4, 3), 12 to listOf(4, 3),
            13 to listOf(4, 3, 2), 14 to listOf(4, 3, 2), 15 to listOf(4, 3, 2), 16 to listOf(4, 3, 3),
            17 to listOf(4, 3, 3), 18 to listOf(4, 3, 3), 19 to listOf(4, 3, 3, 1), 20 to listOf(4, 3, 3, 1)
        )

        val fullCasters = setOf("Bard", "Cleric", "Druid", "Sorcerer", "Warlock", "Wizard")
        val halfCasters = setOf("Artificer", "Paladin", "Ranger")
        val thirdCasterSubclasses = setOf("Eldritch Knight", "Arcane Trickster")

        fun spellSlotsFor(className: String?, subClassName: String?, level: Int): List<Int> {
            if (subClassName != null && subClassName in thirdCasterSubclasses) return thirdCasterSlots[level] ?: emptyList()
            if (className != null && className in fullCasters) return fullCasterSlots[level] ?: emptyList()
            if (className != null && className in halfCasters) return halfCasterSlots[level] ?: emptyList()
            return emptyList()
        }

        // Spellcasting ability per class (null = no spellcasting)
        val spellcastingAbility: Map<String, String?> = mapOf(
            "Artificer" to "Int",
            "Barbarian" to null,
            "Bard" to "Cha",
            "Cleric" to "Wis",
            "Druid" to "Wis",
            "Fighter" to null,
            "Monk" to null,
            "Paladin" to "Cha",
            "Ranger" to "Wis",
            "Rogue" to null,
            "Sorcerer" to "Cha",
            "Warlock" to "Cha",
            "Wizard" to "Int"
        )

        // Subclasses that grant spellcasting (with their ability)
        val subclassSpellcasting: Map<String, String> = mapOf(
            "Eldritch Knight" to "Int",
            "Way of the Four Elements" to "Wis",
            "Arcane Trickster" to "Int",
            "Path of the Totem Warrior" to "Wis"
        )

        // Subclasses that can only cast as rituals
        val ritualOnlySubclasses = setOf("Path of the Totem Warrior")

        // Classes that need to prepare spells
        val preparedCasters = setOf("Artificer", "Cleric", "Druid", "Paladin", "Wizard")

        fun spellcastingAbilityFor(className: String?, subClassName: String? = null): String? {
            // Check subclass first
            if (subClassName != null && subClassName in subclassSpellcasting) {
                return subclassSpellcasting[subClassName]
            }
            return spellcastingAbility[className]
        }
    }
}

class Tormenta20 : SheetModels("Tormenta 20") {
    override val tabs = listOf("Main")
}

class Daggerheart : SheetModels("Daggerheart") {
    override val tabs = listOf("Main")
}

val availableSheetModels: List<SheetModels> = listOf(
    DungeonsAndDragons(),
    Tormenta20(),
    Daggerheart()
)
