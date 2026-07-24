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

// Page 1: Core play information — ability scores, saves, skills, combat stats, attacks, features
fun buildExportPage1(ctx: ExportContext): HTMLDivElement {
    val page = ctx.div("export-page")

    val mainInfo = ctx.mainInfo
    val stats = ctx.stats
    val skills = ctx.skills
    val features = ctx.features
    val spells = ctx.spells
    val weapons = ctx.weapons
    val armors = ctx.armors

    val totalLevel = ctx.totalLevel
    val profBonus = ctx.profBonus
    val strMod = ctx.strMod
    val dexMod = ctx.dexMod
    val conMod = ctx.conMod
    val intMod = ctx.intMod
    val wisMod = ctx.wisMod
    val chaMod = ctx.chaMod

    fun modStr(v: Int) = if (v >= 0) "+$v" else "$v"

    // ── Header ────────────────────────────────────────────────────────────────
    page.appendChild(ctx.buildHeader())

    // ── Body grid ─────────────────────────────────────────────────────────────
    val body = ctx.div("export-body")

    data class AbilityInfo(val label: String, val fullLabel: String, val value: Int?, val mod: Int, val hasSave: Boolean)
    val abilities = listOf(
        AbilityInfo("STR", "Strength",     stats.strValue, strMod, stats.hasStrRes),
        AbilityInfo("DEX", "Dexterity",    stats.dexValue, dexMod, stats.hasDexRes),
        AbilityInfo("CON", "Constitution", stats.conValue, conMod, stats.hasConRes),
        AbilityInfo("INT", "Intelligence", stats.intValue, intMod, stats.hasIntRes),
        AbilityInfo("WIS", "Wisdom",       stats.wisValue, wisMod, stats.hasWisRes),
        AbilityInfo("CHA", "Charisma",     stats.chaValue, chaMod, stats.hasChaRes)
    )

    // col 2 — Ability scores
    val abilitiesPanel = ctx.div("export-panel export-panel--abilities")

    // Inspiration + Prof bonus at the top
    val inspBox = ctx.div("export-inspiration-box")
    inspBox.appendChild(ctx.div("export-inspiration-box__checkbox"))
    inspBox.appendChild(ctx.div("export-inspiration-box__label").also { it.textContent = t("export.inspiration") })
    abilitiesPanel.appendChild(inspBox)
    abilitiesPanel.appendChild(ctx.miniBox(t("stats.proficiency"), modStr(profBonus)))

    abilities.forEach { ab ->
        val card = ctx.div("export-ability-card")
        card.appendChild(ctx.div("export-ability-card__label").also { it.textContent = tStat(ab.label) })
        card.appendChild(ctx.div("export-ability-card__mod").also { it.textContent = modStr(ab.mod) })
        card.appendChild(ctx.div("export-ability-card__score").also { it.textContent = ab.value?.toString() ?: "—" })
        abilitiesPanel.appendChild(card)
    }
    val percEntry = skills.getEntry("perception")
    val passivePerc = 10 + wisMod + (if (percEntry.isTrained) profBonus else 0)
    val percBox = ctx.div("export-passive-box")
    percBox.appendChild(ctx.span("export-passive-box__val", passivePerc.toString()))
    percBox.appendChild(ctx.span("export-passive-box__label", t("export.passivePerception")))
    abilitiesPanel.appendChild(percBox)
    body.appendChild(abilitiesPanel)

    // col 3-5 — Saves + Skills
    val savesSkillsPanel = ctx.div("export-panel export-panel--saves-skills")
    savesSkillsPanel.appendChild(ctx.sectionLabel(t("export.savingThrows")))
    val savesList = ctx.div("export-list")
    abilities.forEach { ab ->
        val saveVal = ab.mod + (if (ab.hasSave) profBonus else 0)
        savesList.appendChild(ctx.dotRow(ab.hasSave, modStr(saveVal), tStat(ab.fullLabel)))
    }
    savesSkillsPanel.appendChild(savesList)
    savesSkillsPanel.appendChild(ctx.sectionLabel(t("export.skills")))
    val skillsList = ctx.div("export-skill-list")
    DndSkills.skillNames.forEachIndexed { idx, skillName ->
        val entry = skills.getEntry(skillName)
        val abilityAbbr = DndSkills.defaultModifiers[skillName] ?: ""
        val row = ctx.div("export-skill-row ${if (idx % 2 == 0) "export-skill-row--even" else ""}")
        row.appendChild(ctx.span(if (entry.isTrained) "export-list__dot export-list__dot--filled" else "export-list__dot", ""))
        row.appendChild(ctx.span("export-skill-row__val", modStr(entry.totalValue)))
        row.appendChild(ctx.span("export-skill-row__ability", tStat(abilityAbbr)))
        row.appendChild(ctx.span("export-skill-row__name", t("skill.$skillName")))
        skillsList.appendChild(row)
    }
    savesSkillsPanel.appendChild(skillsList)
    body.appendChild(savesSkillsPanel)

    // col 4-7 — Combat stats block (3-column × 3-row internal grid)
    val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
    val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
    val shieldAC = equippedShield?.baseAC ?: 0
    val totalAC = if (equippedArmor == null) {
        (stats.emptyArmorClass ?: 10) + shieldAC
    } else {
        val modBonus = when (equippedArmor.acModifier) {
            "Dex" -> dexMod
            "Dex (Max: 2)" -> minOf(dexMod, 2)
            else -> 0
        }
        equippedArmor.baseAC + modBonus + shieldAC
    }
    val currentLife = localStorage.getItem("dnd_playing_life_${ctx.character.id}")?.toIntOrNull() ?: (stats.maxLife ?: 0)
    val hitDie = DungeonsAndDragons.hitDieFor(mainInfo.mainClass)

    val combatPanel = ctx.div("export-panel export-panel--combat")

    // Row 1: CA | Iniciativa | Velocidade
    combatPanel.appendChild(ctx.statBox(t("stats.ac"), totalAC.toString()))
    combatPanel.appendChild(ctx.statBox(t("stats.initiative"), modStr(dexMod)))
    combatPanel.appendChild(ctx.statBox(t("stats.speed"), "${stats.speed ?: 0}m"))

    // Row 2: HP Atual (2 cols) | Vida Temporária (1 col)
    combatPanel.appendChild(ctx.hpBox(t("export.hpCurrent"), currentLife.toString()).also { it.className += " export-hp-box--wide" })
    combatPanel.appendChild(ctx.hpBox(t("export.tempHp"), ""))

    // Row 3: HP Máx (2 cols) | Visão (1 col)
    combatPanel.appendChild(ctx.hpBox(t("export.hpMax"), (stats.maxLife ?: 0).toString()).also { it.className += " export-hp-box--wide" })

    val visionBox = ctx.div("export-hp-box")
    visionBox.appendChild(ctx.div("export-hp-box__label").also { it.textContent = t("stats.vision") })
    val visionVal = ctx.div("export-hp-box__val")
    val v = stats.vision
    visionVal.textContent = if (v != null && v > 0) "${v}m" else "—"
    visionBox.appendChild(visionVal)
    if (stats.hasDarkVision) {
        visionBox.appendChild(ctx.div("export-hp-box__sub").also { it.textContent = t("stats.darkVision") })
    }
    combatPanel.appendChild(visionBox)

    // Row 4: Hit Dice (2 cols) | Death Saves (1 col)
    val hitDiceBox = ctx.div("export-hp-box export-hp-box--wide export-hit-dice-box")
    hitDiceBox.appendChild(ctx.div("export-hp-box__label").also { it.textContent = t("export.hitDice") })

    // Collect hit dice per class (main + secondary)
    data class HdInfo(val die: String, val total: Int, val used: Int)
    val hdList = mutableListOf<HdInfo>()
    mainInfo.mainClass?.let { cls ->
        val lvl = mainInfo.mainClassLevel ?: 0
        if (lvl > 0) {
            val used = localStorage.getItem("dnd_hitdice_main_${ctx.character.id}")?.toIntOrNull() ?: 0
            hdList.add(HdInfo(DungeonsAndDragons.hitDieFor(cls), lvl, used))
        }
    }
    mainInfo.secondaryClass?.let { cls ->
        val lvl = mainInfo.secondaryClassLevel ?: 0
        if (lvl > 0) {
            val used = localStorage.getItem("dnd_hitdice_sec_${ctx.character.id}")?.toIntOrNull() ?: 0
            hdList.add(HdInfo(DungeonsAndDragons.hitDieFor(cls), lvl, used))
        }
    }

    val hdTable = ctx.div("export-hit-dice-table")
    // Header
    val hdHeader = ctx.div("export-hit-dice-row export-hit-dice-row--header")
    listOf("d4", "d6", "d8", "d10", "d12").forEach { die ->
        hdHeader.appendChild(ctx.span("export-hit-dice-cell export-hit-dice-cell--die", die))
    }
    hdTable.appendChild(hdHeader)
    // Total row
    val hdTotal = ctx.div("export-hit-dice-row")
    listOf("d4", "d6", "d8", "d10", "d12").forEach { die ->
        val info = hdList.firstOrNull { it.die == die }
        hdTotal.appendChild(ctx.span("export-hit-dice-cell", info?.total?.toString() ?: "—"))
    }
    hdTable.appendChild(hdTotal)
    // Used row
    val hdUsed = ctx.div("export-hit-dice-row")
    listOf("d4", "d6", "d8", "d10", "d12").forEach { die ->
        val info = hdList.firstOrNull { it.die == die }
        val usedText = info?.let { "${it.used}" } ?: "—"
        hdUsed.appendChild(ctx.span("export-hit-dice-cell export-hit-dice-cell--used", usedText))
    }
    hdTable.appendChild(hdUsed)

    hitDiceBox.appendChild(hdTable)
    combatPanel.appendChild(hitDiceBox)

    val deathBox = ctx.div("export-hp-box export-death-saves")
    deathBox.appendChild(ctx.div("export-hp-box__label").also { it.textContent = t("export.deathSaves") })
    val deathGrid = ctx.div("export-death-grid")
    listOf("❤️" to t("export.deathSaves"), "☠️" to t("export.deathSaves")).forEach { (icon, _) ->
        val row = ctx.div("export-death-row")
        row.appendChild(ctx.span("export-death-row__icon", icon))
        repeat(3) { row.appendChild(ctx.div("export-death-row__box")) }
        deathGrid.appendChild(row)
    }
    deathBox.appendChild(deathGrid)
    combatPanel.appendChild(deathBox)

    body.appendChild(combatPanel)

    // col 9-11 — Features / Backstory
    val featuresPanel = ctx.div("export-panel export-panel--features")
    val backstory = ctx.backstory
    if (backstory != null) {
        fun bgBox(label: String, text: String) {
            if (text.isEmpty()) return
            val box = ctx.div("export-bg-box")
            box.appendChild(ctx.div("export-bg-box__label").also { it.textContent = label })
            box.appendChild(ctx.div("export-bg-box__text").also { it.textContent = text })
            featuresPanel.appendChild(box)
        }
        bgBox(t("bg.personalityTraits"), backstory.personalityTraits)
        bgBox(t("bg.ideals"), backstory.ideals)
        bgBox(t("bg.bonds"), backstory.bonds)
        bgBox(t("bg.defects"), backstory.defects)
    }
    val classFeatures = features.filter { it.type == "Feature" || it.type == "Rechargable Feature" }
    if (classFeatures.isNotEmpty()) {
        featuresPanel.appendChild(ctx.sectionLabel(t("export.features")))
        classFeatures.forEach { feat ->
            val card = ctx.div("export-feature-card")
            val rechargeStr = if (feat.maxQuantity != null) " (${feat.maxQuantity})" else ""
            card.appendChild(ctx.div("export-feature-card__name").also { it.textContent = "${feat.name}$rechargeStr" })
            if (feat.description.isNotEmpty())
                card.appendChild(ctx.div("export-feature-card__desc").also { it.textContent = feat.description })
            featuresPanel.appendChild(card)
        }
    }
    body.appendChild(featuresPanel)

    // col 2-5 (below abilities + saves/skills) — Weapon/Armor proficiencies
    val weaponArmorFeatures = features.filter { it.type == "Weapon/Armor Proficiency" }
    if (weaponArmorFeatures.isNotEmpty()) {
        // Aggregate all entries across features of this type
        val armorCats = mutableSetOf<String>()
        val weaponCats = mutableSetOf<String>()
        val specificWeapons = mutableListOf<String>()
        weaponArmorFeatures.forEach { f ->
            f.description.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { entry ->
                when {
                    entry.startsWith("armor:") -> armorCats.add(entry.removePrefix("armor:"))
                    entry.startsWith("weapon_cat:") -> weaponCats.add(entry.removePrefix("weapon_cat:"))
                    entry.startsWith("weapon:") -> specificWeapons.add(entry.removePrefix("weapon:"))
                }
            }
        }

        val wpnProfPanel = ctx.div("export-panel export-panel--weapon-prof")
        wpnProfPanel.appendChild(ctx.sectionLabel(t("export.weaponArmorProf")))

        // Armor row: Armaduras: [✔] Leve  [ ] Média  [✔] Pesada  [ ] Escudo
        val armorRow = ctx.div("export-prof-check-row")
        armorRow.appendChild(ctx.span("export-prof-check-row__title", "${t("features.armor")}:"))
        listOf("Light" to t("features.armor.light"), "Medium" to t("features.armor.medium"),
               "Heavy" to t("features.armor.heavy"), "Shields" to t("features.armor.shields")).forEach { (key, label) ->
            val chip = ctx.div("export-prof-chip ${if (key in armorCats) "export-prof-chip--checked" else ""}")
            chip.appendChild(ctx.span("export-prof-chip__box", if (key in armorCats) "✔" else ""))
            chip.appendChild(ctx.span("export-prof-chip__label", label))
            armorRow.appendChild(chip)
        }
        wpnProfPanel.appendChild(armorRow)

        // Weapon row: Armas: [✔] Simples  [ ] Marcial  e armas específicas
        val weaponRow = ctx.div("export-prof-check-row")
        weaponRow.appendChild(ctx.span("export-prof-check-row__title", "${t("features.weaponCategories")}:"))
        listOf("Simple" to t("features.weapon.simple"), "Martial" to t("features.weapon.martial")).forEach { (key, label) ->
            val chip = ctx.div("export-prof-chip ${if (key in weaponCats) "export-prof-chip--checked" else ""}")
            chip.appendChild(ctx.span("export-prof-chip__box", if (key in weaponCats) "✔" else ""))
            chip.appendChild(ctx.span("export-prof-chip__label", label))
            weaponRow.appendChild(chip)
        }
        if (specificWeapons.isNotEmpty()) {
            val othersChip = ctx.div("export-prof-chip export-prof-chip--others")
            othersChip.appendChild(ctx.span("export-prof-chip__label", "${t("features.weapon.others")}: ${specificWeapons.joinToString(", ")}"))
            weaponRow.appendChild(othersChip)
        }
        wpnProfPanel.appendChild(weaponRow)

        body.appendChild(wpnProfPanel)
    }

    page.appendChild(body)

    // ── Bottom: Attacks | Spells brief | Prof + Inventory ─────────────────────
    val bottomPanel = ctx.div("export-panel export-panel--bottom")

    val attacksSection = ctx.div("export-bottom__attacks")
    attacksSection.appendChild(ctx.sectionLabel(t("export.attacks")))
    attacksSection.appendChild(ctx.buildAttacksTable(modStr = { v -> modStr(v) }))
    bottomPanel.appendChild(attacksSection)

    // Spells brief (circle + name list only)
    if (spells.isNotEmpty()) {
        val spellsSection = ctx.div("export-bottom__spells")
        spellsSection.appendChild(ctx.sectionLabel(t("export.spells")))
        val circleOrder = listOf("Cantrip", "Circle 1", "Circle 2", "Circle 3", "Circle 4", "Circle 5", "Circle 6", "Circle 7", "Circle 8", "Circle 9")
        val spellsByCircle = spells.groupBy { it.circle }
        circleOrder.forEach { circle ->
            val circleSpells = spellsByCircle[circle] ?: return@forEach
            val circleDiv = ctx.div("export-spell-circle")
            circleDiv.appendChild(ctx.span("export-spell-circle__label", circle))
            circleDiv.appendChild(ctx.span("export-spell-circle__spells", circleSpells.joinToString(", ") { s ->
                buildString {
                    append(s.name)
                    val flags = mutableListOf<String>()
                    if (s.isPrepared) flags.add("P")
                    if (s.needsConcentration) flags.add("C")
                    if (s.canBeRitual) flags.add("R")
                    if (flags.isNotEmpty()) append(" [${flags.joinToString("")}]")
                }
            }))
            spellsSection.appendChild(circleDiv)
        }
        bottomPanel.appendChild(spellsSection)
    }

    // Proficiencies (languages + tools only — weapons already shown above body)
    val profSection = ctx.div("export-bottom__prof-inv")
    val languageFeatures = features.filter { it.type == "Idiom" }
    val toolFeatures = features.filter { it.type == "Tool Proficiency" }
    fun profBlock(label: String, items: List<DndFeature>) {
        if (items.isEmpty()) return
        profSection.appendChild(ctx.sectionLabel(label))
        profSection.appendChild(ctx.p("export-prof-text", items.joinToString(" • ") { it.name }))
    }
    profBlock(t("export.languageProf"), languageFeatures)
    profBlock(t("export.toolProf"), toolFeatures)
    bottomPanel.appendChild(profSection)

    page.appendChild(bottomPanel)
    return page
}
