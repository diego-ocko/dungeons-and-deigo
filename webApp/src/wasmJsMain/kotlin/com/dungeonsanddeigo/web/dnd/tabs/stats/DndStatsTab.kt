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

    // 6 columns: one per stat, each column = Name / Modifier / Value
    val row = document.createElement("div") as HTMLDivElement
    row.style.setProperty("display", "grid")
    row.style.setProperty("grid-template-columns", "repeat(6, 1fr)")
    row.style.setProperty("gap", "16px")
    row.style.textAlign = "center"
    row.style.maxWidth = "600px"

    data class StatRow(val label: String, val value: Int?)

    val rows = listOf(
        StatRow("Str", stats.strValue),
        StatRow("Dex", stats.dexValue),
        StatRow("Con", stats.conValue),
        StatRow("Int", stats.intValue),
        StatRow("Wis", stats.wisValue),
        StatRow("Cha", stats.chaValue)
    )

    val valueInputs = mutableListOf<HTMLInputElement>()

    rows.forEach { stat ->
        val col = document.createElement("div") as HTMLDivElement
        col.style.display = "flex"
        col.style.setProperty("flex-direction", "column")
        col.style.setProperty("align-items", "center")
        col.style.setProperty("gap", "4px")

        // Stat name
        val lbl = document.createElement("span") as HTMLSpanElement
        lbl.textContent = tStat(stat.label)
        lbl.style.fontWeight = "bold"
        lbl.style.fontSize = "14px"
        col.appendChild(lbl)

        // Modifier label
        val modLabel = document.createElement("span") as HTMLSpanElement
        modLabel.textContent = formatModifier(stat.value)
        modLabel.style.fontSize = "20px"
        modLabel.style.fontWeight = "bold"
        col.appendChild(modLabel)

        // Value input
        val valInput = document.createElement("input") as HTMLInputElement
        valInput.type = "number"
        valInput.min = "0"
        valInput.max = "20"
        valInput.value = stat.value?.toString() ?: ""
        valInput.style.padding = "4px"
        valInput.style.width = "50px"
        valInput.style.textAlign = "center"
        col.appendChild(valInput)

        valInput.addEventListener("input", {
            modLabel.textContent = formatModifier(valInput.value.toIntOrNull())
        })

        valueInputs.add(valInput)
        row.appendChild(col)
    }

    container.appendChild(row)

    // Extra fields row
    val extras = document.createElement("div") as HTMLDivElement
    extras.style.setProperty("display", "grid")
    extras.style.setProperty("grid-template-columns", "1fr 1fr 1fr 1fr")
    extras.style.setProperty("gap", "12px")
    extras.style.marginTop = "16px"
    extras.style.maxWidth = "600px"

    fun addExtraNumber(label: String, value: Int?): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        lbl.style.fontSize = "14px"
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"
        input.value = value?.toString() ?: ""
        input.style.padding = "4px"
        input.style.width = "100%"
        col.appendChild(input)
        extras.appendChild(col)
        return input
    }

    fun addExtraLabel(label: String, value: Int?): HTMLSpanElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        lbl.style.fontSize = "14px"
        col.appendChild(lbl)
        val span = document.createElement("span") as HTMLSpanElement
        span.textContent = value?.toString() ?: ""
        span.style.fontSize = "20px"
        span.style.fontWeight = "bold"
        span.style.display = "block"
        col.appendChild(span)
        extras.appendChild(col)
        return span
    }

    fun addExtraCheckbox(label: String, checked: Boolean): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.style.fontWeight = "bold"
        lbl.style.fontSize = "14px"
        val input = document.createElement("input") as HTMLInputElement
        input.type = "checkbox"
        input.checked = checked
        input.style.marginRight = "6px"
        lbl.appendChild(input)
        lbl.append(label)
        col.appendChild(lbl)
        extras.appendChild(col)
        return input
    }

    val proficiencyLabel = addExtraLabel(t("stats.proficiency"), proficiency)
    val maxLifeInput = addExtraNumber(t("stats.maxLife"), stats.maxLife)
    val visionInput = addExtraNumber(t("stats.vision"), stats.vision)
    val darkVisionInput = addExtraCheckbox(t("stats.darkVision"), stats.hasDarkVision)
    val speedInput = addExtraNumber(t("stats.speed"), stats.speed)
    val emptyArmorClassInput = addExtraNumber(t("stats.noArmorAC"), stats.emptyArmorClass)

    // Disarmed Attack Dice
    val disarmedCol = document.createElement("div") as HTMLDivElement
    val disarmedLbl = document.createElement("label") as HTMLLabelElement
    disarmedLbl.textContent = t("combat.disarmedDice")
    disarmedLbl.style.fontWeight = "bold"
    disarmedLbl.style.fontSize = "14px"
    disarmedCol.appendChild(disarmedLbl)
    val disarmedSelect = document.createElement("select") as HTMLSelectElement
    disarmedSelect.style.padding = "4px"
    disarmedSelect.style.width = "100%"
    DndBaseStats.disarmedDiceOptions.forEach { opt ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = opt; o.textContent = opt; disarmedSelect.appendChild(o)
    }
    disarmedSelect.value = stats.disarmedDice
    disarmedCol.appendChild(disarmedSelect)
    extras.appendChild(disarmedCol)

    // Initiative (read-only, equals Dex modifier)
    val dexMod = stats.dexValue?.let { calcModifier(it) }
    val initiativeLabel = addExtraLabel(t("stats.initiative"), dexMod)
    // Update initiative when Dex value changes
    valueInputs[1].addEventListener("input", {
        val v = valueInputs[1].value.toIntOrNull()
        val mod = v?.let { calcModifier(it) }
        initiativeLabel.textContent = if (mod != null) { if (mod >= 0) "+$mod" else "$mod" } else ""
    })

    container.appendChild(extras)

    // Resistance Tests box
    val resBox = document.createElement("div") as HTMLDivElement
    resBox.style.border = "1px solid #ccc"
    resBox.style.borderRadius = "8px"
    resBox.style.padding = "12px"
    resBox.style.marginTop = "16px"
    resBox.style.maxWidth = "300px"

    val resTitle = document.createElement("h3") as HTMLHeadingElement
    resTitle.textContent = t("stats.resistanceTests")
    resTitle.style.marginTop = "0"
    resBox.appendChild(resTitle)

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
        valLabel.style.fontSize = "16px"
        line.appendChild(valLabel)

        cb.addEventListener("change", {
            valLabel.textContent = calcResValue()
        })

        resCheckboxes.add(cb)
        resValueLabels.add(valLabel)
        resBox.appendChild(line)
    }

    // Update resistance values when stat values change
    valueInputs.forEachIndexed { i, input ->
        input.addEventListener("input", {
            resValueLabels[i].textContent = run {
                val statVal = input.value.toIntOrNull()
                val mod = statVal?.let { calcModifier(it) } ?: 0
                val total = if (resCheckboxes[i].checked) mod + proficiency else mod
                if (total >= 0) "+$total" else "$total"
            }
        })
    }

    container.appendChild(resBox)

    // Auto-save status
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

    // Skills section
    renderDndSkillsBox(character, container)
}

