package com.dungeonsanddeigo.i18n

object DndTermsPtBr {
    val classes = mapOf(
        "Artificer" to "Artífice",
        "Barbarian" to "Bárbaro",
        "Bard" to "Bardo",
        "Cleric" to "Clérigo",
        "Druid" to "Druida",
        "Fighter" to "Guerreiro",
        "Monk" to "Monge",
        "Paladin" to "Paladino",
        "Ranger" to "Patrulheiro",
        "Rogue" to "Ladino",
        "Sorcerer" to "Feiticeiro",
        "Warlock" to "Bruxo",
        "Wizard" to "Mago"
    )

    val subclasses = mapOf(
        "Alchemist" to "Alquimista",
        "Armorer" to "Armeiro",
        "Artillerist" to "Artilheiro",
        "Battle Smith" to "Ferreiro de Batalha",
        "Path of the Berserker" to "Caminho do Berserker",
        "Path of the Totem Warrior" to "Caminho do Guerreiro Totêmico",
        "College of Lore" to "Colégio do Conhecimento",
        "College of Valor" to "Colégio da Bravura",
        "Knowledge" to "Conhecimento",
        "Life" to "Vida",
        "Light" to "Luz",
        "Nature" to "Natureza",
        "Tempest" to "Tempestade",
        "Trickery" to "Trapaça",
        "War Domains" to "Domínio da Guerra",
        "Circle of the Land" to "Círculo da Terra",
        "Circle of the Moon" to "Círculo da Lua",
        "Battle Master" to "Mestre de Batalha",
        "Champion" to "Campeão",
        "Eldritch Knight" to "Cavaleiro Arcano",
        "Way of the Four Elements" to "Caminho dos Quatro Elementos",
        "Way of the Open Hand" to "Caminho da Mão Aberta",
        "Way of Shadow" to "Caminho das Sombras",
        "Oath of Devotion" to "Juramento de Devoção",
        "Oath of the Ancients" to "Juramento dos Anciões",
        "Oath of Vengeance" to "Juramento de Vingança",
        "Beast Master" to "Mestre das Feras",
        "Hunter" to "Caçador",
        "Arcane Trickster" to "Trapaceiro Arcano",
        "Assassin" to "Assassino",
        "Thief" to "Ladrão",
        "Draconic Bloodline" to "Linhagem Dracônica",
        "Wild Magic" to "Magia Selvagem",
        "The Archfey" to "O Arquifada",
        "The Fiend" to "O Demônio",
        "The Great Old One" to "O Grande Antigo",
        "School of Abjuration" to "Escola de Abjuração",
        "Conjuration" to "Conjuração",
        "Divination" to "Adivinhação",
        "Enchantment" to "Encantamento",
        "Evocation" to "Evocação",
        "Illusion" to "Ilusão",
        "Necromancy" to "Necromancia",
        "Transmutation" to "Transmutação"
    )

    val races = mapOf(
        "Dwarf" to "Anão",
        "Elf" to "Elfo",
        "Halfling" to "Halfling",
        "Gnome" to "Gnomo",
        "Human" to "Humano",
        "Dragonborn" to "Draconato",
        "Tiefling" to "Tiefling",
        "Half-Elf" to "Meio-Elfo",
        "Half-Orc" to "Meio-Orc"
    )

    val subraces = mapOf(
        "Hill" to "Colina",
        "Mountain" to "Montanha",
        "Duergar" to "Duergar",
        "High" to "Alto",
        "Wood" to "Floresta",
        "Drow" to "Drow",
        "Eladrin" to "Eladrin",
        "Sea" to "Mar",
        "Shadar-kai" to "Shadar-kai",
        "Moon" to "Lua",
        "Sun" to "Sol",
        "Lightfoot" to "Pés-Leves",
        "Stout" to "Robusto",
        "Forest" to "Floresta",
        "Rock" to "Rocha",
        "Deep" to "Profundo",
        "Standard" to "Padrão",
        "Variant" to "Variante",
        "Ravenite" to "Ravenita",
        "Infernal" to "Infernal"
    )

    val alignments = mapOf(
        "Lawful Good" to "Leal e Bom",
        "Neutral Good" to "Neutro e Bom",
        "Chaotic Good" to "Caótico e Bom",
        "Lawful Neutral" to "Leal e Neutro",
        "Neutral Neutral" to "Neutro",
        "Chaotic Neutral" to "Caótico e Neutro",
        "Lawful Evil" to "Leal e Mau",
        "Neutral Evil" to "Neutro e Mau",
        "Chaotic Evil" to "Caótico e Mau"
    )
}

/**
 * Translate a D&D term. Returns the translated version if in PT-BR, otherwise the original.
 * The value stored is always in English.
 */
fun tDnd(category: String, value: String): String {
    if (I18n.current != Locale.PT_BR) return value
    return when (category) {
        "class" -> DndTermsPtBr.classes[value] ?: value
        "subclass" -> DndTermsPtBr.subclasses[value] ?: value
        "race" -> DndTermsPtBr.races[value] ?: value
        "subrace" -> DndTermsPtBr.subraces[value] ?: value
        "alignment" -> DndTermsPtBr.alignments[value] ?: value
        else -> value
    }
}

