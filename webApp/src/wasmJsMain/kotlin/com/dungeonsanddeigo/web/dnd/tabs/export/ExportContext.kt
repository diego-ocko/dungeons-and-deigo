package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tStat
import com.dungeonsanddeigo.model.*
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

class ExportContext(val character: Character) {

    val mainInfo  = Repos.mainInfo.getByCharacterId(character.id) ?: DndMainInfo(characterId = character.id)
    val stats     = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val skills    = Repos.skills.getByCharacterId(character.id) ?: DndSkills(characterId = character.id)
    val features  = Repos.features.getByCharacterId(character.id)
    val spells    = Repos.spell.getByCharacterId(character.id)
    val weapons   = Repos.weapon.getByCharacterId(character.id)
    val armors    = Repos.armor.getByCharacterId(character.id)
    val items     = DndInventoryItem.categories.flatMap { Repos.inventory.getByCategory(character.id, it) }
    val consumables = Repos.consumable.getByCharacterId(character.id)
    val magicItems  = Repos.magicItem.getByCharacterId(character.id)
    val money     = Repos.money.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)
    val appearance  = Repos.appearance.getByCharacterId(character.id)
    val backstory   = Repos.backstory.getByCharacterId(character.id)
    val notes     = Repos.note.getByCharacterId(character.id)

    val totalLevel = (mainInfo.mainClassLevel ?: 0) + (mainInfo.secondaryClassLevel ?: 0)
    val profBonus  = calcProficiency(totalLevel)
    val strMod = stats.strValue?.let { calcModifier(it) } ?: 0
    val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0
    val conMod = stats.conValue?.let { calcModifier(it) } ?: 0
    val intMod = stats.intValue?.let { calcModifier(it) } ?: 0
    val wisMod = stats.wisValue?.let { calcModifier(it) } ?: 0
    val chaMod = stats.chaValue?.let { calcModifier(it) } ?: 0

    val weaponCatProfs = mutableSetOf<String>()
    val weaponSpecificProfs = mutableSetOf<String>()

    init {
        features.filter { it.type == "Weapon/Armor Proficiency" }.forEach { f ->
            f.description.split(",").map { it.trim() }.forEach { entry ->
                when {
                    entry.startsWith("weapon_cat:") -> weaponCatProfs.add(entry.removePrefix("weapon_cat:"))
                    entry.startsWith("weapon:") -> weaponSpecificProfs.add(entry.removePrefix("weapon:"))
                }
            }
        }
    }

    // ── Spell helpers ─────────────────────────────────────────────────────────

    fun circleStr(circle: String) = when (circle) {
        "Cantrip"  -> t("magic.cantrip")
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

    // ── DOM helpers ───────────────────────────────────────────────────────────

    fun div(cls: String): HTMLDivElement =
        (document.createElement("div") as HTMLDivElement).also { it.className = cls }

    fun span(cls: String, text: String): HTMLSpanElement =
        (document.createElement("span") as HTMLSpanElement).also { it.className = cls; it.textContent = text }

    fun p(cls: String, text: String): HTMLParagraphElement =
        (document.createElement("p") as HTMLParagraphElement).also { it.className = cls; it.textContent = text }

    fun sectionLabel(text: String): HTMLElement =
        (document.createElement("h4") as HTMLHeadingElement).also { it.className = "export-section-label"; it.textContent = text }

    fun dotRow(isFilled: Boolean, value: String, name: String): HTMLDivElement {
        val row = div("export-list__row")
        row.appendChild(span(if (isFilled) "export-list__dot export-list__dot--filled" else "export-list__dot", ""))
        row.appendChild(span("export-list__val", value))
        row.appendChild(span("", name))
        return row
    }

    fun miniBox(label: String, value: String): HTMLDivElement {
        val box = div("export-mini-box")
        box.appendChild(div("export-mini-box__val").also { it.textContent = value })
        box.appendChild(div("export-mini-box__label").also { it.textContent = label })
        return box
    }

    fun statBox(label: String, value: String): HTMLDivElement {
        val box = div("export-stat-box")
        box.appendChild(div("export-stat-box__val").also { it.textContent = value })
        box.appendChild(div("export-stat-box__label").also { it.textContent = label })
        return box
    }

    fun hpBox(label: String, value: String): HTMLDivElement {
        val box = div("export-hp-box")
        box.appendChild(div("export-hp-box__label").also { it.textContent = label })
        box.appendChild(div("export-hp-box__val").also { it.textContent = value })
        return box
    }

    // ── Shared header (character name + subtitle chips) ───────────────────────

    fun buildHeader(): HTMLElement {
        val header = div("export-sheet__header")
        val charName = localStorage.getItem("char_name_${character.id}") ?: character.name
        header.appendChild(
            (document.createElement("h1") as HTMLElement).also {
                it.textContent = charName
                it.className = "export-sheet__char-name"
            }
        )
        val subTitle = div("export-sheet__subtitle")
        fun chip(text: String) { subTitle.appendChild(span("export-sheet__subtitle-chip", text)) }

        val classStr = buildString {
            val mc = mainInfo.mainClass; val ml = mainInfo.mainClassLevel
            if (mc != null) {
                append(mc)
                if (mainInfo.mainSubClass != null) append(" (${mainInfo.mainSubClass})")
                if (ml != null && ml > 0) append(" $ml")
            }
            val sc = mainInfo.secondaryClass; val sl = mainInfo.secondaryClassLevel
            if (sc != null) { append(" / $sc"); if (sl != null && sl > 0) append(" $sl") }
        }
        if (classStr.isNotEmpty()) chip(classStr)
        if (mainInfo.race != null) chip(buildString {
            append(mainInfo.race!!)
            if (mainInfo.subRace != null) append(" (${mainInfo.subRace})")
        })
        if (mainInfo.alignment != null) chip(mainInfo.alignment!!)
        if (mainInfo.origin != null) chip(mainInfo.origin!!)
        header.appendChild(subTitle)
        return header
    }

    // ── Attacks table (shared between Page 1 and potentially others) ──────────

    fun buildAttacksTable(modStr: (Int) -> String): HTMLElement {
        val container = div("export-attacks-list")
        val headers = div("export-attacks-header")
        listOf(t("inv.atkTable.test"), t("inv.atkTable.range"), t("inv.atkTable.damage")).forEach { h ->
            headers.appendChild(span("export-attacks-header__col", h))
        }
        container.appendChild(headers)

        fun dmgTypeShort(type: String) = when (type.lowercase()) {
            "bludgeoning" -> t("inv.dmg.bludgeoning")
            "piercing"    -> t("inv.dmg.piercing")
            "slashing"    -> t("inv.dmg.slashing")
            "acid"        -> t("magic.dmg.acid")
            "cold"        -> t("magic.dmg.cold")
            "fire"        -> t("magic.dmg.fire")
            "force"       -> t("magic.dmg.force")
            "lightning"   -> t("magic.dmg.lightning")
            "necrotic"    -> t("magic.dmg.necrotic")
            "poison"      -> t("magic.dmg.poison")
            "psychic"     -> t("magic.dmg.psychic")
            "radiant"     -> t("magic.dmg.radiant")
            "thunder"     -> t("magic.dmg.thunder")
            else -> type
        }

        val colsDef = "0.9fr 0.5fr 1.5fr"

        fun addRow(name: String, test: String, range: String, dmg: String) {
            val entry = div("export-attack-entry")
            entry.appendChild(div("export-attack-entry__name").also { it.textContent = name })
            val stats = div("export-attack-entry__stats")
            stats.style.setProperty("grid-template-columns", colsDef)
            stats.appendChild(span("export-attack-entry__stat", test))
            stats.appendChild(span("export-attack-entry__stat", range))
            val dmgCls = if (dmg.length > 13) "export-attack-entry__stat export-attack-entry__stat--small" else "export-attack-entry__stat"
            stats.appendChild(span(dmgCls, dmg))
            entry.appendChild(stats)
            container.appendChild(entry)
        }

        weapons.filter { it.isEquipped }.forEach { wpn ->
            val hasProfEq = wpn.category in weaponCatProfs || wpn.weaponType in weaponSpecificProfs
            val isRanged = wpn.range && !wpn.thrown

            fun weaponRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
                val mod = if (useDex) dexMod else strMod
                val atkMod = mod + (if (hasProfEq) profBonus else 0)
                val abilityLabel = tStat(if (useDex) "Dex" else "Str")
                val dice = if (twoHanded) wpn.versatileDice else wpn.damageDice
                val dmgMod = if (isRanged && !thrown) "" else (if (mod >= 0) "+$mod" else "$mod")
                val name = buildString {
                    append(wpn.name)
                    if (wpn.finesse) append(" (${if (useDex) tStat("Dex") else tStat("Str")})")
                    if (twoHanded) append(" [2H]")
                    if (thrown) append(" [↑]")
                }
                val rangeStr = when {
                    thrown && wpn.rangeDistance > 0 -> "${wpn.rangeDistance}/${wpn.rangeLongDistance}m"
                    isRanged && wpn.rangeDistance > 0 -> "${wpn.rangeDistance}/${wpn.rangeLongDistance}m"
                    wpn.reach -> "3m"
                    else -> t("export.melee")
                }
                addRow(name, "${modStr(atkMod)} ($abilityLabel)", rangeStr, "$dice$dmgMod ${dmgTypeShort(wpn.damageType)}")
            }

            when {
                isRanged -> weaponRow(useDex = true)
                wpn.finesse -> {
                    weaponRow(useDex = false)
                    if (wpn.versatile) weaponRow(useDex = false, twoHanded = true)
                    weaponRow(useDex = true)
                    if (wpn.versatile) weaponRow(useDex = true, twoHanded = true)
                    if (wpn.thrown) { weaponRow(useDex = false, thrown = true); weaponRow(useDex = true, thrown = true) }
                }
                else -> {
                    weaponRow(useDex = false)
                    if (wpn.versatile) weaponRow(useDex = false, twoHanded = true)
                    if (wpn.thrown) weaponRow(useDex = false, thrown = true)
                }
            }
        }

        // Disarmed
        val disAtkMod = strMod + profBonus
        val disDmg = if (stats.disarmedDice == "Normal") {
            "${maxOf(1, 1 + strMod)} ${t("inv.dmg.bludgeoning")}"
        } else {
            val s = if (strMod >= 0) "+$strMod" else "$strMod"
            "${stats.disarmedDice}$s ${t("inv.dmg.bludgeoning")}"
        }
        addRow(t("combat.disarmedAttack"), "${modStr(disAtkMod)} (${tStat("Str")})", t("export.melee"), disDmg)

        // Spell attacks
        val attackSpells = spells.filter {
            it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters)
        }
        attackSpells.forEach { spell ->
            val spellAbility = DungeonsAndDragons.spellcastingAbilityFor(mainInfo.mainClass, mainInfo.mainSubClass)
                ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo.mainClass}")
                    ?.takeIf { it != "__none__" }
            val spellMod = when (spellAbility) {
                "Str" -> strMod; "Dex" -> dexMod; "Con" -> conMod
                "Int" -> intMod; "Wis" -> wisMod; "Cha" -> chaMod; else -> 0
            }
            val circleSuffix = if (spell.circle == "Cantrip") "" else " (${circleStr(spell.circle)})"
            val name = "✦ ${spell.name}$circleSuffix"
            val spellRange = spell.range.ifBlank { "—" }
            val abilityLabel = tStat(spellAbility ?: "Int")
            if (spell.needsSavingThrow) {
                val saveDC = 8 + profBonus + spellMod
                val dmgStr = buildString {
                    if (spell.attackDamageDice.isNotEmpty()) append(spell.attackDamageDice)
                    if (spell.attackDamageType.isNotEmpty()) append(" ${dmgTypeShort(spell.attackDamageType)}")
                    if (isEmpty()) append("—")
                }
                addRow(name, "DC $saveDC (${tStat(spell.savingThrowAbility.ifBlank { abilityLabel })})", spellRange, dmgStr)
            } else {
                val dmg = buildString {
                    if (spell.attackDamageDice.isNotEmpty()) append(spell.attackDamageDice)
                    if (spell.attackDamageType.isNotEmpty()) append(" ${dmgTypeShort(spell.attackDamageType)}")
                    if (isEmpty()) append("—")
                }
                addRow(name, "${modStr(spellMod + profBonus)} ($abilityLabel)", spellRange, dmg)
            }
        }

        return container
    }
}
