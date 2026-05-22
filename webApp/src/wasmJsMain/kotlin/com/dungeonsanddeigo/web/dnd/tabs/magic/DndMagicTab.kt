package com.dungeonsanddeigo.web.dnd.tabs.magic

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndBaseStats
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.spell.showSpellModal
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun renderDndMagicTab(character: Character, container: HTMLDivElement) {
    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
    val stats = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
    val proficiency = calcProficiency(totalLevel)

    // Determine spellcasting classes
    data class SpellcasterInfo(
        val className: String,
        val rawClassName: String,
        val subClassName: String?,
        val level: Int,
        val ability: String,
        val abilityMod: Int,
        val spellMod: Int,
        val spellDC: Int,
        val needsPreparation: Boolean,
        val ritualOnly: Boolean = false,
        val isCustom: Boolean = false
    )

    val casters = mutableListOf<SpellcasterInfo>()

    val mainClass = mainInfo?.mainClass
    val mainAbility = DungeonsAndDragons.spellcastingAbilityFor(mainClass, mainInfo?.mainSubClass)
    if (mainAbility != null && mainClass != null) {
        val mainSubClass = mainInfo?.mainSubClass
        val mainDisplayName = if (mainSubClass != null && mainSubClass in DungeonsAndDragons.subclassSpellcasting) "${tDnd("class", mainClass)} (${tDnd("subclass", mainSubClass)})" else tDnd("class", mainClass)
        val abilityMod = when (mainAbility) {
            "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
            "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
            "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
            else -> 0
        }
        casters.add(SpellcasterInfo(
            className = mainDisplayName,
            rawClassName = mainClass,
            subClassName = mainSubClass,
            level = mainInfo?.mainClassLevel ?: 1,
            ability = when (mainAbility) { "Cha" -> tStat("Charisma"); "Int" -> tStat("Intelligence"); "Wis" -> tStat("Wisdom"); else -> mainAbility },
            abilityMod = abilityMod,
            spellMod = abilityMod + proficiency,
            spellDC = abilityMod + proficiency + 8,
            needsPreparation = mainClass in DungeonsAndDragons.preparedCasters,
            ritualOnly = mainSubClass != null && mainSubClass in DungeonsAndDragons.ritualOnlySubclasses
        ))
    }

    val secClass = mainInfo?.secondaryClass
    val secAbility = DungeonsAndDragons.spellcastingAbilityFor(secClass, mainInfo?.secondarySubClass)
    if (secAbility != null && secClass != null) {
        val secSubClass = mainInfo?.secondarySubClass
        val secDisplayName = if (secSubClass != null && secSubClass in DungeonsAndDragons.subclassSpellcasting) "${tDnd("class", secClass)} (${tDnd("subclass", secSubClass)})" else tDnd("class", secClass)
        val abilityMod = when (secAbility) {
            "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
            "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
            "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
            else -> 0
        }
        casters.add(SpellcasterInfo(
            className = secDisplayName,
            rawClassName = secClass,
            subClassName = secSubClass,
            level = mainInfo?.secondaryClassLevel ?: 1,
            ability = when (secAbility) { "Cha" -> tStat("Charisma"); "Int" -> tStat("Intelligence"); "Wis" -> tStat("Wisdom"); else -> secAbility },
            abilityMod = abilityMod,
            spellMod = abilityMod + proficiency,
            spellDC = abilityMod + proficiency + 8,
            needsPreparation = secClass in DungeonsAndDragons.preparedCasters,
            ritualOnly = secSubClass != null && secSubClass in DungeonsAndDragons.ritualOnlySubclasses
        ))
    }

    // Handle custom classes (not in default list)
    fun isCustomClass(className: String?): Boolean = className != null && className !in DungeonsAndDragons.defaultClasses

    fun getCustomAbility(className: String): String? {
        return localStorage.getItem("dnd_custom_spell_ability_${character.id}_$className")
    }

    if (mainClass != null && isCustomClass(mainClass) && mainAbility == null) {
        val savedAbility = getCustomAbility(mainClass)
        if (savedAbility != null && savedAbility != "__none__") {
            val abilityMod = when (savedAbility) {
                "Str" -> stats.strValue?.let { calcModifier(it) } ?: 0
                "Dex" -> stats.dexValue?.let { calcModifier(it) } ?: 0
                "Con" -> stats.conValue?.let { calcModifier(it) } ?: 0
                "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                else -> 0
            }
            casters.add(SpellcasterInfo(
                className = mainClass,
                rawClassName = mainClass,
                subClassName = null,
                level = mainInfo?.mainClassLevel ?: 1,
                ability = savedAbility,
                abilityMod = abilityMod,
                spellMod = abilityMod + proficiency,
                spellDC = abilityMod + proficiency + 8,
                needsPreparation = false,
                isCustom = true
            ))
        }
    }

    if (secClass != null && isCustomClass(secClass) && secAbility == null) {
        val savedAbility = getCustomAbility(secClass)
        if (savedAbility != null && savedAbility != "__none__") {
            val abilityMod = when (savedAbility) {
                "Str" -> stats.strValue?.let { calcModifier(it) } ?: 0
                "Dex" -> stats.dexValue?.let { calcModifier(it) } ?: 0
                "Con" -> stats.conValue?.let { calcModifier(it) } ?: 0
                "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                else -> 0
            }
            casters.add(SpellcasterInfo(
                className = secClass,
                rawClassName = secClass,
                subClassName = null,
                level = mainInfo?.secondaryClassLevel ?: 1,
                ability = savedAbility,
                abilityMod = abilityMod,
                spellMod = abilityMod + proficiency,
                spellDC = abilityMod + proficiency + 8,
                needsPreparation = false,
                isCustom = true
            ))
        }
    }

    // Show ability selector for custom classes without saved ability
    val customClassesNeedingAbility = mutableListOf<String>()
    if (mainClass != null && isCustomClass(mainClass) && mainAbility == null && getCustomAbility(mainClass) == null) {
        customClassesNeedingAbility.add(mainClass)
    }
    if (secClass != null && isCustomClass(secClass) && secAbility == null && getCustomAbility(secClass) == null) {
        customClassesNeedingAbility.add(secClass)
    }

    customClassesNeedingAbility.forEach { className ->
        val selectorBox = document.createElement("div") as HTMLDivElement
        selectorBox.style.border = "1px solid #ccc"
        selectorBox.style.borderRadius = "8px"
        selectorBox.style.padding = "12px"
        selectorBox.style.marginBottom = "16px"

        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = "$className - ${t("magic.selectAbility")}:"
        lbl.style.fontWeight = "bold"; lbl.style.marginRight = "8px"
        selectorBox.appendChild(lbl)

        val sel = document.createElement("select") as HTMLSelectElement
        sel.style.padding = "4px"
        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""; emptyOpt.textContent = t("magic.select")
        sel.appendChild(emptyOpt)
        listOf("Str", "Dex", "Con", "Int", "Wis", "Cha").forEach { ab ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = ab; opt.textContent = ab
            sel.appendChild(opt)
        }
        selectorBox.appendChild(sel)

        val noneOpt = document.createElement("option") as HTMLOptionElement
        noneOpt.value = "__none__"; noneOpt.textContent = t("magic.noSpellcastingOpt")
        sel.appendChild(noneOpt)

        sel.addEventListener("change", {
            if (sel.value == "__none__") {
                localStorage.setItem("dnd_custom_spell_ability_${character.id}_$className", "__none__")
            } else if (sel.value.isNotEmpty()) {
                localStorage.setItem("dnd_custom_spell_ability_${character.id}_$className", sel.value)
            }
            container.innerHTML = ""
            renderDndMagicTab(character, container)
        })

        container.appendChild(selectorBox)
    }

    // Check for custom classes that chose "No Spellcasting" — show change button
    val customClassesWithNone = mutableListOf<String>()
    if (mainClass != null && isCustomClass(mainClass) && getCustomAbility(mainClass) == "__none__") {
        customClassesWithNone.add(mainClass)
    }
    if (secClass != null && isCustomClass(secClass) && getCustomAbility(secClass) == "__none__") {
        customClassesWithNone.add(secClass)
    }

    if (casters.isEmpty() && customClassesNeedingAbility.isEmpty()) {
        val noMagic = document.createElement("p") as HTMLParagraphElement
        noMagic.textContent = t("magic.noSpellcasting")
        noMagic.style.color = "#999"
        container.appendChild(noMagic)

        customClassesWithNone.forEach { className ->
            val changeBtn = document.createElement("button") as HTMLButtonElement
            changeBtn.textContent = "\u270E ${t("magic.changeAbility")} ($className)"
            changeBtn.style.fontSize = "11px"
            changeBtn.style.marginTop = "8px"
            changeBtn.style.display = "block"
            changeBtn.addEventListener("click", {
                localStorage.removeItem("dnd_custom_spell_ability_${character.id}_$className")
                container.innerHTML = ""
                renderDndMagicTab(character, container)
            })
            container.appendChild(changeBtn)
        }

        return
    }

    // Header for each caster
    casters.forEach { caster ->
        val box = document.createElement("div") as HTMLDivElement
        box.style.border = "1px solid #ccc"
        box.style.borderRadius = "8px"
        box.style.padding = "12px"
        box.style.marginBottom = "16px"

        val titleRow = document.createElement("div") as HTMLDivElement
        titleRow.style.display = "flex"
        titleRow.style.justifyContent = "space-between"
        titleRow.style.alignItems = "center"
        titleRow.style.marginBottom = "10px"

        val titleEl = document.createElement("h4") as HTMLHeadingElement
        titleEl.textContent = "${caster.className} - ${t("magic.spellcasting")}"
        titleEl.style.margin = "0"
        titleRow.appendChild(titleEl)

        if (caster.isCustom) {
            val changeBtn = document.createElement("button") as HTMLButtonElement
            changeBtn.textContent = "\u270E " + t("magic.changeAbility")
            changeBtn.style.fontSize = "11px"
            changeBtn.addEventListener("click", {
                localStorage.removeItem("dnd_custom_spell_ability_${character.id}_${caster.className}")
                container.innerHTML = ""
                renderDndMagicTab(character, container)
            })
            titleRow.appendChild(changeBtn)
        }

        box.appendChild(titleRow)

        if (caster.ritualOnly) {
            val ritualMsg = document.createElement("p") as HTMLParagraphElement
            ritualMsg.textContent = "\u26A0\uFE0F " + t("magic.ritualOnly")
            ritualMsg.style.color = "#c00"
            ritualMsg.style.fontSize = "13px"
            ritualMsg.style.margin = "0 0 10px 0"
            box.appendChild(ritualMsg)
        }

        val grid = document.createElement("div") as HTMLDivElement
        grid.style.setProperty("display", "grid")
        grid.style.setProperty("grid-template-columns", "repeat(auto-fit, minmax(120px, 1fr))")
        grid.style.setProperty("gap", "12px")
        grid.style.textAlign = "center"

        fun addStat(label: String, value: String) {
            val col = document.createElement("div") as HTMLDivElement
            val valEl = document.createElement("div") as HTMLDivElement
            valEl.textContent = value
            valEl.style.fontSize = "24px"; valEl.style.fontWeight = "bold"
            col.appendChild(valEl)
            val lblEl = document.createElement("div") as HTMLDivElement
            lblEl.textContent = label
            lblEl.style.fontSize = "11px"; lblEl.style.color = "#666"
            col.appendChild(lblEl)
            grid.appendChild(col)
        }

        addStat(t("magic.ability"), caster.ability)
        addStat(t("magic.spellMod"), if (caster.spellMod >= 0) "+${caster.spellMod}" else "${caster.spellMod}")
        addStat(t("magic.spellDC"), caster.spellDC.toString())

        val casterSpells = Repos.spell.getByCharacterId(character.id).filter { it.originClass == caster.className }
        val cantripsCount = casterSpells.count { it.circle == "Cantrip" }
        val spellsCount = casterSpells.count { it.circle != "Cantrip" }
        val preparedCount = casterSpells.count { it.isPrepared && it.circle != "Cantrip" }

        addStat(t("magic.cantripsKnown"), cantripsCount.toString())
        addStat(t("magic.spellsKnown"), spellsCount.toString())
        if (caster.needsPreparation) {
            addStat(t("magic.prepared"), preparedCount.toString())
        }

        box.appendChild(grid)

        // Spell Slots
        val slots = DungeonsAndDragons.spellSlotsFor(caster.rawClassName, caster.subClassName, caster.level)
        if (slots.isNotEmpty()) {
            val slotsDiv = document.createElement("div") as HTMLDivElement
            slotsDiv.style.display = "flex"
            slotsDiv.style.setProperty("gap", "12px")
            slotsDiv.style.marginTop = "10px"
            slotsDiv.style.setProperty("flex-wrap", "wrap")

            slots.forEachIndexed { idx, totalSlots ->
                val circleNum = idx + 1
                val slotKey = "dnd_spell_slots_${character.id}_${caster.rawClassName}_$circleNum"
                val usedSlots = localStorage.getItem(slotKey)?.toIntOrNull() ?: 0

                val slotCol = document.createElement("div") as HTMLDivElement
                slotCol.style.textAlign = "center"
                slotCol.style.fontSize = "12px"

                val slotLabel = document.createElement("div") as HTMLDivElement
                slotLabel.textContent = "${circleNum}\u00BA"
                slotLabel.style.fontWeight = "bold"
                slotCol.appendChild(slotLabel)

                val slotValue = document.createElement("div") as HTMLDivElement
                slotValue.textContent = "$usedSlots / $totalSlots"
                slotValue.style.color = if (usedSlots >= totalSlots) "#c00" else "#333"
                slotCol.appendChild(slotValue)

                slotsDiv.appendChild(slotCol)
            }

            box.appendChild(slotsDiv)
        }

        container.appendChild(box)
    }

    // Add Spell button
    val addSpellBtn = document.createElement("button") as HTMLButtonElement
    addSpellBtn.textContent = "\u2728 " + t("magic.addSpell")
    addSpellBtn.style.marginBottom = "16px"
    addSpellBtn.addEventListener("click", {
        showSpellModal(character, null) {
            container.innerHTML = ""
            renderDndMagicTab(character, container)
        }
    })
    container.appendChild(addSpellBtn)

    // Spell list by circle
    val allSpells = Repos.spell.getByCharacterId(character.id)
    val spellsByCircle = allSpells.groupBy { it.circle }

    val spellGrid = document.createElement("div") as HTMLDivElement
    spellGrid.style.setProperty("display", "grid")
    spellGrid.style.setProperty("grid-template-columns", "repeat(auto-fill, minmax(250px, 1fr))")
    spellGrid.style.setProperty("gap", "12px")

    com.dungeonsanddeigo.model.DndSpell.circles.forEach { circle ->
        val spells = spellsByCircle[circle] ?: return@forEach
        val sorted = spells.sortedByDescending { it.isPrepared }

        val col = document.createElement("div") as HTMLDivElement
        col.style.border = "1px solid #ccc"
        col.style.borderRadius = "8px"
        col.style.padding = "10px"

        val colTitle = document.createElement("h5") as HTMLHeadingElement
        colTitle.textContent = if (circle == "Cantrip") t("magic.cantrips") else tCircle(circle)
        colTitle.style.margin = "0 0 8px 0"
        col.appendChild(colTitle)

        sorted.forEach { spell ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"
            row.style.fontSize = "12px"

            // Line 1: prepared + name + school + buttons
            val line1 = document.createElement("div") as HTMLDivElement
            line1.style.display = "flex"
            line1.style.justifyContent = "space-between"
            line1.style.alignItems = "center"

            val nameDiv = document.createElement("div") as HTMLDivElement
            val isCantrip = spell.circle == "Cantrip"
            val classUsesPrepared = spell.originClass in DungeonsAndDragons.preparedCasters
            val showPrepared = isCantrip || !classUsesPrepared || spell.isPrepared
            val prepIcon = if (showPrepared) "\u2705 " else "\u2B1C "
            val schoolEmoji = when (spell.school) {
                "Abjuration" -> "\uD83D\uDEE1\uFE0F"
                "Conjuration" -> "\u2728"
                "Divination" -> "\uD83D\uDD2E"
                "Enchantment" -> "\uD83D\uDCAB"
                "Evocation" -> "\uD83D\uDD25"
                "Illusion" -> "\uD83C\uDF00"
                "Necromancy" -> "\u2620\uFE0F"
                "Transmutation" -> "\u2699\uFE0F"
                else -> ""
            }
            nameDiv.textContent = "$prepIcon${spell.name} $schoolEmoji"
            nameDiv.style.fontWeight = if (showPrepared) "bold" else "normal"
            line1.appendChild(nameDiv)

            val btns = document.createElement("div") as HTMLDivElement
            btns.style.display = "flex"
            btns.style.setProperty("gap", "2px")

            val editBtn = document.createElement("button") as HTMLButtonElement
            editBtn.textContent = "\u270E"
            editBtn.style.fontSize = "10px"
            editBtn.addEventListener("click", {
                showSpellModal(character, spell) {
                    container.innerHTML = ""
                    renderDndMagicTab(character, container)
                }
            })
            btns.appendChild(editBtn)

            val delBtn = document.createElement("button") as HTMLButtonElement
            delBtn.textContent = "\u2716"
            delBtn.style.fontSize = "10px"; delBtn.style.color = "red"
            delBtn.addEventListener("click", {
                Repos.spell.delete(character.id, spell.id)
                container.innerHTML = ""
                renderDndMagicTab(character, container)
            })
            btns.appendChild(delBtn)

            line1.appendChild(btns)
            row.appendChild(line1)

            // Line 2: Origin + Casting Time + Duration + Range + Ritual
            val line2 = document.createElement("div") as HTMLDivElement
            line2.style.color = "#666"
            line2.style.fontSize = "11px"
            line2.style.marginTop = "2px"
            val originStr = "${tDnd("class", spell.originClass)} (${tOriginLevel(spell.originLevel)})"
            val rangeStr = if (spell.range.isNotEmpty()) "${spell.range}m" else ""
            val ritualStr = if (spell.canBeRitual) " | \uD83D\uDD2E ${t("magic.ritualShort")}" else ""
            val concStr = if (spell.needsConcentration) " | \uD83C\uDFAF ${t("magic.concShort")}" else ""
            line2.textContent = "$originStr | ${spell.castingTime} | ${spell.duration} | $rangeStr$ritualStr$concStr"
            row.appendChild(line2)

            col.appendChild(row)
        }

        spellGrid.appendChild(col)
    }

    container.appendChild(spellGrid)
}