fun tStat(abbr: String): String {
    if (I18n.current != Locale.PT_BR) return abbr
    return when (abbr) {
        "Str" -> "For"
        "Dex" -> "Des"
        "Con" -> "Con"
        "Int" -> "Int"
        "Wis" -> "Sab"
        "Cha" -> "Car"
        "Strength" -> "Força"
        "Dexterity" -> "Destreza"
        "Constitution" -> "Constituição"
        "Intelligence" -> "Inteligência"
        "Wisdom" -> "Sabedoria"
        "Charisma" -> "Carisma"
        else -> abbr
    }
}

private val idiomsPtBr = mapOf(
    "Common" to "Comum", "Dwarvish" to "Anão", "Elvish" to "Élfico",
    "Giant" to "Gigante", "Gnomish" to "Gnômico", "Goblin" to "Goblin",
    "Halfling" to "Halfling", "Orc" to "Orc", "Draconic" to "Dracônico"
)

private val toolsPtBr = mapOf(
    "Alchemist's Supplies" to "Suprimentos de Alquimista",
    "Brewer's Supplies" to "Suprimentos de Cervejeiro",
    "Calligrapher's Supplies" to "Suprimentos de Calígrafo",
    "Carpenter's Tools" to "Ferramentas de Carpinteiro",
    "Cartographer's Tools" to "Ferramentas de Cartógrafo",
    "Cobbler's Tools" to "Ferramentas de Sapateiro",
    "Cook's Utensils" to "Utensílios de Cozinheiro",
    "Glassblower's Tools" to "Ferramentas de Vidreiro",
    "Jeweler's Tools" to "Ferramentas de Joalheiro",
    "Leatherworker's Tools" to "Ferramentas de Curtidor",
    "Mason's Tools" to "Ferramentas de Pedreiro",
    "Painter's Supplies" to "Suprimentos de Pintor",
    "Potter's Tools" to "Ferramentas de Oleiro",
    "Smith's Tools" to "Ferramentas de Ferreiro",
    "Tinker's Tools" to "Ferramentas de Funileiro",
    "Weaver's Tools" to "Ferramentas de Tecelão",
    "Woodcarver's Tools" to "Ferramentas de Entalhador",
    "Disguise Kit" to "Kit de Disfarce",
    "Forgery Kit" to "Kit de Falsificação",
    "Herbalism Kit" to "Kit de Herbalismo",
    "Navigator's Tools" to "Ferramentas de Navegador",
    "Poisoner's Kit" to "Kit de Venenos",
    "Thieves' Tools" to "Ferramentas de Ladrão"
)

private val weaponsPtBr = mapOf(
    "Club" to "Clava", "Dagger" to "Adaga", "Greatclub" to "Bordão",
    "Handaxe" to "Machadinha", "Javelin" to "Dardo", "Light Hammer" to "Martelo Leve",
    "Mace" to "Maça", "Quarterstaff" to "Bordão", "Sickle" to "Foice",
    "Spear" to "Lança", "Crossbow (Light)" to "Besta (Leve)", "Dart" to "Dardo",
    "Shortbow" to "Arco Curto", "Sling" to "Funda", "Battleaxe" to "Machado de Batalha",
    "Flail" to "Mangual", "Glaive" to "Glaive", "Greataxe" to "Machado Grande",
    "Greatsword" to "Montante", "Halberd" to "Alabarda", "Lance" to "Lança de Montaria",
    "Longsword" to "Espada Longa", "Maul" to "Malho", "Morningstar" to "Estrela d'Alva",
    "Pike" to "Pique", "Rapier" to "Rapieira", "Scimitar" to "Cimitarra",
    "Shortsword" to "Espada Curta", "Trident" to "Tridente", "War Pick" to "Picareta de Guerra",
    "Warhammer" to "Martelo de Guerra", "Whip" to "Chicote", "Blowgun" to "Zarabatana",
    "Crossbow (Hand)" to "Besta (Mão)", "Crossbow (Heavy)" to "Besta (Pesada)",
    "Longbow" to "Arco Longo", "Net" to "Rede"
)

fun tIdiom(value: String): String {
    if (I18n.current != Locale.PT_BR) return value
    return idiomsPtBr[value] ?: value
}

fun tTool(value: String): String {
    if (I18n.current != Locale.PT_BR) return value
    return toolsPtBr[value] ?: value
}

fun tWeapon(value: String): String {
    if (I18n.current != Locale.PT_BR) return value
    return weaponsPtBr[value] ?: value
}

private val statusesPtBr = mapOf(
    "Poisoned" to "Envenenado",
    "Confused" to "Confuso",
    "Flying" to "Voando",
    "Frightened" to "Amedrontado",
    "Blinded" to "Cego",
    "Charmed" to "Enfeitiçado",
    "Deafened" to "Surdo",
    "Grappled" to "Agarrado",
    "Incapacitated" to "Incapacitado",
    "Invisible" to "Invisível",
    "Paralyzed" to "Paralisado",
    "Petrified" to "Petrificado",
    "Prone" to "Caído",
    "Restrained" to "Impedido",
    "Stunned" to "Atordoado",
    "Unconscious" to "Inconsciente",
    "Exhaustion" to "Exaustão"
)

fun tStatus(value: String): String {
    if (I18n.current != Locale.PT_BR) return value
    return statusesPtBr[value] ?: value
}
