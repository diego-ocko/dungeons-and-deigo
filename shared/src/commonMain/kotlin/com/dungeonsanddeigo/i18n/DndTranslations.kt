package com.dungeonsanddeigo.i18n

fun tFeatureType(type: String): String = when (type) {
    "Idiom" -> t("features.idiom")
    "Tool Proficiency" -> t("features.toolProf")
    "Weapon/Armor Proficiency" -> t("features.weaponArmorProf")
    "Feature" -> t("features.feature")
    "Rechargable Feature" -> t("features.rechargable")
    else -> type
}

fun tSource(source: String): String = when (source) {
    "Class" -> t("features.source.class")
    "Origin" -> t("features.source.origin")
    "Race" -> t("features.source.race")
    "Custom" -> t("features.source.custom")
    else -> source
}

fun tReloadRule(rule: String): String = when (rule) {
    "Unlimited" -> t("features.reload.unlimited")
    "Short Rest" -> t("features.reload.shortRest")
    "Long Rest" -> t("features.reload.longRest")
    "Day" -> t("features.reload.day")
    else -> rule
}

fun tSection(section: String): String = when (section) {
    "Armor" -> t("inventory.armor")
    "Weapons" -> t("inventory.weapons")
    "Magic Items" -> t("inventory.magicItems")
    "Money" -> t("inventory.money")
    "Potions Ammo and Ration" -> t("inventory.potions")
    "Key Items, Loot and others" -> t("inventory.other")
    else -> section
}

fun tArmorType(type: String): String = when (type) {
    "Light Armor" -> t("inv.lightArmor")
    "Medium Armor" -> t("inv.mediumArmor")
    "Heavy Armor" -> t("inv.heavyArmor")
    "Shield" -> t("inv.shield")
    "Clothes" -> t("inv.clothes")
    else -> type
}

fun tAcModifier(mod: String): String = when (mod) {
    "none" -> t("inv.acMod.none")
    "Dex" -> t("inv.acMod.dex")
    "Dex (Max: 2)" -> t("inv.acMod.dexMax2")
    else -> mod
}

fun tCurrency(abbr: String): String = when (abbr) {
    "pc" -> t("coin.copper.abbr")
    "ps" -> t("coin.silver.abbr")
    "pe" -> t("coin.electrum.abbr")
    "pg" -> t("coin.gold.abbr")
    "pp" -> t("coin.platinum.abbr")
    else -> abbr
}

fun tLifestyle(ls: String): String = when (ls) {
    "Wretched" -> t("inv.lifestyle.wretched")
    "Squalid" -> t("inv.lifestyle.squalid")
    "Poor" -> t("inv.lifestyle.poor")
    "Modest" -> t("inv.lifestyle.modest")
    "Comfortable" -> t("inv.lifestyle.comfortable")
    "Wealthy" -> t("inv.lifestyle.wealthy")
    "Aristocratic" -> t("inv.lifestyle.aristocratic")
    else -> ls
}

fun tSchool(school: String): String = when (school) {
    "Abjuration" -> t("magic.school.abjuration")
    "Conjuration" -> t("magic.school.conjuration")
    "Divination" -> t("magic.school.divination")
    "Enchantment" -> t("magic.school.enchantment")
    "Evocation" -> t("magic.school.evocation")
    "Illusion" -> t("magic.school.illusion")
    "Necromancy" -> t("magic.school.necromancy")
    "Transmutation" -> t("magic.school.transmutation")
    else -> school
}

fun tSpellDamageType(dmg: String): String = when (dmg) {
    "Acid" -> t("magic.dmg.acid")
    "Bludgeoning" -> t("magic.dmg.bludgeoning")
    "Cold" -> t("magic.dmg.cold")
    "Fire" -> t("magic.dmg.fire")
    "Force" -> t("magic.dmg.force")
    "Lightning" -> t("magic.dmg.lightning")
    "Necrotic" -> t("magic.dmg.necrotic")
    "Piercing" -> t("magic.dmg.piercing")
    "Poison" -> t("magic.dmg.poison")
    "Psychic" -> t("magic.dmg.psychic")
    "Radiant" -> t("magic.dmg.radiant")
    "Slashing" -> t("magic.dmg.slashing")
    "Thunder" -> t("magic.dmg.thunder")
    else -> dmg
}

fun tCircle(circle: String): String = when (circle) {
    "Cantrip" -> t("magic.cantrip")
    "Circle 1" -> t("magic.circle1")
    "Circle 2" -> t("magic.circle2")
    "Circle 3" -> t("magic.circle3")
    "Circle 4" -> t("magic.circle4")
    "Circle 5" -> t("magic.circle5")
    "Circle 6" -> t("magic.circle6")
    "Circle 7" -> t("magic.circle7")
    "Circle 8" -> t("magic.circle8")
    "Circle 9" -> t("magic.circle9")
    else -> circle
}

fun tOriginLevel(level: String): String = when (level) {
    "Learned by Scroll" -> t("magic.learnedByScroll")
    else -> level
}