private val statAbbreviations = listOf("Str", "Dex", "Con", "Int", "Wis", "Cha")

private fun renderDndSkillsBox(character: Character, container: HTMLDivElement) {
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

    DndSkills.skillNames.forEach { skillName ->
        val entry = skills.getEntry(skillName)
        val defaultMod = DndSkills.defaultModifiers[skillName] ?: "Str"
        val currentMod = entry.modifier.ifEmpty { defaultMod }

        val line = document.createElement("div") as HTMLDivElement
        line.className = "stats-skill-row"

        // Trained checkbox
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.checked = entry.isTrained
        line.appendChild(cb)

        // Skill name
        val nameSpan = document.createElement("span") as HTMLSpanElement
        nameSpan.textContent = t("skill.$skillName")
        nameSpan.className = "stats-skill-name"
        line.appendChild(nameSpan)

        // Modifier select
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

        // Plus label
        val plusLabel = document.createElement("span") as HTMLSpanElement
        plusLabel.textContent = "+"
        line.appendChild(plusLabel)

        // Additional value
        val addInput = document.createElement("input") as HTMLInputElement
        addInput.type = "number"
        addInput.value = entry.additionalValue.toString()
        addInput.className = "stats-skill-add"
        line.appendChild(addInput)

        // Equals + total
        val totalLabel = document.createElement("span") as HTMLSpanElement
        totalLabel.className = "stats-skill-total"

        fun calcTotal(): Int {
            val mod = statModValue(modSelect.value)
            val prof = if (cb.checked) proficiency else 0
            val add = addInput.value.toIntOrNull() ?: 0
            return mod + prof + add
        }

        fun updateTotal() {
            val t = calcTotal()
            totalLabel.textContent = "= ${if (t >= 0) "+$t" else "$t"}"
        }
        updateTotal()

        line.appendChild(totalLabel)

        cb.addEventListener("change", { updateTotal() })
        modSelect.addEventListener("change", { updateTotal() })
        addInput.addEventListener("input", { updateTotal() })

        skillRows.add(SkillRow(skillName, cb, modSelect, addInput, totalLabel))
        box.appendChild(line)
    }

    container.appendChild(box)

    // Auto-save
    val autoSaveIndicator = AutoSaveIndicator(container)

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
}
