package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tDnd
import com.dungeonsanddeigo.i18n.tIdiom
import com.dungeonsanddeigo.i18n.tStat
import com.dungeonsanddeigo.i18n.tTool
import com.dungeonsanddeigo.i18n.tWeapon
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

    // Row 1: CA | Iniciativa | Velocidade (each span 2 of 6)
    combatPanel.appendChild(ctx.statBox(t("stats.ac"), totalAC.toString()).also { it.className += " export-stat-box--2col" })
    combatPanel.appendChild(ctx.statBox(t("stats.initiative"), modStr(dexMod)).also { it.className += " export-stat-box--2col" })
    combatPanel.appendChild(ctx.statBox(t("stats.speedShort"), "${stats.speed ?: 0}m").also { it.className += " export-stat-box--2col" })

    // Row 2: HP Atual (4 cols) | Vida Temporária (2 cols)
    combatPanel.appendChild(ctx.hpBox(t("export.hpCurrent"), currentLife.toString()).also { it.className += " export-hp-box--wide" })
    combatPanel.appendChild(ctx.hpBox(t("export.tempHp"), "").also { it.className += " export-hp-box--2col" })

    // Row 3: HP Máx (4 cols) | Visão (2 cols)
    combatPanel.appendChild(ctx.hpBox(t("export.hpMax"), (stats.maxLife ?: 0).toString()).also { it.className += " export-hp-box--wide" })

    val visionBox = ctx.div("export-hp-box export-hp-box--2col")
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
    val hitDiceBox = ctx.div("export-hp-box export-hp-box--half export-hit-dice-box")
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

    val activeDice = hdList.map { it.die }
    val colsDef = "repeat(${activeDice.size.coerceAtLeast(1)}, 1fr)"
    val hdTable = ctx.div("export-hit-dice-table")
    // Header
    val hdHeader = ctx.div("export-hit-dice-row export-hit-dice-row--header")
    hdHeader.style.setProperty("grid-template-columns", colsDef)
    activeDice.forEach { die -> hdHeader.appendChild(ctx.span("export-hit-dice-cell export-hit-dice-cell--die", die)) }
    hdTable.appendChild(hdHeader)
    // Total row
    val hdTotal = ctx.div("export-hit-dice-row")
    hdTotal.style.setProperty("grid-template-columns", colsDef)
    hdList.forEach { info -> hdTotal.appendChild(ctx.span("export-hit-dice-cell", info.total.toString())) }
    hdTable.appendChild(hdTotal)
    // Used row
    val hdUsed = ctx.div("export-hit-dice-row")
    hdUsed.style.setProperty("grid-template-columns", colsDef)
    hdList.forEach { info -> hdUsed.appendChild(ctx.span("export-hit-dice-cell export-hit-dice-cell--used", info.used.toString())) }
    hdTable.appendChild(hdUsed)

    hitDiceBox.appendChild(hdTable)
    combatPanel.appendChild(hitDiceBox)

    val deathBox = ctx.div("export-hp-box export-hp-box--half export-death-saves")
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

    // col 5-8 — Combat + Attacks stacked in a flex wrapper
    val combatWrapper = ctx.div("export-panel--combat-col")
    combatWrapper.appendChild(combatPanel)
    val attacksPanel = ctx.div("export-panel export-panel--attacks")
    attacksPanel.appendChild(ctx.sectionLabel(t("export.attacks")))
    attacksPanel.appendChild(ctx.buildAttacksTable(modStr = { v -> modStr(v) }))
    combatWrapper.appendChild(attacksPanel)

    // Spell slots panel
    val mainCls = mainInfo.mainClass
    val mainLvl = mainInfo.mainClassLevel ?: 0
    val mainSlots = DungeonsAndDragons.spellSlotsFor(mainCls, mainInfo.mainSubClass, mainLvl)
    val secCls2 = mainInfo.secondaryClass
    val secLvl = mainInfo.secondaryClassLevel ?: 0
    val secSlots = DungeonsAndDragons.spellSlotsFor(secCls2, mainInfo.secondarySubClass, secLvl)
    // Merge slots: take max per circle
    val maxCircles = maxOf(mainSlots.size, secSlots.size)
    val mergedSlots = (0 until maxCircles).map { i ->
        maxOf(mainSlots.getOrElse(i) { 0 }, secSlots.getOrElse(i) { 0 })
    }.filter { it > 0 }

    if (mergedSlots.isNotEmpty()) {
        val slotsPanel = ctx.div("export-panel export-panel--spell-slots")
        slotsPanel.appendChild(ctx.sectionLabel(t("export.spellSlots")))
        val slotsRow = ctx.div("export-spell-slots-row")
        mergedSlots.forEachIndexed { idx, total ->
            val circleNum = idx + 1
            val usedMain = localStorage.getItem("dnd_spell_slots_${ctx.character.id}_${mainCls}_$circleNum")?.toIntOrNull() ?: 0
            val usedSec = if (secCls2 != null) localStorage.getItem("dnd_spell_slots_${ctx.character.id}_${secCls2}_$circleNum")?.toIntOrNull() ?: 0 else 0
            val used = usedMain + usedSec
            val remaining = (total - used).coerceAtLeast(0)
            val col = ctx.div("export-slot-col")
            col.appendChild(ctx.span("export-slot-col__circle", "${circleNum}º"))
            col.appendChild(ctx.span("export-slot-col__val", "$remaining/$total"))
            slotsRow.appendChild(col)
        }
        slotsPanel.appendChild(slotsRow)
        combatWrapper.appendChild(slotsPanel)
    }

    // Spells panel
    if (spells.isNotEmpty()) {
        val spellsPanel = ctx.div("export-panel export-panel--spells-list")
        spellsPanel.appendChild(ctx.sectionLabel(t("export.spells")))
        val circleOrder = listOf("Cantrip", "Circle 1", "Circle 2", "Circle 3", "Circle 4", "Circle 5", "Circle 6", "Circle 7", "Circle 8", "Circle 9")
        val spellsByCircle = spells.groupBy { it.circle }
        circleOrder.forEach { circle ->
            val circleSpells = spellsByCircle[circle] ?: return@forEach
            val circleDiv = ctx.div("export-spell-circle")
            circleDiv.appendChild(ctx.span("export-spell-circle__label", ctx.circleStr(circle)))
            val isCantrip = circle == "Cantrip"
            val spellsText = circleSpells.joinToString(", ") { s ->
                buildString {
                    if (!isCantrip) append(if (s.isPrepared) "✅ " else "🔲 ")
                    append(s.name)
                    if (s.canBeRitual) append(" [R]")
                }
            }
            circleDiv.appendChild(ctx.span("export-spell-circle__spells", spellsText))
            spellsPanel.appendChild(circleDiv)
        }
        combatWrapper.appendChild(spellsPanel)
    }

    body.appendChild(combatWrapper)

    // col 9-11 — Features / Backstory
    val featuresPanel = ctx.div("export-panel export-panel--features")

    // Limited abilities (Rechargable Feature) — table above features
    val rechargableFeatures = features.filter { it.type == "Rechargable Feature" }
    if (rechargableFeatures.isNotEmpty()) {
        featuresPanel.appendChild(ctx.sectionLabel(t("export.limitedFeatures")))

        val limitedTable = ctx.div("export-limited-table")

        val hdrRow = ctx.div("export-limited-row export-limited-row--header")
        hdrRow.appendChild(ctx.span("export-limited-cell export-limited-cell--name", ""))
        hdrRow.appendChild(ctx.span("export-limited-cell export-limited-cell--reload", ""))
        hdrRow.appendChild(ctx.span("export-limited-cell export-limited-cell--num", t("export.limitedFeatures.max")))
        hdrRow.appendChild(ctx.span("export-limited-cell export-limited-cell--num", t("export.limitedFeatures.remaining")))
        limitedTable.appendChild(hdrRow)

        fun rechargeAbbr(rule: String?) = when (rule) {
            "Short Rest"  -> t("export.recharge.short")
            "Long Rest"   -> t("export.recharge.long")
            "Day"         -> t("export.recharge.day")
            else          -> t("export.recharge.unlimited")
        }

        rechargableFeatures.forEach { feat ->
            val remaining = (feat.maxQuantity ?: 0) - feat.currentUsages
            val row = ctx.div("export-limited-row")
            row.appendChild(ctx.span("export-limited-cell export-limited-cell--name", feat.name))
            row.appendChild(ctx.span("export-limited-cell export-limited-cell--reload", rechargeAbbr(feat.reloadRule)))
            row.appendChild(ctx.span("export-limited-cell export-limited-cell--num", feat.maxQuantity?.toString() ?: "—"))
            row.appendChild(ctx.span("export-limited-cell export-limited-cell--num", remaining.toString()))
            limitedTable.appendChild(row)
        }
        featuresPanel.appendChild(limitedTable)

        val legend = ctx.div("export-limited-legend")
        legend.textContent = t("export.recharge.legend")
        featuresPanel.appendChild(legend)
    }

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
    val allFeatures = features.filter { it.type == "Feature" || it.type == "Rechargable Feature" }
    if (allFeatures.isNotEmpty()) {
        featuresPanel.appendChild(ctx.sectionLabel(t("export.features")))

        fun addFeatureCard(feat: DndFeature) {
            val card = ctx.div("export-feature-card")
            val rechargeStr = if (feat.maxQuantity != null) " (${feat.maxQuantity})" else ""
            card.appendChild(ctx.div("export-feature-card__name").also { it.textContent = "${feat.name}$rechargeStr" })
            if (feat.description.isNotEmpty())
                card.appendChild(ctx.div("export-feature-card__desc").also { it.textContent = feat.description })
            featuresPanel.appendChild(card)
        }

        data class FeatureGroup(val label: String, val items: List<DndFeature>)

        val mainCls = mainInfo.mainClass
        val secCls = mainInfo.secondaryClass

        val raceGroup  = allFeatures.filter { it.source == "Race" }
        val originGroup = allFeatures.filter { it.source == "Origin" }
        val mainClsGroup = allFeatures.filter { it.source == "Class" && it.sourceClass == mainCls }
            .sortedBy { it.sourceClassLevel ?: 0 }
        val secClsGroup = allFeatures.filter { it.source == "Class" && it.sourceClass == secCls }
            .sortedBy { it.sourceClassLevel ?: 0 }
        val customGroup = allFeatures.filter { it.source == "Custom" }

        val mainClsLabel = if (mainCls != null) "${t("features.source.class")}: ${tDnd("class", mainCls)}" else t("features.source.class")
        val secClsLabel  = if (secCls != null)  "${t("features.source.class")}: ${tDnd("class", secCls)}"  else ""

        val groups = listOf(
            FeatureGroup(t("features.source.race"), raceGroup),
            FeatureGroup(t("features.source.origin"), originGroup),
            FeatureGroup(mainClsLabel, mainClsGroup),
            FeatureGroup(secClsLabel, secClsGroup),
            FeatureGroup(t("features.source.custom"), customGroup)
        )

        groups.forEach { group ->
            if (group.items.isEmpty() || group.label.isEmpty()) return@forEach
            featuresPanel.appendChild(ctx.div("export-feature-group-label").also { it.textContent = group.label })
            group.items.forEach { addFeatureCard(it) }
        }
    }
    body.appendChild(featuresPanel)

    // Left column wrapper: abilities + saves/skills + proficiências (stacked)
    val leftCol = ctx.div("export-panel--left-col")
    leftCol.appendChild(abilitiesPanel)
    leftCol.appendChild(savesSkillsPanel)

    // Weapon/Armor proficiencies — appended to leftCol below
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
        wpnProfPanel.appendChild(weaponRow)

        if (specificWeapons.isNotEmpty()) {
            val othersRow = ctx.div("export-prof-check-row")
            othersRow.appendChild(ctx.span("export-prof-check-row__title", "${t("export.otherWeapons")}:"))
            othersRow.appendChild(ctx.span("export-prof-chip__label", specificWeapons.joinToString(", ") { tWeapon(it) }))
            wpnProfPanel.appendChild(othersRow)
        }

        val toolFeatures = features.filter { it.type == "Tool Proficiency" }
        val allTools = toolFeatures.flatMap { f -> f.description.split(",").map { tTool(it.trim()) }.filter { it.isNotEmpty() } }
        if (allTools.isNotEmpty()) {
            val toolRow = ctx.div("export-prof-check-row")
            toolRow.appendChild(ctx.span("export-prof-check-row__title", "${t("export.toolProf")}:"))
            toolRow.appendChild(ctx.span("export-prof-chip__label", allTools.joinToString(", ")))
            wpnProfPanel.appendChild(toolRow)
        }

        val languageFeatures = features.filter { it.type == "Idiom" }
        val allLangs = languageFeatures.flatMap { f -> f.description.split(",").map { tIdiom(it.trim()) }.filter { it.isNotEmpty() } }
        if (allLangs.isNotEmpty()) {
            val langRow = ctx.div("export-prof-check-row")
            langRow.appendChild(ctx.span("export-prof-check-row__title", "${t("export.languageProf")}:"))
            langRow.appendChild(ctx.span("export-prof-chip__label", allLangs.joinToString(", ")))
            wpnProfPanel.appendChild(langRow)
        }

        leftCol.appendChild(wpnProfPanel)
    }

    // Equipment panel
    val equipPanel = ctx.div("export-panel export-panel--equipment")
    equipPanel.appendChild(ctx.sectionLabel(t("export.equipment")))

    // Row 1: Armadura + checkboxes
    val equippedArmor2 = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
    val equippedShield2 = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
    val armorEquipRow = ctx.div("export-equip-row")
    armorEquipRow.appendChild(ctx.span("export-equip-row__label", "${t("export.equippedArmor")}:"))
    armorEquipRow.appendChild(ctx.span("export-equip-row__val", equippedArmor2?.name ?: "—"))
    val stealthCheck = if (equippedArmor2?.hasSneakDisadvantage == true) "[✔]" else "[ ]"
    armorEquipRow.appendChild(ctx.span("export-equip-row__check", "$stealthCheck ${t("export.stealthDisadv")}"))
    val shieldCheck = if (equippedShield2 != null) "[✔]" else "[ ]"
    armorEquipRow.appendChild(ctx.span("export-equip-row__check", "$shieldCheck ${t("export.shield")}"))
    equipPanel.appendChild(armorEquipRow)

    // Row 2: Arma Equipada
    val equippedWeapon = weapons.firstOrNull { it.isEquipped }
    val weaponEquipRow = ctx.div("export-equip-row")
    weaponEquipRow.appendChild(ctx.span("export-equip-row__label", "${t("export.equippedWeapon")}:"))
    weaponEquipRow.appendChild(ctx.span("export-equip-row__val", equippedWeapon?.name ?: "—"))
    equipPanel.appendChild(weaponEquipRow)

    // Row 3: Moedas | Consumíveis (side by side, each as a vertical list)
    val moneyConsRow = ctx.div("export-equip-money-row")

    val coinsCol = ctx.div("export-equip-col")
    coinsCol.appendChild(ctx.span("export-equip-col__label", "${t("export.coins")}:"))
    val money = ctx.money
    data class CoinInfo(val icon: String, val abbr: String, val qty: Int)
    listOf(
        CoinInfo("🟤", t("coin.copper.abbr"), money.copper),
        CoinInfo("⚪", t("coin.silver.abbr"), money.silver),
        CoinInfo("🔵", t("coin.electrum.abbr"), money.electrum),
        CoinInfo("🟡", t("coin.gold.abbr"), money.gold),
        CoinInfo("⬜", t("coin.platinum.abbr"), money.platinum)
    ).filter { it.qty > 0 }.forEach { coin ->
        val row = ctx.div("export-equip-col__row")
        row.appendChild(ctx.span("export-equip-col__icon", coin.icon))
        row.appendChild(ctx.span("export-equip-col__item", "${coin.qty} ${coin.abbr}"))
        coinsCol.appendChild(row)
    }
    moneyConsRow.appendChild(coinsCol)

    val consCol = ctx.div("export-equip-col")
    consCol.appendChild(ctx.span("export-equip-col__label", "${t("export.consumables")}:"))
    val consumables = ctx.consumables
    if (consumables.isEmpty()) {
        consCol.appendChild(ctx.span("export-equip-col__item", "—"))
    } else {
        consumables.forEach { c ->
            val row = ctx.div("export-equip-col__row")
            row.appendChild(ctx.span("export-equip-col__item", c.name))
            row.appendChild(ctx.span("export-equip-col__qty", "×${c.quantity}"))
            consCol.appendChild(row)
        }
    }
    moneyConsRow.appendChild(consCol)
    equipPanel.appendChild(moneyConsRow)

    // Row 4: Itens Mágicos Sintonizados
    val attunedItems = ctx.magicItems.filter { it.isSynched }
    if (attunedItems.isNotEmpty()) {
        val attunedRow = ctx.div("export-equip-row")
        attunedRow.appendChild(ctx.span("export-equip-row__label", "${t("export.attunedItems")}:"))
        attunedRow.appendChild(ctx.span("export-equip-row__val", attunedItems.joinToString(", ") { it.name }))
        equipPanel.appendChild(attunedRow)
    }

    leftCol.appendChild(equipPanel)
    body.appendChild(leftCol)

    page.appendChild(body)

    page.appendChild(ctx.div("export-panel export-panel--bottom"))
    return page
}
