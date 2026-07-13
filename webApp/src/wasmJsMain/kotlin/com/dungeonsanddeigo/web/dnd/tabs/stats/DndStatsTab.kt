package com.dungeonsanddeigo.web.dnd.tabs.stats

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.dnd.rules.formatModifier
import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tStat
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndBaseStats
import com.dungeonsanddeigo.model.DndSkills
import com.dungeonsanddeigo.model.DndSkillEntry
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.autoSaveIndicator.AutoSaveIndicator
import kotlinx.browser.document
import org.w3c.dom.*

fun renderDndStatsTab(character: Character, container: HTMLDivElement) {
    val stats = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
    val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
    val proficiency = calcProficiency(totalLevel)

    fun makeBox(titleKey: String): HTMLDivElement {
        val box = document.createElement("div") as HTMLDivElement
        box.className = "main-tab-box"
        val title = document.createElement("h3") as HTMLHeadingElement
        title.className = "main-tab-box-title"
        title.textContent = t(titleKey)
        box.appendChild(title)
        return box
    }

    // --- Box 1: Base Stats ---
    val statsBox = makeBox("stats.boxBaseStats")

    val row = document.createElement("div") as HTMLDivElement
    row.className = "stats-row"
    statsBox.appendChild(row)

    data class StatRow(val label: String, val value: Int?)

    val statDefs = listOf(
        StatRow("Str", stats.strValue),
        StatRow("Dex", stats.dexValue),
        StatRow("Con", stats.conValue),
        StatRow("Int", stats.intValue),
        StatRow("Wis", stats.wisValue),
        StatRow("Cha", stats.chaValue)
    )

    val valueInputs = mutableListOf<HTMLInputElement>()

    statDefs.forEach { stat ->
        val col = document.createElement("div") as HTMLDivElement
        col.className = "stats-col"

        val lbl = document.createElement("span") as HTMLSpanElement
        lbl.className = "stats-col-label"
        lbl.textContent = tStat(stat.label)
        col.appendChild(lbl)

        val modLabel = document.createElement("span") as HTMLSpanElement
        modLabel.className = "stats-col-mod"
        modLabel.textContent = formatModifier(stat.value)
        col.appendChild(modLabel)

        val valInput = document.createElement("input") as HTMLInputElement
        valInput.type = "number"
        valInput.min = "0"
        valInput.max = "20"
        valInput.value = stat.value?.toString() ?: ""
        valInput.className = "stats-col-input"
        col.appendChild(valInput)

        valInput.addEventListener("input", {
            modLabel.textContent = formatModifier(valInput.value.toIntOrNull())
        })

        valueInputs.add(valInput)
        row.appendChild(col)
    }

    // --- Box 2: Life & Numbers ---
    val lifeBox = makeBox("stats.boxLifeNumbers")

    val extras = document.createElement("div") as HTMLDivElement
    extras.className = "stats-extras"
    lifeBox.appendChild(extras)

    fun addExtraNumber(label: String, value: Int?): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"
        input.value = value?.toString() ?: ""
        col.appendChild(input)
        extras.appendChild(col)
        return input
    }

    fun addExtraLabel(label: String, value: Int?): HTMLSpanElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val span = document.createElement("span") as HTMLSpanElement
        span.className = "stats-extra-value"
        span.textContent = value?.toString() ?: ""
        col.appendChild(span)
        extras.appendChild(col)
        return span
    }

    fun addExtraCheckbox(label: String, checked: Boolean): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        val input = document.createElement("input") as HTMLInputElement
        input.type = "checkbox"
        input.checked = checked
        input.className = "stats-extra-checkbox"
        lbl.appendChild(input)
        lbl.append(label)
        col.appendChild(lbl)
        extras.appendChild(col)
        return input
    }

    // col 1: maxLife, armorClass, proficiency, initiative
    // col 2: disarmedDice, speed, vision, darkVision
    val maxLifeInput = addExtraNumber(t("stats.maxLife"), stats.maxLife)

    val disarmedCol = document.createElement("div") as HTMLDivElement
    val disarmedLbl = document.createElement("label") as HTMLLabelElement
    disarmedLbl.textContent = t("combat.disarmedDice")
    disarmedCol.appendChild(disarmedLbl)
    val disarmedSelect = document.createElement("select") as HTMLSelectElement
    DndBaseStats.disarmedDiceOptions.forEach { opt ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = opt; o.textContent = opt; disarmedSelect.appendChild(o)
    }
    disarmedSelect.value = stats.disarmedDice
    disarmedCol.appendChild(disarmedSelect)
    extras.appendChild(disarmedCol)

    val emptyArmorClassInput = addExtraNumber(t("stats.noArmorAC"), stats.emptyArmorClass)
    val speedInput = addExtraNumber(t("stats.speed"), stats.speed).also { input ->
        val row = document.createElement("div") as HTMLDivElement
        row.className = "stats-extras-input-row"
        input.parentElement?.replaceChild(row, input)
        row.appendChild(input)
        val unit = document.createElement("span") as HTMLSpanElement
        unit.textContent = "m"
        row.appendChild(unit)
    }

    val proficiencyLabel = addExtraLabel(t("stats.proficiency"), proficiency)
    val visionInput = addExtraNumber(t("stats.vision"), stats.vision).also { input ->
        val row = document.createElement("div") as HTMLDivElement
        row.className = "stats-extras-input-row"
        input.parentElement?.replaceChild(row, input)
        row.appendChild(input)
        val unit = document.createElement("span") as HTMLSpanElement
        unit.textContent = "m"
        row.appendChild(unit)
    }

    val dexMod = stats.dexValue?.let { calcModifier(it) }
    val initiativeLabel = addExtraLabel(t("stats.initiative"), dexMod)
    valueInputs[1].addEventListener("input", {
        val v = valueInputs[1].value.toIntOrNull()
        val mod = v?.let { calcModifier(it) }
        initiativeLabel.textContent = if (mod != null) { if (mod >= 0) "+$mod" else "$mod" } else ""
    })

    val darkVisionInput = addExtraCheckbox(t("stats.darkVision"), stats.hasDarkVision)

    // --- Box 3: Resistance Tests ---
    val resBox = makeBox("stats.resistanceTests")
    resBox.className += " stats-res-box"

    data class ResRow(val label: String, val hasRes: Boolean, val statIndex: Int)

    val resRows = listOf(
        ResRow("Str", stats.hasStrRes, 0),
        ResRow("Dex", stats.hasDexRes, 1),
        ResRow("Con", stats.hasConRes, 2),
        ResRow("Int", stats.hasIntRes, 3),
        ResRow("Wis", stats.hasWisRes, 4),
        ResRow("Cha", stats.hasChaRes, 5)
    )

    val resCheckboxes = mutableListOf<HTMLInputElement>()
    val resValueLabels = mutableListOf<HTMLSpanElement>()

    resRows.forEach { r ->
        val line = document.createElement("div") as HTMLDivElement
        line.className = "stats-res-row"

        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.checked = r.hasRes
        line.appendChild(cb)

        val name = document.createElement("span") as HTMLSpanElement
        name.textContent = tStat(r.label)
        name.className = "stats-res-name"
        line.appendChild(name)

        val valLabel = document.createElement("span") as HTMLSpanElement
        valLabel.className = "stats-res-value"
        fun calcResValue(): String {
            val statVal = valueInputs[r.statIndex].value.toIntOrNull()
            val mod = statVal?.let { calcModifier(it) } ?: 0
            val total = if (cb.checked) mod + proficiency else mod
            return if (total >= 0) "+$total" else "$total"
        }
        valLabel.textContent = calcResValue()
        line.appendChild(valLabel)

        cb.addEventListener("change", { valLabel.textContent = calcResValue() })

        resCheckboxes.add(cb)
        resValueLabels.add(valLabel)
        resBox.appendChild(line)
    }

    valueInputs.forEachIndexed { i, input ->
        input.addEventListener("input", {
            val statVal = input.value.toIntOrNull()
            val mod = statVal?.let { calcModifier(it) } ?: 0
            val total = if (resCheckboxes[i].checked) mod + proficiency else mod
            resValueLabels[i].textContent = if (total >= 0) "+$total" else "$total"
        })
    }

    // Auto-save
    val autoSaveIndicator = AutoSaveIndicator(container)

    fun autoSave() {
        autoSaveIndicator.schedule {
            fun v(i: Int) = valueInputs[i].value.toIntOrNull()
            fun m(i: Int) = v(i)?.let { calcModifier(it) }
            val updated = DndBaseStats(
                characterId = character.id,
                strValue = v(0), strMod = m(0),
                dexValue = v(1), dexMod = m(1),
                conValue = v(2), conMod = m(2),
                intValue = v(3), intMod = m(3),
                wisValue = v(4), wisMod = m(4),
                chaValue = v(5), chaMod = m(5),
                proficiency = calcProficiency(totalLevel),
                maxLife = maxLifeInput.value.toIntOrNull(),
                vision = visionInput.value.toIntOrNull(),
                hasDarkVision = darkVisionInput.checked,
                speed = speedInput.value.toIntOrNull(),
                emptyArmorClass = emptyArmorClassInput.value.toIntOrNull(),
                disarmedDice = disarmedSelect.value,
                hasStrRes = resCheckboxes[0].checked,
                hasDexRes = resCheckboxes[1].checked,
                hasConRes = resCheckboxes[2].checked,
                hasIntRes = resCheckboxes[3].checked,
                hasWisRes = resCheckboxes[4].checked,
                hasChaRes = resCheckboxes[5].checked
            )
            Repos.baseStats.save(updated)
        }
    }

    container.addEventListener("input", { autoSave() })
    container.addEventListener("change", { autoSave() })

    // --- Layout: left column (boxes 1-3) + right column (box 4) ---
    val midGrid = document.createElement("div") as HTMLDivElement
    midGrid.className = "stats-mid-grid"
    midGrid.appendChild(lifeBox)
    midGrid.appendChild(resBox)

    val leftColumn = document.createElement("div") as HTMLDivElement
    leftColumn.className = "stats-left-column"
    leftColumn.appendChild(statsBox)
    leftColumn.appendChild(midGrid)

    val skillsBox = buildDndSkillsBox(character, container, autoSaveIndicator)

    val outerGrid = document.createElement("div") as HTMLDivElement
    outerGrid.className = "stats-outer-grid"
    outerGrid.appendChild(leftColumn)
    outerGrid.appendChild(skillsBox)

    container.appendChild(outerGrid)
}

private val statAbbreviations = listOf("Str", "Dex", "Con", "Int", "Wis", "Cha")

private fun buildDndSkillsBox(character: Character, container: HTMLDivElement, autoSaveIndicator: AutoSaveIndicator): HTMLDivElement {
    val skills = Repos.skills.getByCharacterId(character.id) ?: DndSkills(characterId = character.id)
    val stats = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
    val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
    val proficiency = calcProficiency(totalLevel)

    fun statModValue(abbr: String): Int {
        val v = when (abbr) {
            "Str" -> stats.strValue
            "Dex" -> stats.dexValue
            "Con" -> stats.conValue
            "Int" -> stats.intValue
            "Wis" -> stats.wisValue
            "Cha" -> stats.chaValue
            else -> null
        }
        return v?.let { calcModifier(it) } ?: 0
    }

    val box = document.createElement("div") as HTMLDivElement
    box.className = "stats-skills-box"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.className = "main-tab-box-title"
    title.textContent = t("stats.skills")
    box.appendChild(title)

    data class SkillRow(
        val name: String,
        val checkbox: HTMLInputElement,
        val modSelect: HTMLSelectElement,
        val addInput: HTMLInputElement,
        val totalLabel: HTMLSpanElement
    )

    val skillRows = mutableListOf<SkillRow>()

    val skillsGrid = document.createElement("div") as HTMLDivElement
    skillsGrid.className = "stats-skills-grid"
    box.appendChild(skillsGrid)

    DndSkills.skillNames.forEach { skillName ->
        val entry = skills.getEntry(skillName)
        val defaultMod = DndSkills.defaultModifiers[skillName] ?: "Str"
        val currentMod = entry.modifier.ifEmpty { defaultMod }

        val line = document.createElement("div") as HTMLDivElement
        line.className = "stats-skill-row"

        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.checked = entry.isTrained
        line.appendChild(cb)

        val nameSpan = document.createElement("span") as HTMLSpanElement
        nameSpan.textContent = t("skill.$skillName")
        nameSpan.className = "stats-skill-name"
        line.appendChild(nameSpan)

        val modSelect = document.createElement("select") as HTMLSelectElement
        modSelect.className = "stats-skill-mod"
        statAbbreviations.forEach { abbr ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = abbr
            opt.textContent = tStat(abbr)
            modSelect.appendChild(opt)
        }
        modSelect.value = currentMod
        line.appendChild(modSelect)

        val plusLabel = document.createElement("span") as HTMLSpanElement
        plusLabel.textContent = "+"
        line.appendChild(plusLabel)

        val addInput = document.createElement("input") as HTMLInputElement
        addInput.type = "number"
        addInput.value = entry.additionalValue.toString()
        addInput.className = "stats-skill-add"
        line.appendChild(addInput)

        val totalLabel = document.createElement("span") as HTMLSpanElement
        totalLabel.className = "stats-skill-total"

        fun calcTotal(): Int {
            val mod = statModValue(modSelect.value)
            val prof = if (cb.checked) proficiency else 0
            val add = addInput.value.toIntOrNull() ?: 0
            return mod + prof + add
        }

        fun updateTotal() {
            val total = calcTotal()
            totalLabel.textContent = "= ${if (total >= 0) "+$total" else "$total"}"
        }
        updateTotal()

        line.appendChild(totalLabel)

        cb.addEventListener("change", { updateTotal() })
        modSelect.addEventListener("change", { updateTotal() })
        addInput.addEventListener("input", { updateTotal() })

        skillRows.add(SkillRow(skillName, cb, modSelect, addInput, totalLabel))
        skillsGrid.appendChild(line)
    }

    container.appendChild(box)

    fun autoSave() {
        autoSaveIndicator.schedule {
            var updated = skills.copy(characterId = character.id)
            skillRows.forEach { r ->
                val mod = statModValue(r.modSelect.value)
                val prof = if (r.checkbox.checked) proficiency else 0
                val add = r.addInput.value.toIntOrNull() ?: 0
                val entry = DndSkillEntry(
                    isTrained = r.checkbox.checked,
                    modifier = r.modSelect.value,
                    additionalValue = add,
                    totalValue = mod + prof + add
                )
                updated = updated.withEntry(r.name, entry)
            }
            Repos.skills.save(updated)
        }
    }

    container.addEventListener("input", { autoSave() })
    container.addEventListener("change", { autoSave() })

    return box
}
