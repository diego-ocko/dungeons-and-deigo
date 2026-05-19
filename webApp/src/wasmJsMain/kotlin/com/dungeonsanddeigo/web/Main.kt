package com.dungeonsanddeigo.web

import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndBaseStats
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.model.availableSheetModels
import com.dungeonsanddeigo.model.DndMainInfo
import com.dungeonsanddeigo.model.DndSkills
import com.dungeonsanddeigo.model.DndSkillEntry
import com.dungeonsanddeigo.repository.WasmCharacterRepository
import com.dungeonsanddeigo.repository.WasmCustomClassRepository
import com.dungeonsanddeigo.repository.WasmDndBaseStatsRepository
import com.dungeonsanddeigo.repository.WasmDndMainInfoRepository
import com.dungeonsanddeigo.model.DndFeature
import com.dungeonsanddeigo.model.DndInventoryItem
import com.dungeonsanddeigo.model.DndMoney
import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.model.DndConsumable
import com.dungeonsanddeigo.model.DndWeapon
import com.dungeonsanddeigo.model.DndMagicItem
import com.dungeonsanddeigo.repository.WasmDndFeaturesRepository
import com.dungeonsanddeigo.repository.WasmDndArmorRepository
import com.dungeonsanddeigo.repository.WasmDndAppearanceRepository
import com.dungeonsanddeigo.repository.WasmDndBackstoryRepository
import com.dungeonsanddeigo.repository.WasmDndNoteRepository
import com.dungeonsanddeigo.repository.WasmDndSpellRepository
import com.dungeonsanddeigo.repository.WasmDndConsumableRepository
import com.dungeonsanddeigo.repository.WasmDndWeaponRepository
import com.dungeonsanddeigo.repository.WasmDndMagicItemRepository
import com.dungeonsanddeigo.repository.WasmDndInventoryRepository
import com.dungeonsanddeigo.repository.WasmDndMoneyRepository
import com.dungeonsanddeigo.repository.WasmDndSkillsRepository
import kotlinx.browser.window
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*
import org.w3c.files.FileReader

private val repo = WasmCharacterRepository()
private val dndMainInfoRepo = WasmDndMainInfoRepository()
private val dndBaseStatsRepo = WasmDndBaseStatsRepository()
private val dndSkillsRepo = WasmDndSkillsRepository()
private val dndFeaturesRepo = WasmDndFeaturesRepository()
private val dndInventoryRepo = WasmDndInventoryRepository()
private val dndMoneyRepo = WasmDndMoneyRepository()
private val dndArmorRepo = WasmDndArmorRepository()
private val dndConsumableRepo = WasmDndConsumableRepository()
private val dndWeaponRepo = WasmDndWeaponRepository()
private val dndMagicItemRepo = WasmDndMagicItemRepository()
private val customClassRepo = WasmCustomClassRepository()
private val dndAppearanceRepo = WasmDndAppearanceRepository()
private val dndBackstoryRepo = WasmDndBackstoryRepository()
private val dndNoteRepo = WasmDndNoteRepository()
private val dndSpellRepo = WasmDndSpellRepository()
private val app by lazy {
    val div = document.createElement("div") as HTMLDivElement
    document.body?.appendChild(div)
    div
}

private fun currentTimestamp(): String = js("new Date().toLocaleString()")

fun main() {
    showListScreen()
}

private fun renderTabContent(tabName: String, character: Character, container: HTMLDivElement) {
    container.innerHTML = ""
    if (character.sheetModel is DungeonsAndDragons) {
        when (tabName) {
            "Main" -> renderDndMainTab(character, container)
            "Stats" -> renderDndStatsTab(character, container)
            "Features" -> renderDndFeaturesTab(character, container)
            "Magic" -> renderDndMagicTab(character, container)
            "Inventory" -> renderDndInventoryTab(character, container)
            "Playing" -> renderDndPlayingTab(character, container)
            "Background" -> renderDndBackgroundTab(character, container)
            "Notes" -> renderDndNotesTab(character, container)
            else -> {
                val placeholder = document.createElement("p")
                placeholder.textContent = "$tabName content"
                container.appendChild(placeholder)
            }
        }
    } else {
        val placeholder = document.createElement("p")
        placeholder.textContent = "$tabName content"
        container.appendChild(placeholder)
    }
}

private fun calcModifier(value: Int): Int = (value - 10) / 2 - (if (value < 10 && value % 2 != 0) 1 else 0)

private fun formatModifier(value: Int?): String {
    if (value == null) return ""
    val mod = calcModifier(value)
    return if (mod >= 0) "+$mod" else "$mod"
}

private fun calcProficiency(totalLevel: Int): Int = when {
    totalLevel < 1 -> 2
    totalLevel <= 4 -> 2
    totalLevel <= 8 -> 3
    totalLevel <= 12 -> 4
    totalLevel <= 16 -> 5
    else -> 6
}

private fun renderDndStatsTab(character: Character, container: HTMLDivElement) {
    val stats = dndBaseStatsRepo.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
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
        lbl.textContent = stat.label
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

    val proficiencyLabel = addExtraLabel("Proficiency", proficiency)
    val maxLifeInput = addExtraNumber("Max Life", stats.maxLife)
    val visionInput = addExtraNumber("Vision", stats.vision)
    val darkVisionInput = addExtraCheckbox("Dark Vision", stats.hasDarkVision)
    val speedInput = addExtraNumber("Speed", stats.speed)
    val emptyArmorClassInput = addExtraNumber("No Armor AC", stats.emptyArmorClass)

    // Disarmed Attack Dice
    val disarmedCol = document.createElement("div") as HTMLDivElement
    val disarmedLbl = document.createElement("label") as HTMLLabelElement
    disarmedLbl.textContent = "Disarmed Dice"
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
    val initiativeLabel = addExtraLabel("Initiative", dexMod)
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
    resTitle.textContent = "Resistance Tests"
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
        line.style.display = "flex"
        line.style.alignItems = "center"
        line.style.setProperty("gap", "8px")
        line.style.marginBottom = "6px"

        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.checked = r.hasRes
        line.appendChild(cb)

        val name = document.createElement("span") as HTMLSpanElement
        name.textContent = r.label
        name.style.width = "30px"
        name.style.fontWeight = "bold"
        line.appendChild(name)

        val valLabel = document.createElement("span") as HTMLSpanElement
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
    val statusEl = document.createElement("span") as HTMLSpanElement
    statusEl.style.marginTop = "8px"
    statusEl.style.display = "block"
    statusEl.style.fontSize = "14px"
    container.appendChild(statusEl)

    var saveTimeout = 0

    fun autoSave() {
        statusEl.textContent = "Saving..."
        statusEl.style.color = "gray"

        if (saveTimeout != 0) window.clearTimeout(saveTimeout)
        saveTimeout = window.setTimeout({
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
            dndBaseStatsRepo.save(updated)
            statusEl.textContent = "\u2713 Saved"
            statusEl.style.color = "green"
            null
        }, 500)
    }

    container.addEventListener("input", { autoSave() })
    container.addEventListener("change", { autoSave() })

    // Skills section
    renderDndSkillsBox(character, container)
}

private val statAbbreviations = listOf("Str", "Dex", "Con", "Int", "Wis", "Cha")

private fun renderDndSkillsBox(character: Character, container: HTMLDivElement) {
    val skills = dndSkillsRepo.getByCharacterId(character.id) ?: DndSkills(characterId = character.id)
    val stats = dndBaseStatsRepo.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
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
    box.style.border = "1px solid #ccc"
    box.style.borderRadius = "8px"
    box.style.padding = "12px"
    box.style.maxWidth = "650px"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Skills"
    title.style.marginTop = "0"
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
        line.style.display = "flex"
        line.style.alignItems = "center"
        line.style.setProperty("gap", "8px")
        line.style.marginBottom = "6px"

        // Trained checkbox
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"
        cb.checked = entry.isTrained
        line.appendChild(cb)

        // Skill name
        val nameSpan = document.createElement("span") as HTMLSpanElement
        nameSpan.textContent = skillName
        nameSpan.style.width = "130px"
        nameSpan.style.fontWeight = "bold"
        nameSpan.style.fontSize = "13px"
        line.appendChild(nameSpan)

        // Modifier select
        val modSelect = document.createElement("select") as HTMLSelectElement
        modSelect.style.padding = "2px"
        statAbbreviations.forEach { abbr ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = abbr
            opt.textContent = abbr
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
        addInput.style.width = "45px"
        addInput.style.padding = "2px"
        addInput.style.textAlign = "center"
        line.appendChild(addInput)

        // Equals + total
        val totalLabel = document.createElement("span") as HTMLSpanElement
        totalLabel.style.fontWeight = "bold"
        totalLabel.style.minWidth = "50px"

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
    val statusEl = document.createElement("span") as HTMLSpanElement
    statusEl.style.marginTop = "8px"
    statusEl.style.display = "block"
    statusEl.style.fontSize = "14px"
    container.appendChild(statusEl)

    var saveTimeout = 0

    fun autoSave() {
        statusEl.textContent = "Saving..."
        statusEl.style.color = "gray"

        if (saveTimeout != 0) window.clearTimeout(saveTimeout)
        saveTimeout = window.setTimeout({
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
            dndSkillsRepo.save(updated)
            statusEl.textContent = "\u2713 Saved"
            statusEl.style.color = "green"
            null
        }, 500)
    }

    container.addEventListener("input", { autoSave() })
    container.addEventListener("change", { autoSave() })
}

private fun featureTypeSortOrder(f: DndFeature): Int = when (f.type) {
    "Weapon/Armor Proficiency" -> 0
    "Tool Proficiency" -> 1
    "Idiom" -> 2
    "Rechargable Feature" -> 3
    "Feature" -> 4
    else -> 5
}

private fun featureSourceSortOrder(f: DndFeature): Int = when (f.source) {
    "Race" -> 0
    "Origin" -> 1
    "Custom" -> 2
    "Class" -> 3
    else -> 4
}

private fun featureSourceLabel(f: DndFeature): String = when (f.source) {
    "Class" -> "${f.sourceClass ?: ""} Lv.${f.sourceClassLevel ?: "?"}"
    "Origin" -> f.sourceOrigin ?: "Origin"
    "Race" -> listOfNotNull(f.sourceRace, f.sourceSubRace).joinToString(" / ").ifEmpty { "Race" }
    "Custom" -> f.sourceCustom ?: "Custom"
    else -> f.source
}

private val namedColors = mapOf(
    "Black" to Triple(0, 0, 0), "White" to Triple(255, 255, 255),
    "Red" to Triple(255, 0, 0), "Dark Red" to Triple(139, 0, 0),
    "Brown" to Triple(139, 69, 19), "Light Brown" to Triple(181, 137, 80),
    "Tan" to Triple(210, 180, 140), "Beige" to Triple(245, 245, 220),
    "Peach" to Triple(255, 218, 185), "Olive" to Triple(128, 128, 0),
    "Green" to Triple(0, 128, 0), "Dark Green" to Triple(0, 100, 0),
    "Blue" to Triple(0, 0, 255), "Light Blue" to Triple(173, 216, 230),
    "Dark Blue" to Triple(0, 0, 139), "Purple" to Triple(128, 0, 128),
    "Violet" to Triple(238, 130, 238), "Pink" to Triple(255, 192, 203),
    "Orange" to Triple(255, 165, 0), "Yellow" to Triple(255, 255, 0),
    "Gold" to Triple(255, 215, 0), "Silver" to Triple(192, 192, 192),
    "Gray" to Triple(128, 128, 128), "Dark Gray" to Triple(64, 64, 64),
    "Light Gray" to Triple(211, 211, 211), "Copper" to Triple(184, 115, 51),
    "Auburn" to Triple(165, 42, 42), "Blonde" to Triple(250, 240, 190),
    "Platinum" to Triple(229, 228, 226), "Ivory" to Triple(255, 255, 240),
    "Ebony" to Triple(33, 36, 33), "Hazel" to Triple(142, 118, 58),
    "Amber" to Triple(255, 191, 0), "Teal" to Triple(0, 128, 128)
)

private fun hexToColorName(hex: String): String {
    val r = hex.substring(1, 3).toInt(16)
    val g = hex.substring(3, 5).toInt(16)
    val b = hex.substring(5, 7).toInt(16)
    var closest = "Black"
    var minDist = Int.MAX_VALUE
    namedColors.forEach { (name, rgb) ->
        val dist = (r - rgb.first) * (r - rgb.first) + (g - rgb.second) * (g - rgb.second) + (b - rgb.third) * (b - rgb.third)
        if (dist < minDist) { minDist = dist; closest = name }
    }
    return closest
}

private fun renderDndMagicTab(character: Character, container: HTMLDivElement) {
    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
    val stats = dndBaseStatsRepo.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
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
        val mainDisplayName = if (mainSubClass != null && mainSubClass in DungeonsAndDragons.subclassSpellcasting) "$mainClass ($mainSubClass)" else mainClass
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
            ability = when (mainAbility) { "Cha" -> "Charisma"; "Int" -> "Intelligence"; "Wis" -> "Wisdom"; else -> mainAbility },
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
        val secDisplayName = if (secSubClass != null && secSubClass in DungeonsAndDragons.subclassSpellcasting) "$secClass ($secSubClass)" else secClass
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
            ability = when (secAbility) { "Cha" -> "Charisma"; "Int" -> "Intelligence"; "Wis" -> "Wisdom"; else -> secAbility },
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
        lbl.textContent = "$className - Select Spellcasting Ability:"
        lbl.style.fontWeight = "bold"; lbl.style.marginRight = "8px"
        selectorBox.appendChild(lbl)

        val sel = document.createElement("select") as HTMLSelectElement
        sel.style.padding = "4px"
        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""; emptyOpt.textContent = "-- Select --"
        sel.appendChild(emptyOpt)
        listOf("Str", "Dex", "Con", "Int", "Wis", "Cha").forEach { ab ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = ab; opt.textContent = ab
            sel.appendChild(opt)
        }
        selectorBox.appendChild(sel)

        val noneOpt = document.createElement("option") as HTMLOptionElement
        noneOpt.value = "__none__"; noneOpt.textContent = "No Spellcasting"
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
        noMagic.textContent = "Your class does not have spellcasting ability."
        noMagic.style.color = "#999"
        container.appendChild(noMagic)

        customClassesWithNone.forEach { className ->
            val changeBtn = document.createElement("button") as HTMLButtonElement
            changeBtn.textContent = "\u270E Change Ability ($className)"
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
        titleEl.textContent = "${caster.className} - Spellcasting"
        titleEl.style.margin = "0"
        titleRow.appendChild(titleEl)

        if (caster.isCustom) {
            val changeBtn = document.createElement("button") as HTMLButtonElement
            changeBtn.textContent = "\u270E Change Ability"
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
            ritualMsg.textContent = "\u26A0\uFE0F Only cast magics as rituals"
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

        addStat("Ability", caster.ability)
        addStat("Spell Modifier", if (caster.spellMod >= 0) "+${caster.spellMod}" else "${caster.spellMod}")
        addStat("Spell DC", caster.spellDC.toString())

        val casterSpells = dndSpellRepo.getByCharacterId(character.id).filter { it.originClass == caster.className }
        val cantripsCount = casterSpells.count { it.circle == "Cantrip" }
        val spellsCount = casterSpells.count { it.circle != "Cantrip" }
        val preparedCount = casterSpells.count { it.isPrepared && it.circle != "Cantrip" }

        addStat("Cantrips Known", cantripsCount.toString())
        addStat("Spells Known", spellsCount.toString())
        if (caster.needsPreparation) {
            addStat("Prepared", preparedCount.toString())
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
    addSpellBtn.textContent = "\u2728 Add Spell"
    addSpellBtn.style.marginBottom = "16px"
    addSpellBtn.addEventListener("click", {
        showSpellModal(character, null) {
            container.innerHTML = ""
            renderDndMagicTab(character, container)
        }
    })
    container.appendChild(addSpellBtn)

    // Spell list by circle
    val allSpells = dndSpellRepo.getByCharacterId(character.id)
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
        colTitle.textContent = if (circle == "Cantrip") "Cantrips" else circle
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
                dndSpellRepo.delete(character.id, spell.id)
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
            val originStr = "${spell.originClass} (${spell.originLevel})"
            val rangeStr = if (spell.range.isNotEmpty()) "${spell.range}m" else ""
            val ritualStr = if (spell.canBeRitual) " | \uD83D\uDD2E Ritual" else ""
            val concStr = if (spell.needsConcentration) " | \uD83C\uDFAF Conc." else ""
            line2.textContent = "$originStr | ${spell.castingTime} | ${spell.duration} | $rangeStr$ritualStr$concStr"
            row.appendChild(line2)

            col.appendChild(row)
        }

        spellGrid.appendChild(col)
    }

    container.appendChild(spellGrid)
}

private fun showSpellModal(character: Character, existing: com.dungeonsanddeigo.model.DndSpell?, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "700px"; modal.style.width = "90%"
    modal.style.maxHeight = "85vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Spell" else "Add Spell"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("gap", "10px")

    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
    val classes = listOfNotNull(mainInfo?.mainClass, mainInfo?.secondaryClass).filter { it.isNotBlank() }

    // Row 1: Name, Origin Class, Origin Level
    val row1 = document.createElement("div") as HTMLDivElement
    row1.style.setProperty("display", "grid")
    row1.style.setProperty("grid-template-columns", "2fr 1fr 1fr")
    row1.style.setProperty("gap", "8px")

    fun labeledInput(parent: HTMLDivElement, label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "11px"; lbl.style.display = "block"
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.value = value; input.style.width = "100%"; input.style.padding = "4px"
        col.appendChild(input)
        parent.appendChild(col)
        return input
    }

    fun labeledSelect(parent: HTMLDivElement, label: String, options: List<String>, value: String): HTMLSelectElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "11px"; lbl.style.display = "block"
        col.appendChild(lbl)
        val sel = document.createElement("select") as HTMLSelectElement
        sel.style.width = "100%"; sel.style.padding = "4px"
        options.forEach { o -> val opt = document.createElement("option") as HTMLOptionElement; opt.value = o; opt.textContent = o; sel.appendChild(opt) }
        sel.value = value
        col.appendChild(sel)
        parent.appendChild(col)
        return sel
    }

    val nameInput = labeledInput(row1, "Name", existing?.name ?: "")
    val originClassSel = labeledSelect(row1, "Origin Class", classes.ifEmpty { listOf("") }, existing?.originClass ?: classes.firstOrNull() ?: "")
    val originLevelSel = labeledSelect(row1, "Origin Level", com.dungeonsanddeigo.model.DndSpell.originLevels, existing?.originLevel ?: "1")
    form.appendChild(row1)

    // Row 2: Circle, School, Prepared
    val row2 = document.createElement("div") as HTMLDivElement
    row2.style.setProperty("display", "grid")
    row2.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
    row2.style.setProperty("gap", "8px")

    val circleSel = labeledSelect(row2, "Circle", com.dungeonsanddeigo.model.DndSpell.circles, existing?.circle ?: "Cantrip")
    val schoolSel = labeledSelect(row2, "School", com.dungeonsanddeigo.model.DndSpell.schools, existing?.school ?: com.dungeonsanddeigo.model.DndSpell.schools.first())

    val prepCol = document.createElement("div") as HTMLDivElement
    val prepLbl = document.createElement("label") as HTMLLabelElement
    prepLbl.textContent = "Prepared"; prepLbl.style.fontWeight = "bold"; prepLbl.style.fontSize = "11px"; prepLbl.style.display = "block"
    prepCol.appendChild(prepLbl)
    val preparedCb = document.createElement("input") as HTMLInputElement
    preparedCb.type = "checkbox"; preparedCb.checked = existing?.isPrepared ?: false
    preparedCb.style.marginTop = "6px"
    prepCol.appendChild(preparedCb)
    row2.appendChild(prepCol)

    // Show Prepared only if not Cantrip and class uses prepared spells
    fun updatePreparedVisibility() {
        val isCantrip = circleSel.value == "Cantrip"
        val classUsesPrepared = originClassSel.value in DungeonsAndDragons.preparedCasters
        prepCol.style.display = if (!isCantrip && classUsesPrepared) "" else "none"
    }
    updatePreparedVisibility()
    circleSel.addEventListener("change", { updatePreparedVisibility() })
    originClassSel.addEventListener("change", { updatePreparedVisibility() })

    form.appendChild(row2)

    // Row 3: Casting Time, Duration, Range, Ritual
    val row3 = document.createElement("div") as HTMLDivElement
    row3.style.setProperty("display", "grid")
    row3.style.setProperty("grid-template-columns", "1fr 1fr 1fr auto")
    row3.style.setProperty("gap", "8px")
    row3.style.alignItems = "end"

    val castingTimeInput = labeledInput(row3, "Casting Time", existing?.castingTime ?: "")
    val durationInput = labeledInput(row3, "Duration", existing?.duration ?: "")

    // Range with m suffix
    val rangeCol = document.createElement("div") as HTMLDivElement
    val rangeLbl = document.createElement("label") as HTMLLabelElement
    rangeLbl.textContent = "Range"; rangeLbl.style.fontWeight = "bold"; rangeLbl.style.fontSize = "11px"; rangeLbl.style.display = "block"
    rangeCol.appendChild(rangeLbl)
    val rangeRow = document.createElement("div") as HTMLDivElement
    rangeRow.style.display = "flex"; rangeRow.style.alignItems = "center"; rangeRow.style.setProperty("gap", "2px")
    val rangeInput = document.createElement("input") as HTMLInputElement
    rangeInput.type = "number"; rangeInput.min = "0"
    rangeInput.value = existing?.range ?: ""; rangeInput.style.width = "100%"; rangeInput.style.padding = "4px"
    rangeRow.appendChild(rangeInput)
    val mLabel = document.createElement("span") as HTMLSpanElement
    mLabel.textContent = "m"; mLabel.style.fontSize = "12px"; mLabel.style.color = "#666"
    rangeRow.appendChild(mLabel)
    rangeCol.appendChild(rangeRow)
    row3.appendChild(rangeCol)

    // Ritual checkbox
    val ritualCol = document.createElement("div") as HTMLDivElement
    ritualCol.style.paddingBottom = "4px"
    val ritualLbl = document.createElement("label") as HTMLLabelElement
    val ritualCb = document.createElement("input") as HTMLInputElement
    ritualCb.type = "checkbox"; ritualCb.checked = existing?.canBeRitual ?: false
    ritualCb.style.marginRight = "4px"
    ritualLbl.appendChild(ritualCb); ritualLbl.append("Ritual")
    ritualLbl.style.fontSize = "12px"
    ritualCol.appendChild(ritualLbl)
    row3.appendChild(ritualCol)

    form.appendChild(row3)

    // Row 4: Concentration + Verbal, Somatic, Material, Material Components
    val row4 = document.createElement("div") as HTMLDivElement
    row4.style.setProperty("display", "grid")
    row4.style.setProperty("grid-template-columns", "auto auto auto auto 1fr")
    row4.style.setProperty("gap", "12px")
    row4.style.alignItems = "center"

    fun inlineCb(parent: HTMLDivElement, label: String, checked: Boolean): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"; cb.checked = checked; cb.style.marginRight = "4px"
        lbl.appendChild(cb); lbl.append(label); lbl.style.fontSize = "12px"
        parent.appendChild(lbl)
        return cb
    }

    val concentrationCb = inlineCb(row4, "Concentration", existing?.needsConcentration ?: false)
    val verbalCb = inlineCb(row4, "V", existing?.hasVerbal ?: false)
    val somaticCb = inlineCb(row4, "S", existing?.hasSomatic ?: false)
    val materialCb = inlineCb(row4, "M", existing?.hasMaterial ?: false)

    val materialInput = document.createElement("input") as HTMLInputElement
    materialInput.value = existing?.materialComponents ?: ""
    materialInput.placeholder = "Material components..."
    materialInput.style.padding = "4px"; materialInput.style.width = "100%"
    materialInput.style.display = if (existing?.hasMaterial == true) "" else "none"
    row4.appendChild(materialInput)

    materialCb.addEventListener("change", {
        materialInput.style.display = if (materialCb.checked) "" else "none"
    })

    form.appendChild(row4)

    // Description
    val descLbl = document.createElement("label") as HTMLLabelElement
    descLbl.textContent = "Description"; descLbl.style.fontWeight = "bold"; descLbl.style.fontSize = "11px"
    form.appendChild(descLbl)
    val descInput = document.createElement("textarea") as HTMLTextAreaElement
    descInput.value = existing?.description ?: ""; descInput.rows = 4; descInput.style.width = "100%"
    form.appendChild(descInput)

    // Higher Circles
    val higherLbl = document.createElement("label") as HTMLLabelElement
    higherLbl.textContent = "At Higher Circles"; higherLbl.style.fontWeight = "bold"; higherLbl.style.fontSize = "11px"
    form.appendChild(higherLbl)
    val higherInput = document.createElement("textarea") as HTMLTextAreaElement
    higherInput.value = existing?.higherCircles ?: ""; higherInput.rows = 2; higherInput.style.width = "100%"
    form.appendChild(higherInput)

    // Attack / Saving Throw section
    val attackRow = document.createElement("div") as HTMLDivElement
    attackRow.style.display = "flex"; attackRow.style.alignItems = "center"; attackRow.style.setProperty("gap", "16px")
    val attackLbl = document.createElement("label") as HTMLLabelElement
    val attackCb = document.createElement("input") as HTMLInputElement
    attackCb.type = "checkbox"; attackCb.checked = existing?.isAttack ?: false; attackCb.style.marginRight = "4px"
    attackLbl.appendChild(attackCb); attackLbl.append("This is an Attack"); attackLbl.style.fontWeight = "bold"; attackLbl.style.fontSize = "12px"
    attackRow.appendChild(attackLbl)

    val savingThrowLbl = document.createElement("label") as HTMLLabelElement
    val savingThrowCb = document.createElement("input") as HTMLInputElement
    savingThrowCb.type = "checkbox"; savingThrowCb.checked = existing?.needsSavingThrow ?: false; savingThrowCb.style.marginRight = "4px"
    savingThrowLbl.appendChild(savingThrowCb); savingThrowLbl.append("Need Saving Throw"); savingThrowLbl.style.fontWeight = "bold"; savingThrowLbl.style.fontSize = "12px"
    attackRow.appendChild(savingThrowLbl)
    form.appendChild(attackRow)

    // Attack details row (Damage Dice + Damage Type)
    val atkDetailsRow = document.createElement("div") as HTMLDivElement
    atkDetailsRow.style.setProperty("display", "grid")
    atkDetailsRow.style.setProperty("grid-template-columns", "1fr 1fr")
    atkDetailsRow.style.setProperty("gap", "8px")
    atkDetailsRow.style.display = if (existing?.isAttack == true) "grid" else "none"

    val diceInput = labeledInput(atkDetailsRow, "Damage Dice", existing?.attackDamageDice ?: "")
    val dmgTypeSel = labeledSelect(atkDetailsRow, "Damage Type", com.dungeonsanddeigo.model.DndSpell.damageTypes, existing?.attackDamageType ?: "Fire")
    form.appendChild(atkDetailsRow)

    // Saving Throw details (Save Ability)
    val saveDetailsRow = document.createElement("div") as HTMLDivElement
    saveDetailsRow.style.setProperty("display", "grid")
    saveDetailsRow.style.setProperty("grid-template-columns", "1fr")
    saveDetailsRow.style.setProperty("gap", "8px")
    saveDetailsRow.style.maxWidth = "200px"
    saveDetailsRow.style.display = if (existing?.needsSavingThrow == true) "grid" else "none"

    val saveSel = labeledSelect(saveDetailsRow, "Save Ability", com.dungeonsanddeigo.model.DndSpell.savingThrowAbilities, existing?.savingThrowAbility ?: "Dex")
    form.appendChild(saveDetailsRow)

    // Independent toggles: both can be enabled
    attackCb.addEventListener("change", {
        atkDetailsRow.style.display = if (attackCb.checked) "grid" else "none"
    })
    savingThrowCb.addEventListener("change", {
        saveDetailsRow.style.display = if (savingThrowCb.checked) "grid" else "none"
    })

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"; tagsLbl.style.fontSize = "11px"
    form.appendChild(tagsLbl)
    val tagBadges = document.createElement("div") as HTMLDivElement
    tagBadges.style.display = "flex"; tagBadges.style.setProperty("flex-wrap", "wrap"); tagBadges.style.setProperty("gap", "4px"); tagBadges.style.marginBottom = "6px"
    fun refreshSpellTags() {
        tagBadges.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"; badge.style.backgroundColor = "#e0e0e0"; badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"; badge.style.fontSize = "12px"; badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshSpellTags() })
            tagBadges.appendChild(badge)
        }
    }
    refreshSpellTags()
    form.appendChild(tagBadges)
    val tagRow = document.createElement("div") as HTMLDivElement
    tagRow.style.display = "flex"; tagRow.style.setProperty("gap", "8px")
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val t = tagInput.value.trim()
        if (t.isNotEmpty() && t !in selectedTags) { selectedTags.add(t); refreshSpellTags() }
        tagInput.value = ""
    })
    tagRow.appendChild(tagAddBtn)
    form.appendChild(tagRow)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val spell = com.dungeonsanddeigo.model.DndSpell(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            originClass = originClassSel.value,
            originLevel = originLevelSel.value,
            circle = circleSel.value,
            school = schoolSel.value,
            castingTime = castingTimeInput.value,
            duration = durationInput.value,
            range = rangeInput.value,
            canBeRitual = ritualCb.checked,
            needsConcentration = concentrationCb.checked,
            hasVerbal = verbalCb.checked,
            hasSomatic = somaticCb.checked,
            hasMaterial = materialCb.checked,
            materialComponents = materialInput.value,
            description = descInput.value,
            higherCircles = higherInput.value,
            isAttack = attackCb.checked,
            needsSavingThrow = savingThrowCb.checked,
            attackType = if (attackCb.checked) "Roll for Attack" else "",
            savingThrowAbility = saveSel.value,
            attackDamageDice = diceInput.value,
            attackDamageType = dmgTypeSel.value,
            isPrepared = preparedCb.checked,
            tags = selectedTags.toList()
        )
        dndSpellRepo.save(spell)
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun renderDndBackgroundTab(character: Character, container: HTMLDivElement) {
    // Appearance section
    val appearanceBox = document.createElement("div") as HTMLDivElement
    appearanceBox.style.border = "1px solid #ccc"
    appearanceBox.style.borderRadius = "8px"
    appearanceBox.style.padding = "16px"
    appearanceBox.style.setProperty("flex", "1")

    val appearanceTitle = document.createElement("h4") as HTMLHeadingElement
    appearanceTitle.textContent = "Appearance"
    appearanceTitle.style.margin = "0 0 12px 0"
    appearanceBox.appendChild(appearanceTitle)

    val content = document.createElement("div") as HTMLDivElement
    content.style.display = "flex"
    content.style.setProperty("gap", "16px")
    content.style.alignItems = "center"

    // Character picture
    val imgContainer = document.createElement("div") as HTMLDivElement
    imgContainer.style.textAlign = "center"

    val charImageKey = "char_image_${character.id}"
    val currentImage = localStorage.getItem(charImageKey) ?: character.imageBase64

    if (currentImage != null) {
        val img = document.createElement("img") as HTMLImageElement
        img.src = currentImage
        img.style.maxWidth = "300px"
        img.style.borderRadius = "8px"
        imgContainer.appendChild(img)
    } else {
        val placeholder = document.createElement("div") as HTMLDivElement
        placeholder.style.width = "120px"
        placeholder.style.height = "120px"
        placeholder.style.borderRadius = "8px"
        placeholder.style.backgroundColor = "#eee"
        placeholder.style.display = "flex"
        placeholder.style.alignItems = "center"
        placeholder.style.justifyContent = "center"
        placeholder.textContent = "\uD83D\uDDBC\uFE0F"
        placeholder.style.fontSize = "32px"
        imgContainer.appendChild(placeholder)
    }

    // Change picture button
    val changeImgBtn = document.createElement("button") as HTMLButtonElement
    changeImgBtn.textContent = "\uD83D\uDCF7 Change"
    changeImgBtn.style.fontSize = "11px"
    changeImgBtn.style.marginTop = "8px"
    changeImgBtn.style.display = "block"

    val fileInput = document.createElement("input") as HTMLInputElement
    fileInput.type = "file"
    fileInput.accept = "image/*"
    fileInput.style.display = "none"

    changeImgBtn.addEventListener("click", { fileInput.click() })
    fileInput.addEventListener("change", {
        val file = fileInput.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result
                if (result != null) {
                    val newImage = result.toString()
                    // Update character image in localStorage
                    val charImageKey = "char_image_${character.id}"
                    localStorage.setItem(charImageKey, newImage)
                    // Update header image
                    val headerImg = document.getElementById("header-char-img") as? HTMLImageElement
                    if (headerImg != null) {
                        headerImg.src = newImage
                    }
                    container.innerHTML = ""
                    renderDndBackgroundTab(character.copy(imageBase64 = newImage), container)
                }
                Unit
            }
            reader.readAsDataURL(file)
        }
    })

    imgContainer.appendChild(changeImgBtn)
    imgContainer.appendChild(fileInput)
    content.appendChild(imgContainer)

    // Character name (editable with autosave)
    val nameDiv = document.createElement("div") as HTMLDivElement
    val nameLabel = document.createElement("div") as HTMLDivElement
    nameLabel.textContent = "Name"
    nameLabel.style.fontSize = "12px"
    nameLabel.style.color = "#666"
    nameLabel.style.marginBottom = "4px"
    nameDiv.appendChild(nameLabel)

    val charNameKey = "char_name_${character.id}"
    val currentName = localStorage.getItem(charNameKey) ?: character.name

    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = currentName
    nameInput.style.fontSize = "24px"
    nameInput.style.fontWeight = "bold"
    nameInput.style.border = "none"
    nameInput.style.borderBottom = "1px solid #ccc"
    nameInput.style.outline = "none"
    nameInput.style.width = "100%"
    nameDiv.appendChild(nameInput)

    val nameStatus = document.createElement("span") as HTMLSpanElement
    nameStatus.style.fontSize = "11px"
    nameStatus.style.display = "block"
    nameStatus.style.marginTop = "4px"
    nameDiv.appendChild(nameStatus)

    var nameSaveTimeout = 0
    nameInput.addEventListener("input", {
        nameStatus.textContent = "Saving..."
        nameStatus.style.color = "gray"
        if (nameSaveTimeout != 0) window.clearTimeout(nameSaveTimeout)
        nameSaveTimeout = window.setTimeout({
            localStorage.setItem(charNameKey, nameInput.value)
            // Update header name
            val headerName = document.getElementById("header-char-name")
            if (headerName != null) headerName.textContent = nameInput.value
            nameStatus.textContent = "\u2713 Saved"
            nameStatus.style.color = "green"
            null
        }, 500)
    })

    content.appendChild(nameDiv)

    appearanceBox.appendChild(content)

    // Appearance details form
    val appearance = dndAppearanceRepo.getByCharacterId(character.id) ?: com.dungeonsanddeigo.model.DndAppearance(characterId = character.id)

    val detailsForm = document.createElement("div") as HTMLDivElement
    detailsForm.style.setProperty("display", "grid")
    detailsForm.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
    detailsForm.style.setProperty("gap", "10px")
    detailsForm.style.marginTop = "16px"

    fun addDetailField(label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "12px"
        lbl.style.display = "block"; lbl.style.marginBottom = "4px"
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.value = value; input.style.width = "100%"; input.style.padding = "4px"
        col.appendChild(input)
        detailsForm.appendChild(col)
        return input
    }

    fun addColorField(label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "12px"
        lbl.style.display = "block"; lbl.style.marginBottom = "4px"
        col.appendChild(lbl)
        val row = document.createElement("div") as HTMLDivElement
        row.style.display = "flex"; row.style.setProperty("gap", "8px"); row.style.alignItems = "center"

        val colorPicker = document.createElement("input") as HTMLInputElement
        colorPicker.type = "color"
        colorPicker.style.width = "30px"; colorPicker.style.height = "30px"
        colorPicker.style.border = "none"; colorPicker.style.cursor = "pointer"
        // Set initial color from saved name
        if (value.isNotEmpty()) {
            val rgb = namedColors[value]
            if (rgb != null) {
                colorPicker.value = "#${rgb.first.toString(16).padStart(2, '0')}${rgb.second.toString(16).padStart(2, '0')}${rgb.third.toString(16).padStart(2, '0')}"
            }
        }
        row.appendChild(colorPicker)

        val colorLabel = document.createElement("span") as HTMLSpanElement
        colorLabel.textContent = value.ifEmpty { "Pick a color" }
        colorLabel.style.fontSize = "14px"
        row.appendChild(colorLabel)

        // Hidden input to carry the value for autosave
        val input = document.createElement("input") as HTMLInputElement
        input.type = "hidden"
        input.value = value
        row.appendChild(input)

        colorPicker.addEventListener("input", {
            val name = hexToColorName(colorPicker.value)
            colorLabel.textContent = name
            input.value = name
            // Trigger autosave
            val event = document.createEvent("Event")
            event.initEvent("input", true, true)
            appearanceBox.dispatchEvent(event)
        })

        col.appendChild(row)
        detailsForm.appendChild(col)
        return input
    }

    val ageInput = addDetailField("Age", appearance.age)
    val heightInput = addDetailField("Height", appearance.height)
    val weightInput = addDetailField("Weight", appearance.weight)
    val eyeColorInput = addColorField("Eye Color", appearance.eyeColor)
    val skinColorInput = addColorField("Skin Color", appearance.skinColor)
    val hairColorInput = addColorField("Hair Color", appearance.hairColor)

    appearanceBox.appendChild(detailsForm)

    // Description textarea
    val descLabel = document.createElement("label") as HTMLLabelElement
    descLabel.textContent = "Appearance Description"
    descLabel.style.fontWeight = "bold"; descLabel.style.fontSize = "12px"
    descLabel.style.display = "block"; descLabel.style.marginTop = "12px"; descLabel.style.marginBottom = "4px"
    appearanceBox.appendChild(descLabel)
    val descInput = document.createElement("textarea") as HTMLTextAreaElement
    descInput.value = appearance.description
    descInput.rows = 4; descInput.style.width = "100%"
    appearanceBox.appendChild(descInput)

    // Auto-save
    val appearanceStatus = document.createElement("span") as HTMLSpanElement
    appearanceStatus.style.fontSize = "11px"; appearanceStatus.style.display = "block"
    appearanceStatus.style.marginTop = "6px"
    appearanceBox.appendChild(appearanceStatus)

    var appearanceSaveTimeout = 0
    fun autoSaveAppearance() {
        appearanceStatus.textContent = "Saving..."
        appearanceStatus.style.color = "gray"
        if (appearanceSaveTimeout != 0) window.clearTimeout(appearanceSaveTimeout)
        appearanceSaveTimeout = window.setTimeout({
            dndAppearanceRepo.save(com.dungeonsanddeigo.model.DndAppearance(
                characterId = character.id,
                age = ageInput.value,
                height = heightInput.value,
                weight = weightInput.value,
                eyeColor = eyeColorInput.value,
                skinColor = skinColorInput.value,
                hairColor = hairColorInput.value,
                description = descInput.value
            ))
            appearanceStatus.textContent = "\u2713 Saved"
            appearanceStatus.style.color = "green"
            null
        }, 500)
    }

    appearanceBox.addEventListener("input", { autoSaveAppearance() })

    // Layout: Appearance (left) + Backstory (right)
    val bgRow = document.createElement("div") as HTMLDivElement
    bgRow.style.display = "flex"
    bgRow.style.setProperty("gap", "16px")
    bgRow.style.alignItems = "flex-start"
    bgRow.appendChild(appearanceBox)

    // === BACKSTORY BOX ===
    val backstoryBox = document.createElement("div") as HTMLDivElement
    backstoryBox.style.border = "1px solid #ccc"
    backstoryBox.style.borderRadius = "8px"
    backstoryBox.style.padding = "16px"
    backstoryBox.style.setProperty("flex", "1")

    val backstoryTitle = document.createElement("h4") as HTMLHeadingElement
    backstoryTitle.textContent = "Backstory"
    backstoryTitle.style.margin = "0 0 12px 0"
    backstoryBox.appendChild(backstoryTitle)

    val backstory = dndBackstoryRepo.getByCharacterId(character.id) ?: com.dungeonsanddeigo.model.DndBackstory(characterId = character.id)

    // Personality fields grid
    val personalityGrid = document.createElement("div") as HTMLDivElement
    personalityGrid.style.setProperty("display", "grid")
    personalityGrid.style.setProperty("grid-template-columns", "1fr 1fr")
    personalityGrid.style.setProperty("gap", "12px")
    personalityGrid.style.marginBottom = "12px"

    fun addTextArea(parent: HTMLDivElement, label: String, value: String, rows: Int = 3): HTMLTextAreaElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "12px"
        lbl.style.display = "block"; lbl.style.marginBottom = "4px"
        col.appendChild(lbl)
        val ta = document.createElement("textarea") as HTMLTextAreaElement
        ta.value = value; ta.rows = rows; ta.style.width = "100%"
        col.appendChild(ta)
        parent.appendChild(col)
        return ta
    }

    val traitsInput = addTextArea(personalityGrid, "Personality Traits", backstory.personalityTraits)
    val idealsInput = addTextArea(personalityGrid, "Ideals", backstory.ideals)
    val bondsInput = addTextArea(personalityGrid, "Bonds", backstory.bonds)
    val defectsInput = addTextArea(personalityGrid, "Defects", backstory.defects)
    val habitsInput = addTextArea(personalityGrid, "Habits", backstory.habits)

    backstoryBox.appendChild(personalityGrid)

    // Faction section
    val factionSection = document.createElement("div") as HTMLDivElement
    factionSection.style.borderTop = "1px solid #eee"
    factionSection.style.paddingTop = "12px"
    factionSection.style.marginBottom = "12px"

    val factionCheckLabel = document.createElement("label") as HTMLLabelElement
    factionCheckLabel.style.fontWeight = "bold"; factionCheckLabel.style.fontSize = "13px"
    val factionCb = document.createElement("input") as HTMLInputElement
    factionCb.type = "checkbox"; factionCb.checked = backstory.hasFaction
    factionCb.style.marginRight = "6px"
    factionCheckLabel.appendChild(factionCb)
    factionCheckLabel.append("Has Faction?")
    factionSection.appendChild(factionCheckLabel)

    val factionDetails = document.createElement("div") as HTMLDivElement
    factionDetails.style.marginTop = "10px"
    factionDetails.style.display = if (backstory.hasFaction) "block" else "none"

    // Faction Name
    val factionNameLbl = document.createElement("label") as HTMLLabelElement
    factionNameLbl.textContent = "Faction Name"; factionNameLbl.style.fontWeight = "bold"; factionNameLbl.style.fontSize = "12px"
    factionNameLbl.style.display = "block"; factionNameLbl.style.marginBottom = "4px"
    factionDetails.appendChild(factionNameLbl)
    val factionNameInput = document.createElement("input") as HTMLInputElement
    factionNameInput.value = backstory.factionName; factionNameInput.style.width = "100%"; factionNameInput.style.padding = "4px"
    factionNameInput.style.marginBottom = "10px"
    factionDetails.appendChild(factionNameInput)

    // Faction Symbol
    val factionSymbolLbl = document.createElement("label") as HTMLLabelElement
    factionSymbolLbl.textContent = "Faction Symbol"; factionSymbolLbl.style.fontWeight = "bold"; factionSymbolLbl.style.fontSize = "12px"
    factionSymbolLbl.style.display = "block"; factionSymbolLbl.style.marginBottom = "4px"
    factionDetails.appendChild(factionSymbolLbl)

    if (backstory.factionSymbol != null) {
        val symbolImg = document.createElement("img") as HTMLImageElement
        symbolImg.src = backstory.factionSymbol!!
        symbolImg.style.maxWidth = "100px"; symbolImg.style.borderRadius = "4px"
        symbolImg.style.display = "block"; symbolImg.style.marginBottom = "6px"
        factionDetails.appendChild(symbolImg)
    }

    val symbolFileInput = document.createElement("input") as HTMLInputElement
    symbolFileInput.type = "file"; symbolFileInput.accept = "image/*"
    symbolFileInput.style.marginBottom = "10px"
    factionDetails.appendChild(symbolFileInput)

    var factionSymbolBase64: String? = backstory.factionSymbol
    symbolFileInput.addEventListener("change", {
        val file = symbolFileInput.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result
                if (result != null) {
                    factionSymbolBase64 = result.toString()
                    // Trigger save
                    val event = document.createEvent("Event")
                    event.initEvent("input", true, true)
                    backstoryBox.dispatchEvent(event)
                }
                Unit
            }
            reader.readAsDataURL(file)
        }
    })

    // Faction Backstory
    val factionBackstoryInput = addTextArea(factionDetails, "Faction Backstory", backstory.factionBackstory)

    factionCb.addEventListener("change", {
        factionDetails.style.display = if (factionCb.checked) "block" else "none"
    })

    factionSection.appendChild(factionDetails)
    backstoryBox.appendChild(factionSection)

    // Character Backstory
    val charBackstoryLbl = document.createElement("label") as HTMLLabelElement
    charBackstoryLbl.textContent = "Character Backstory"; charBackstoryLbl.style.fontWeight = "bold"; charBackstoryLbl.style.fontSize = "12px"
    charBackstoryLbl.style.display = "block"; charBackstoryLbl.style.marginBottom = "4px"
    charBackstoryLbl.style.borderTop = "1px solid #eee"; charBackstoryLbl.style.paddingTop = "12px"
    backstoryBox.appendChild(charBackstoryLbl)
    val charBackstoryInput = document.createElement("textarea") as HTMLTextAreaElement
    charBackstoryInput.value = backstory.characterBackstory
    charBackstoryInput.rows = 10; charBackstoryInput.style.width = "100%"
    backstoryBox.appendChild(charBackstoryInput)

    // Auto-save
    val backstoryStatus = document.createElement("span") as HTMLSpanElement
    backstoryStatus.style.fontSize = "11px"; backstoryStatus.style.display = "block"
    backstoryStatus.style.marginTop = "6px"
    backstoryBox.appendChild(backstoryStatus)

    var backstorySaveTimeout = 0
    fun autoSaveBackstory() {
        backstoryStatus.textContent = "Saving..."
        backstoryStatus.style.color = "gray"
        if (backstorySaveTimeout != 0) window.clearTimeout(backstorySaveTimeout)
        backstorySaveTimeout = window.setTimeout({
            dndBackstoryRepo.save(com.dungeonsanddeigo.model.DndBackstory(
                characterId = character.id,
                personalityTraits = traitsInput.value,
                ideals = idealsInput.value,
                bonds = bondsInput.value,
                defects = defectsInput.value,
                habits = habitsInput.value,
                hasFaction = factionCb.checked,
                factionName = factionNameInput.value,
                factionSymbol = factionSymbolBase64,
                factionBackstory = factionBackstoryInput.value,
                characterBackstory = charBackstoryInput.value
            ))
            backstoryStatus.textContent = "\u2713 Saved"
            backstoryStatus.style.color = "green"
            null
        }, 500)
    }

    backstoryBox.addEventListener("input", { autoSaveBackstory() })
    backstoryBox.addEventListener("change", { autoSaveBackstory() })

    bgRow.appendChild(backstoryBox)
    container.appendChild(bgRow)
}

private fun renderDndNotesTab(character: Character, container: HTMLDivElement) {
    fun refresh() {
        container.innerHTML = ""

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "\uD83D\uDCDD Add Note"
        addBtn.style.marginBottom = "16px"
        addBtn.addEventListener("click", {
            showNoteModal(character, null) { refresh() }
        })
        container.appendChild(addBtn)

        val notes = dndNoteRepo.getByCharacterId(character.id).sortedByDescending { it.timestamp }

        if (notes.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = "No notes yet."
            empty.style.color = "#999"
            container.appendChild(empty)
            return
        }

        notes.forEach { note ->
            val card = document.createElement("div") as HTMLDivElement
            card.style.border = "1px solid #ccc"
            card.style.borderRadius = "8px"
            card.style.padding = "12px"
            card.style.marginBottom = "10px"
            card.style.maxWidth = "700px"

            // Header: title + session + timestamp
            val header = document.createElement("div") as HTMLDivElement
            header.style.display = "flex"
            header.style.justifyContent = "space-between"
            header.style.alignItems = "center"
            header.style.marginBottom = "6px"

            val titleEl = document.createElement("h4") as HTMLHeadingElement
            titleEl.textContent = note.title.ifEmpty { "(Untitled)" }
            titleEl.style.margin = "0"
            header.appendChild(titleEl)

            val metaDiv = document.createElement("div") as HTMLDivElement
            metaDiv.style.fontSize = "11px"; metaDiv.style.color = "#666"
            metaDiv.style.textAlign = "right"
            if (note.session.isNotEmpty()) {
                val sessionSpan = document.createElement("div") as HTMLDivElement
                sessionSpan.textContent = "Session: ${note.session}"
                metaDiv.appendChild(sessionSpan)
            }
            val timeSpan = document.createElement("div") as HTMLDivElement
            timeSpan.textContent = note.timestamp
            metaDiv.appendChild(timeSpan)
            header.appendChild(metaDiv)

            card.appendChild(header)

            // Note content
            val noteContent = document.createElement("p") as HTMLParagraphElement
            noteContent.textContent = note.note
            noteContent.style.margin = "0 0 8px 0"
            noteContent.style.fontSize = "14px"
            noteContent.style.whiteSpace = "pre-wrap"
            card.appendChild(noteContent)

            // Tags
            if (note.tags.isNotEmpty()) {
                val tagsDiv = document.createElement("div") as HTMLDivElement
                tagsDiv.style.marginBottom = "8px"
                tagsDiv.style.display = "flex"
                tagsDiv.style.setProperty("flex-wrap", "wrap")
                tagsDiv.style.setProperty("gap", "4px")
                note.tags.forEach { tag ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = tag
                    badge.style.backgroundColor = "#eee"
                    badge.style.padding = "2px 6px"
                    badge.style.borderRadius = "4px"
                    badge.style.fontSize = "11px"
                    tagsDiv.appendChild(badge)
                }
                card.appendChild(tagsDiv)
            }

            // Actions
            val actions = document.createElement("div") as HTMLDivElement
            actions.style.display = "flex"
            actions.style.setProperty("gap", "8px")

            val editBtn = document.createElement("button") as HTMLButtonElement
            editBtn.textContent = "Edit"
            editBtn.style.fontSize = "11px"
            editBtn.addEventListener("click", { showNoteModal(character, note) { refresh() } })
            actions.appendChild(editBtn)

            val deleteBtn = document.createElement("button") as HTMLButtonElement
            deleteBtn.textContent = "Delete"
            deleteBtn.style.fontSize = "11px"; deleteBtn.style.color = "red"
            deleteBtn.addEventListener("click", {
                dndNoteRepo.delete(character.id, note.id)
                refresh()
            })
            actions.appendChild(deleteBtn)

            card.appendChild(actions)
            container.appendChild(card)
        }
    }

    refresh()
}

private fun showNoteModal(character: Character, existing: com.dungeonsanddeigo.model.DndNote?, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "500px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Note" else "Add Note"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("gap", "10px")

    // Title
    val titleLbl = document.createElement("label") as HTMLLabelElement
    titleLbl.textContent = "Title"; titleLbl.style.fontWeight = "bold"
    form.appendChild(titleLbl)
    val titleInput = document.createElement("input") as HTMLInputElement
    titleInput.value = existing?.title ?: ""
    titleInput.style.padding = "6px"
    form.appendChild(titleInput)

    // Session
    val sessionLbl = document.createElement("label") as HTMLLabelElement
    sessionLbl.textContent = "Session"; sessionLbl.style.fontWeight = "bold"
    form.appendChild(sessionLbl)
    val sessionInput = document.createElement("input") as HTMLInputElement
    sessionInput.value = existing?.session ?: ""
    sessionInput.placeholder = "e.g. Session 5"
    sessionInput.style.padding = "6px"
    form.appendChild(sessionInput)

    // Note
    val noteLbl = document.createElement("label") as HTMLLabelElement
    noteLbl.textContent = "Note"; noteLbl.style.fontWeight = "bold"
    form.appendChild(noteLbl)
    val noteInput = document.createElement("textarea") as HTMLTextAreaElement
    noteInput.value = existing?.note ?: ""
    noteInput.rows = 6; noteInput.style.width = "100%"
    form.appendChild(noteInput)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"
    form.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")
    tagBadgesDiv.style.marginBottom = "6px"

    fun refreshNoteTags() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshNoteTags() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshNoteTags()
    form.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshNoteTags() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    form.appendChild(tagAddRow)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val now = currentTimestamp()
        val note = com.dungeonsanddeigo.model.DndNote(
            id = existing?.id ?: 0,
            characterId = character.id,
            title = titleInput.value,
            timestamp = if (existing != null) existing.timestamp else now,
            session = sessionInput.value,
            note = noteInput.value,
            tags = selectedTags.toList()
        )
        dndNoteRepo.save(note)
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun renderDndInventoryTab(character: Character, container: HTMLDivElement) {
    val sections = listOf("Armor", "Weapons", "Magic Items", "Money", "Potions Ammo and Ration", "Key Items, Loot and others")

    fun refresh() {
        container.innerHTML = ""
        val grid = document.createElement("div") as HTMLDivElement
        grid.style.setProperty("display", "grid")
        grid.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
        grid.style.setProperty("gap", "16px")

        sections.forEach { section ->
            val box = document.createElement("div") as HTMLDivElement
            box.style.border = "1px solid #ccc"
            box.style.borderRadius = "8px"
            box.style.padding = "12px"
            box.style.minHeight = "150px"

            // Header with title and Add button
            val header = document.createElement("div") as HTMLDivElement
            header.style.display = "flex"
            header.style.justifyContent = "space-between"
            header.style.alignItems = "center"
            header.style.marginBottom = "8px"

            val title = document.createElement("h4") as HTMLHeadingElement
            title.textContent = section
            title.style.margin = "0"
            header.appendChild(title)

            if (section != "Money") {
                val addBtn = document.createElement("button") as HTMLButtonElement
                addBtn.textContent = "+ Add"
                addBtn.style.fontSize = "12px"
                addBtn.addEventListener("click", {
                    when (section) {
                        "Armor" -> showArmorModal(character, null) { refresh() }
                        "Weapons" -> showWeaponModal(character, null) { refresh() }
                        "Magic Items" -> showMagicItemModal(character, null) { refresh() }
                        "Potions Ammo and Ration" -> showConsumableModal(character, null) { refresh() }
                        else -> showInventoryItemModal(character, section, null) { refresh() }
                    }
                })
                header.appendChild(addBtn)
            }

            box.appendChild(header)

            if (section == "Money") {
                // Money section: editable fields with auto-save
                val money = dndMoneyRepo.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)

                val coinsGrid = document.createElement("div") as HTMLDivElement
                coinsGrid.style.setProperty("display", "grid")
                coinsGrid.style.setProperty("grid-template-columns", "1fr 1fr")
                coinsGrid.style.setProperty("gap", "6px")
                coinsGrid.style.fontSize = "13px"

                fun addCoinField(label: String, abbr: String, value: Int): HTMLInputElement {
                    val lbl = document.createElement("label") as HTMLLabelElement
                    lbl.textContent = label
                    lbl.style.fontWeight = "bold"
                    coinsGrid.appendChild(lbl)
                    val wrapper = document.createElement("div") as HTMLDivElement
                    wrapper.style.display = "flex"
                    wrapper.style.alignItems = "center"
                    wrapper.style.setProperty("gap", "4px")
                    val input = document.createElement("input") as HTMLInputElement
                    input.type = "number"; input.min = "0"
                    input.value = value.toString()
                    input.style.width = "100%"; input.style.padding = "2px"
                    wrapper.appendChild(input)
                    val abbrSpan = document.createElement("span") as HTMLSpanElement
                    abbrSpan.textContent = abbr
                    abbrSpan.style.fontSize = "12px"
                    abbrSpan.style.color = "#666"
                    wrapper.appendChild(abbrSpan)
                    coinsGrid.appendChild(wrapper)
                    return input
                }

                val cpInput = addCoinField("Copper", "pc", money.copper)
                val spInput = addCoinField("Silver", "ps", money.silver)
                val epInput = addCoinField("Electrum", "pe", money.electrum)
                val gpInput = addCoinField("Gold", "pg", money.gold)
                val ppInput = addCoinField("Platinum", "pp", money.platinum)

                // Lifestyle
                val lifeLbl = document.createElement("label") as HTMLLabelElement
                lifeLbl.textContent = "Lifestyle"; lifeLbl.style.fontWeight = "bold"
                coinsGrid.appendChild(lifeLbl)
                val lifeSelect = document.createElement("select") as HTMLSelectElement
                lifeSelect.style.width = "100%"
                val emptyOpt = document.createElement("option") as HTMLOptionElement
                emptyOpt.value = ""; emptyOpt.textContent = "--"
                lifeSelect.appendChild(emptyOpt)
                DndMoney.lifestyles.forEach { ls ->
                    val opt = document.createElement("option") as HTMLOptionElement
                    opt.value = ls; opt.textContent = ls
                    lifeSelect.appendChild(opt)
                }
                lifeSelect.value = money.lifestyle
                coinsGrid.appendChild(lifeSelect)

                // Cost per day label
                val costLabel = document.createElement("span") as HTMLSpanElement
                costLabel.style.setProperty("grid-column", "1 / -1")
                costLabel.style.fontSize = "12px"
                costLabel.style.color = "#555"
                costLabel.style.marginTop = "4px"

                fun lifestyleCost(ls: String): String = when (ls) {
                    "Wretched" -> "No Cost"
                    "Squalid" -> "1 ps / day"
                    "Poor" -> "2 ps / day"
                    "Modest" -> "1 pg / day"
                    "Comfortable" -> "2 pg / day"
                    "Wealthy" -> "4 pg / day"
                    "Aristocratic" -> "10 pg / day"
                    else -> ""
                }

                fun updateCostLabel() {
                    val cost = lifestyleCost(lifeSelect.value)
                    costLabel.textContent = if (cost.isNotEmpty()) "Cost per day: $cost" else ""
                }
                updateCostLabel()
                lifeSelect.addEventListener("change", { updateCostLabel() })
                coinsGrid.appendChild(costLabel)

                box.appendChild(coinsGrid)

                // Auto-save for money
                val moneyStatus = document.createElement("span") as HTMLSpanElement
                moneyStatus.style.fontSize = "11px"
                moneyStatus.style.display = "block"
                moneyStatus.style.marginTop = "6px"
                box.appendChild(moneyStatus)

                var moneySaveTimeout = 0
                fun autoSaveMoney() {
                    moneyStatus.textContent = "Saving..."
                    moneyStatus.style.color = "gray"
                    if (moneySaveTimeout != 0) window.clearTimeout(moneySaveTimeout)
                    moneySaveTimeout = window.setTimeout({
                        dndMoneyRepo.save(DndMoney(
                            characterId = character.id,
                            copper = cpInput.value.toIntOrNull() ?: 0,
                            silver = spInput.value.toIntOrNull() ?: 0,
                            electrum = epInput.value.toIntOrNull() ?: 0,
                            gold = gpInput.value.toIntOrNull() ?: 0,
                            platinum = ppInput.value.toIntOrNull() ?: 0,
                            lifestyle = lifeSelect.value
                        ))
                        moneyStatus.textContent = "\u2713 Saved"
                        moneyStatus.style.color = "green"
                        null
                    }, 500)
                }
                coinsGrid.addEventListener("input", { autoSaveMoney() })
                coinsGrid.addEventListener("change", { autoSaveMoney() })
            } else if (section == "Armor") {
                // Armor section
                val armorTypeOrder = mapOf("Light Armor" to 0, "Medium Armor" to 1, "Heavy Armor" to 2, "Shield" to 3, "Clothes" to 4)
                val armors = dndArmorRepo.getByCharacterId(character.id)
                    .sortedWith(compareByDescending<DndArmor> { it.isEquipped }
                        .thenBy { armorTypeOrder[it.type] ?: 5 }
                        .thenBy { it.name })

                // Calculate AC
                val stats = dndBaseStatsRepo.getByCharacterId(character.id)
                val dexMod = stats?.dexValue?.let { calcModifier(it) } ?: 0
                val noArmorAC = stats?.emptyArmorClass ?: 10
                val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
                val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
                val shieldAC = equippedShield?.baseAC ?: 0

                val totalAC = if (equippedArmor == null) {
                    noArmorAC + shieldAC
                } else {
                    val armorBase = equippedArmor.baseAC
                    val modBonus = when (equippedArmor.acModifier) {
                        "Dex" -> dexMod
                        "Dex (Max: 2)" -> minOf(dexMod, 2)
                        else -> 0
                    }
                    armorBase + modBonus + shieldAC
                }

                // Display AC
                // Check proficiencies and stats for warnings
                val features = dndFeaturesRepo.getByCharacterId(character.id)
                val weaponArmorFeatures = features.filter { it.type == "Weapon/Armor Proficiency" || it.type == "Proficiency" }
                val armorProficiencies = mutableSetOf<String>()
                weaponArmorFeatures.forEach { f ->
                    f.description.split(",").map { it.trim() }.forEach { entry ->
                        if (entry.startsWith("armor:")) armorProficiencies.add(entry.removePrefix("armor:").trim())
                    }
                }

                // Check if equipped armor/shield lacks proficiency
                val equippedArmorProfKey = when (equippedArmor?.type) {
                    "Light Armor" -> "Light"
                    "Medium Armor" -> "Medium"
                    "Heavy Armor" -> "Heavy"
                    else -> null
                }
                val equippedShieldProfKey = if (equippedShield != null) "Shields" else null
                val lacksArmorProf = equippedArmor != null && equippedArmorProfKey != null && equippedArmorProfKey !in armorProficiencies
                val lacksShieldProf = equippedShield != null && equippedShieldProfKey != null && equippedShieldProfKey !in armorProficiencies
                val lacksAnyProf = lacksArmorProf || lacksShieldProf

                // Display AC with optional warning
                val acRow = document.createElement("div") as HTMLDivElement
                acRow.style.display = "flex"
                acRow.style.alignItems = "center"
                acRow.style.setProperty("gap", "12px")
                acRow.style.marginBottom = "12px"

                val acDisplay = document.createElement("div") as HTMLDivElement
                acDisplay.style.textAlign = "center"
                val acNumber = document.createElement("span") as HTMLSpanElement
                acNumber.textContent = totalAC.toString()
                acNumber.style.fontSize = "36px"
                acNumber.style.fontWeight = "bold"
                acDisplay.appendChild(acNumber)
                val acLabel = document.createElement("div") as HTMLDivElement
                acLabel.textContent = "AC"
                acLabel.style.fontSize = "12px"
                acLabel.style.color = "#666"
                acDisplay.appendChild(acLabel)
                acRow.appendChild(acDisplay)

                // Strength warning check
                val charStrength = stats?.strValue ?: 0
                val charSpeed = stats?.speed ?: 0
                val lacksStrength = equippedArmor != null && equippedArmor.minimumStrength > 0 && charStrength < equippedArmor.minimumStrength

                // Warnings column
                val warningsDiv = document.createElement("div") as HTMLDivElement
                warningsDiv.style.setProperty("flex", "1")

                if (lacksAnyProf) {
                    val warning = document.createElement("div") as HTMLDivElement
                    warning.textContent = "\u26A0\uFE0F Lack Proficiency: Disadvantages on Tests, Saving Throws and attacks for Str and Dex. Cannot use magic."
                    warning.style.color = "#c00"
                    warning.style.fontSize = "12px"
                    warning.style.marginBottom = "4px"
                    warningsDiv.appendChild(warning)
                }

                if (equippedArmor?.hasSneakDisadvantage == true) {
                    val sneakWarn = document.createElement("div") as HTMLDivElement
                    sneakWarn.textContent = "\u26A0\uFE0F Equipped armor has Disadvantages on Tests for Dex (Stealth)"
                    sneakWarn.style.color = "#c00"
                    sneakWarn.style.fontSize = "12px"
                    sneakWarn.style.marginBottom = "4px"
                    warningsDiv.appendChild(sneakWarn)
                }

                if (lacksStrength) {
                    val strWarn = document.createElement("div") as HTMLDivElement
                    strWarn.textContent = "\u26A0\uFE0F Need more strength to use this armor. Your speed is reduced by 3"
                    strWarn.style.color = "#c00"
                    strWarn.style.fontSize = "12px"
                    strWarn.style.marginBottom = "4px"
                    warningsDiv.appendChild(strWarn)
                }

                acRow.appendChild(warningsDiv)

                // Speed display
                val speedDisplay = document.createElement("div") as HTMLDivElement
                speedDisplay.style.textAlign = "center"
                val effectiveSpeed = if (lacksStrength) charSpeed - 3 else charSpeed
                val speedNumber = document.createElement("span") as HTMLSpanElement
                speedNumber.textContent = "${effectiveSpeed}m"
                speedNumber.style.fontSize = "36px"
                speedNumber.style.fontWeight = "bold"
                if (lacksStrength) speedNumber.style.color = "#c00"
                speedDisplay.appendChild(speedNumber)
                val speedLabel = document.createElement("div") as HTMLDivElement
                speedLabel.textContent = "Speed"
                speedLabel.style.fontSize = "12px"
                speedLabel.style.color = "#666"
                speedDisplay.appendChild(speedLabel)
                acRow.appendChild(speedDisplay)

                box.appendChild(acRow)

                if (armors.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = "No armor yet."
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    armors.forEach { armor ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val eqIcon = if (armor.isEquipped) "\u2705 " else ""
                        nameSpan.textContent = "$eqIcon${armor.name}"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showArmorModal(character, armor) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { dndArmorRepo.delete(armor.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        // Check warnings
                        val profKey = when (armor.type) {
                            "Light Armor" -> "Light"
                            "Medium Armor" -> "Medium"
                            "Heavy Armor" -> "Heavy"
                            "Shield" -> "Shields"
                            else -> ""
                        }
                        val hasProficiency = armor.type == "Clothes" || profKey in armorProficiencies
                        val hasStrWarning = armor.minimumStrength > 0 && charStrength < armor.minimumStrength

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val warnings = StringBuilder()
                        if (!hasProficiency) warnings.append("\u26A0\uFE0F ")
                        if (hasStrWarning) warnings.append("\u26A0\uFE0F ")
                        val sneakStr = if (armor.hasSneakDisadvantage) " | Sneak Disadv." else ""
                        val strStr = if (armor.minimumStrength > 0) " | Min Str: ${armor.minimumStrength}" else ""
                        if (armor.type == "Clothes") {
                            val featText = if (armor.additionalFeatures.isNotEmpty()) {
                                val truncated = if (armor.additionalFeatures.length > 50) armor.additionalFeatures.take(50) + "..." else armor.additionalFeatures
                                " | $truncated"
                            } else ""
                            line2.textContent = "${armor.type} | ${armor.weight}kg | ${armor.price} ${armor.priceCurrency}$featText"
                        } else {
                            line2.textContent = "$warnings${armor.type} | AC: ${armor.baseAC} (${armor.acModifier})$strStr$sneakStr | ${armor.weight}kg | ${armor.price} ${armor.priceCurrency}"
                        }
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Weapons") {
                val weapons = dndWeaponRepo.getByCharacterId(character.id)

                // Gather weapon proficiencies from features
                val features = dndFeaturesRepo.getByCharacterId(character.id)
                val weaponCatProfs = mutableSetOf<String>()
                val weaponSpecificProfs = mutableSetOf<String>()
                features.filter { it.type == "Weapon/Armor Proficiency" }.forEach { f ->
                    f.description.split(",").map { it.trim() }.forEach { entry ->
                        when {
                            entry.startsWith("weapon_cat:") -> weaponCatProfs.add(entry.removePrefix("weapon_cat:"))
                            entry.startsWith("weapon:") -> weaponSpecificProfs.add(entry.removePrefix("weapon:"))
                        }
                    }
                }

                // Attacks section (equipped weapons)
                val equippedWeapon = weapons.firstOrNull { it.isEquipped }
                if (equippedWeapon != null) {
                    val stats = dndBaseStatsRepo.getByCharacterId(character.id)
                    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
                    val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                    val profBonus = calcProficiency(totalLevel)
                    val strMod = stats?.strValue?.let { calcModifier(it) } ?: 0
                    val dexMod = stats?.dexValue?.let { calcModifier(it) } ?: 0

                    val hasProfEq = equippedWeapon.category in weaponCatProfs
                            || equippedWeapon.weaponType in weaponSpecificProfs
                    val isRanged = equippedWeapon.range && !equippedWeapon.thrown

                    val atkTable = document.createElement("table") as HTMLTableElement
                    atkTable.style.width = "100%"
                    atkTable.style.fontSize = "12px"
                    atkTable.style.borderCollapse = "collapse"
                    atkTable.style.marginBottom = "10px"

                    // Header
                    val thead = document.createElement("thead")
                    val headerRow = document.createElement("tr") as HTMLTableRowElement
                    listOf("Name", "Range", "Test", "Damage", "Notes").forEach { h ->
                        val th = document.createElement("th") as HTMLTableCellElement
                        th.textContent = h
                        th.style.textAlign = "left"
                        th.style.padding = "4px"
                        th.style.borderBottom = "1px solid #ccc"
                        th.style.fontSize = "11px"
                        th.style.color = "#666"
                        headerRow.appendChild(th)
                    }
                    thead.appendChild(headerRow)
                    atkTable.appendChild(thead)

                    val tbody = document.createElement("tbody")

                    fun addAttackRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
                        val mod = if (useDex) dexMod else strMod
                        val atkMod = mod + (if (hasProfEq) profBonus else 0)

                        val tr = document.createElement("tr") as HTMLTableRowElement

                        // Name
                        val tdName = document.createElement("td") as HTMLTableCellElement
                        tdName.style.padding = "3px 4px"
                        tdName.style.fontWeight = "bold"
                        val statLabel = if (equippedWeapon.finesse) " (${if (useDex) "Dex" else "Str"})" else ""
                        val handLabel = if (twoHanded) " [2H]" else ""
                        val thrownLabel = if (thrown) " [Thrown]" else ""
                        tdName.textContent = "${equippedWeapon.name}$statLabel$handLabel$thrownLabel"
                        tr.appendChild(tdName)

                        // Range
                        val tdRange = document.createElement("td") as HTMLTableCellElement
                        tdRange.style.padding = "3px 4px"
                        tdRange.textContent = if (isRanged || thrown) "${equippedWeapon.rangeDistance}/${equippedWeapon.rangeLongDistance}m" else "Melee"
                        tr.appendChild(tdRange)

                        // Test
                        val tdTest = document.createElement("td") as HTMLTableCellElement
                        tdTest.style.padding = "3px 4px"
                        val testSign = if (atkMod >= 0) "+" else ""
                        tdTest.textContent = "$testSign$atkMod"
                        tr.appendChild(tdTest)

                        // Damage
                        val tdDmg = document.createElement("td") as HTMLTableCellElement
                        tdDmg.style.padding = "3px 4px"
                        val dice = if (twoHanded) equippedWeapon.versatileDice else equippedWeapon.damageDice
                        val dmgStr = if (isRanged || thrown) {
                            "$dice ${equippedWeapon.damageType}"
                        } else {
                            val modSign = if (mod >= 0) "+" else ""
                            "$dice$modSign$mod ${equippedWeapon.damageType}"
                        }
                        tdDmg.textContent = dmgStr
                        tr.appendChild(tdDmg)

                        // Notes (icons + additional features)
                        val tdNotes = document.createElement("td") as HTMLTableCellElement
                        tdNotes.style.padding = "3px 4px"
                        tdNotes.style.color = "#888"
                        val icons = mutableListOf<String>()
                        if (equippedWeapon.silver) icons.add("\uD83E\uDD48")
                        if (equippedWeapon.heavy) icons.add("\u2693")
                        if (equippedWeapon.light) icons.add("\uD83E\uDEB6")
                        if (equippedWeapon.reach) icons.add("\uD83D\uDCAB")
                        if (equippedWeapon.ammunition) icons.add("\uD83C\uDFF9")
                        if (equippedWeapon.loading) icons.add("\u23F3")
                        val extras = mutableListOf<String>()
                        if (icons.isNotEmpty()) extras.add(icons.joinToString(""))
                        if (equippedWeapon.additionalFeatures.isNotEmpty()) {
                            val maxLen = 50 - (extras.firstOrNull()?.length ?: 0)
                            val feat = if (equippedWeapon.additionalFeatures.length > maxLen)
                                equippedWeapon.additionalFeatures.take(maxLen) + "..." else equippedWeapon.additionalFeatures
                            extras.add(feat)
                        }
                        tdNotes.textContent = extras.joinToString(" ")
                        tr.appendChild(tdNotes)

                        tbody.appendChild(tr)
                    }

                    if (isRanged) {
                        addAttackRow(useDex = true)
                    } else if (equippedWeapon.finesse) {
                        addAttackRow(useDex = false)
                        if (equippedWeapon.versatile) addAttackRow(useDex = false, twoHanded = true)
                        addAttackRow(useDex = true)
                        if (equippedWeapon.versatile) addAttackRow(useDex = true, twoHanded = true)
                        if (equippedWeapon.thrown) {
                            addAttackRow(useDex = false, thrown = true)
                            addAttackRow(useDex = true, thrown = true)
                        }
                    } else {
                        addAttackRow(useDex = false)
                        if (equippedWeapon.versatile) addAttackRow(useDex = false, twoHanded = true)
                        if (equippedWeapon.thrown) addAttackRow(useDex = false, thrown = true)
                    }

                    atkTable.appendChild(tbody)
                    box.appendChild(atkTable)
                }

                if (weapons.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = "No weapons yet."
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    weapons.sortedByDescending { it.isEquipped }.forEach { weapon ->
                        val hasProficiency = weapon.category in weaponCatProfs
                                || weapon.weaponType in weaponSpecificProfs

                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val eqIcon = if (weapon.isEquipped) "\u2705 " else ""
                        val warnIcon = if (!hasProficiency) "\u26A0\uFE0F " else ""
                        nameSpan.textContent = "$warnIcon$eqIcon${weapon.name}"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showWeaponModal(character, weapon) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { dndWeaponRepo.delete(weapon.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val props = mutableListOf<String>()
                        props.add(weapon.category)
                        props.add("${weapon.damageDice} ${weapon.damageType}")
                        if (weapon.versatile) props.add("Versatile (${weapon.versatileDice})")
                        if (weapon.range) props.add("Range ${weapon.rangeDistance}/${weapon.rangeLongDistance}m")
                        props.add("${weapon.weight}kg")
                        props.add("${weapon.price} ${weapon.priceCurrency}")
                        // Property icons
                        val icons = mutableListOf<String>()
                        if (weapon.silver) icons.add("\uD83E\uDD48") // silver medal
                        if (weapon.heavy) icons.add("\u2693") // anchor = heavy
                        if (weapon.light) icons.add("\uD83E\uDEB6") // feather = light
                        if (weapon.reach) icons.add("\uD83D\uDCAB") // reach
                        if (weapon.ammunition) icons.add("\uD83C\uDFF9") // bow = ammo
                        if (weapon.loading) icons.add("\u23F3") // hourglass = loading
                        if (icons.isNotEmpty()) props.add(icons.joinToString(""))
                        // Features text
                        val featuresText = listOf(weapon.specialDescription, weapon.additionalFeatures)
                            .filter { it.isNotEmpty() }.joinToString(" | ")
                        if (featuresText.isNotEmpty()) {
                            val truncated = if (featuresText.length > 50) featuresText.take(50) + "..." else featuresText
                            props.add(truncated)
                        }
                        line2.textContent = props.joinToString(" | ")
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Magic Items") {
                val magicItems = dndMagicItemRepo.getByCharacterId(character.id)
                    .sortedWith(compareBy<DndMagicItem> {
                        when {
                            it.needSynch && it.isSynched -> 0
                            !it.needSynch -> 1
                            else -> 2
                        }
                    }.thenBy { it.name })

                // Synch counter
                val synchedCount = magicItems.count { it.needSynch && it.isSynched }
                val synchInfo = document.createElement("div") as HTMLDivElement
                synchInfo.style.fontSize = "12px"
                synchInfo.style.marginBottom = "8px"
                synchInfo.style.color = if (synchedCount > 3) "#c00" else "#555"
                synchInfo.textContent = "Synched items: $synchedCount / Max: 3"
                box.appendChild(synchInfo)

                if (magicItems.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = "No magic items yet."
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    magicItems.forEach { item ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val synchIcon = if (!item.needSynch) "\uD83D\uDD35 "
                            else if (item.isSynched) "\u2705 " else ""
                        nameSpan.textContent = "$synchIcon${item.name}"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showMagicItemModal(character, item) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { dndMagicItemRepo.delete(item.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val effectStr = if (item.effect.isNotEmpty()) {
                            val truncated = if (item.effect.length > 80) item.effect.take(80) + "..." else item.effect
                            "$truncated | "
                        } else ""
                        line2.textContent = "$effectStr${item.weight}kg | ${item.price} ${item.priceCurrency}"
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Potions Ammo and Ration") {
                val consumables = dndConsumableRepo.getByCharacterId(character.id)
                if (consumables.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = "No items yet."
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    consumables.forEach { item ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val qtyStr = if (item.quantity > 1) " x${item.quantity}" else ""
                        nameSpan.textContent = "${item.name}$qtyStr"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showConsumableModal(character, item) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { dndConsumableRepo.delete(item.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val effectStr = if (item.effect.isNotEmpty()) {
                            val truncated = if (item.effect.length > 50) item.effect.take(50) + "..." else item.effect
                            " | $truncated"
                        } else ""
                        line2.textContent = "${item.type}$effectStr | ${item.weight}kg | ${item.price} ${item.priceCurrency}"
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else {
            // Key Items, Loot and others
            val items = dndInventoryRepo.getByCategory(character.id, section).sortedBy { it.name }
            if (items.isEmpty()) {
                val placeholder = document.createElement("p") as HTMLParagraphElement
                placeholder.textContent = "No items yet."
                placeholder.style.color = "#999"
                placeholder.style.fontSize = "13px"
                box.appendChild(placeholder)
            } else {
                items.forEach { item ->
                    val row = document.createElement("div") as HTMLDivElement
                    row.style.borderBottom = "1px solid #eee"
                    row.style.padding = "6px 0"
                    row.style.fontSize = "12px"

                    val line1 = document.createElement("div") as HTMLDivElement
                    line1.style.display = "flex"
                    line1.style.justifyContent = "space-between"
                    line1.style.alignItems = "center"

                    val nameSpan = document.createElement("span") as HTMLSpanElement
                    nameSpan.textContent = item.name
                    nameSpan.style.fontWeight = "bold"
                    line1.appendChild(nameSpan)

                    val actions = document.createElement("div") as HTMLDivElement
                    actions.style.display = "flex"
                    actions.style.setProperty("gap", "4px")
                    val editBtn = document.createElement("button") as HTMLButtonElement
                    editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                    editBtn.addEventListener("click", {
                        showInventoryItemModal(character, section, item) { refresh() }
                    })
                    actions.appendChild(editBtn)
                    val delBtn = document.createElement("button") as HTMLButtonElement
                    delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                    delBtn.addEventListener("click", { dndInventoryRepo.delete(item.id); refresh() })
                    actions.appendChild(delBtn)
                    line1.appendChild(actions)
                    row.appendChild(line1)

                    if (item.description.isNotEmpty()) {
                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val truncated = if (item.description.length > 80) item.description.take(80) + "..." else item.description
                        line2.textContent = truncated
                        row.appendChild(line2)
                    }

                    box.appendChild(row)
                }
            }
            } // end else (non-Money)

            grid.appendChild(box)
        }

        container.appendChild(grid)
    }

    refresh()
}

private fun showInventoryItemModal(
    character: Character,
    category: String,
    existing: DndInventoryItem?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "450px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Item" else "Add Item"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text; l.style.fontWeight = "bold"
        form.appendChild(l)
    }

    // Name (full width)
    val nameLbl = document.createElement("label") as HTMLLabelElement
    nameLbl.textContent = "Name"; nameLbl.style.fontWeight = "bold"
    nameLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(nameLbl)
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    nameInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(nameInput)

    // Description
    val descLbl = document.createElement("label") as HTMLLabelElement
    descLbl.textContent = "Description"; descLbl.style.fontWeight = "bold"
    descLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(descLbl)
    val descInput = document.createElement("textarea") as HTMLTextAreaElement
    descInput.value = existing?.description ?: ""
    descInput.rows = 3; descInput.style.width = "100%"
    descInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(descInput)

    // Weight
    lbl("Weight")
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.style.display = "flex"; weightRow.style.alignItems = "center"
    weightRow.style.setProperty("gap", "4px")
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.quantity ?: 0).toString()
    weightInput.style.width = "100%"
    weightRow.appendChild(weightInput)
    val kgLabel = document.createElement("span") as HTMLSpanElement
    kgLabel.textContent = "Kg"; kgLabel.style.fontSize = "12px"; kgLabel.style.color = "#666"
    weightRow.appendChild(kgLabel)
    form.appendChild(weightRow)

    // Price
    lbl("Price")
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = "0"
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    listOf("pc", "ps", "pe", "pg", "pp").forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = c; currSelect.appendChild(o)
    }
    currSelect.value = "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Tags
    val selectedTags = mutableListOf<String>()

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"
    tagsLbl.style.display = "block"; tagsLbl.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val item = DndInventoryItem(
            id = existing?.id ?: 0,
            characterId = character.id,
            category = category,
            name = nameInput.value,
            description = descInput.value,
            quantity = 1,
            equipped = false
        )
        dndInventoryRepo.save(item)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showMagicItemModal(
    character: Character,
    existing: DndMagicItem?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "450px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Magic Item" else "Add Magic Item"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text; l.style.fontWeight = "bold"
        form.appendChild(l)
    }

    // Name
    lbl("Name")
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    form.appendChild(nameInput)

    // Need Synch + Is Synched
    val synchDiv = document.createElement("div") as HTMLDivElement
    synchDiv.style.setProperty("grid-column", "1 / -1")
    synchDiv.style.display = "flex"
    synchDiv.style.setProperty("gap", "16px")

    val needSynchLbl = document.createElement("label") as HTMLLabelElement
    val needSynchCb = document.createElement("input") as HTMLInputElement
    needSynchCb.type = "checkbox"; needSynchCb.checked = existing?.needSynch ?: false
    needSynchCb.style.marginRight = "4px"
    needSynchLbl.appendChild(needSynchCb); needSynchLbl.append("Need Synch")
    needSynchLbl.style.fontWeight = "bold"
    synchDiv.appendChild(needSynchLbl)

    val isSynchedLbl = document.createElement("label") as HTMLLabelElement
    val isSynchedCb = document.createElement("input") as HTMLInputElement
    isSynchedCb.type = "checkbox"; isSynchedCb.checked = existing?.isSynched ?: false
    isSynchedCb.style.marginRight = "4px"
    isSynchedLbl.appendChild(isSynchedCb); isSynchedLbl.append("Is Synched")
    isSynchedLbl.style.fontWeight = "bold"
    synchDiv.appendChild(isSynchedLbl)

    fun updateSynchedVisibility() {
        isSynchedLbl.style.display = if (needSynchCb.checked) "" else "none"
    }
    updateSynchedVisibility()
    needSynchCb.addEventListener("change", { updateSynchedVisibility() })
    form.appendChild(synchDiv)

    // Effect
    val effectLbl = document.createElement("label") as HTMLLabelElement
    effectLbl.textContent = "Effect"; effectLbl.style.fontWeight = "bold"
    effectLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(effectLbl)
    val effectInput = document.createElement("textarea") as HTMLTextAreaElement
    effectInput.value = existing?.effect ?: ""
    effectInput.rows = 3; effectInput.style.width = "100%"
    effectInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(effectInput)

    // Weight
    lbl("Weight")
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.style.display = "flex"; weightRow.style.alignItems = "center"
    weightRow.style.setProperty("gap", "4px")
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightInput.style.width = "100%"
    weightRow.appendChild(weightInput)
    val kgLabel = document.createElement("span") as HTMLSpanElement
    kgLabel.textContent = "Kg"; kgLabel.style.fontSize = "12px"; kgLabel.style.color = "#666"
    weightRow.appendChild(kgLabel)
    form.appendChild(weightRow)

    // Price
    lbl("Price")
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndMagicItem.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = c; currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"
    tagsLbl.style.display = "block"; tagsLbl.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val item = DndMagicItem(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            needSynch = needSynchCb.checked,
            isSynched = isSynchedCb.checked,
            effect = effectInput.value,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            tags = selectedTags.toList()
        )
        dndMagicItemRepo.save(item)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showConsumableModal(
    character: Character,
    existing: DndConsumable?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "450px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Consumable" else "Add Consumable"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text; l.style.fontWeight = "bold"
        form.appendChild(l)
    }

    // Name
    lbl("Name")
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    form.appendChild(nameInput)

    // Type
    lbl("Type")
    val typeSelect = document.createElement("select") as HTMLSelectElement
    DndConsumable.types.forEach { t ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = t; o.textContent = t; typeSelect.appendChild(o)
    }
    typeSelect.value = existing?.type ?: DndConsumable.types.first()
    form.appendChild(typeSelect)

    // Quantity
    lbl("Quantity")
    val qtyInput = document.createElement("input") as HTMLInputElement
    qtyInput.type = "number"; qtyInput.min = "0"
    qtyInput.value = (existing?.quantity ?: 1).toString()
    form.appendChild(qtyInput)

    // Weight per unit
    lbl("Weight per unit")
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.style.display = "flex"; weightRow.style.alignItems = "center"
    weightRow.style.setProperty("gap", "4px")
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightInput.style.width = "100%"
    weightRow.appendChild(weightInput)
    val kgLabel = document.createElement("span") as HTMLSpanElement
    kgLabel.textContent = "Kg"; kgLabel.style.fontSize = "12px"; kgLabel.style.color = "#666"
    weightRow.appendChild(kgLabel)
    form.appendChild(weightRow)

    // Price per unit
    lbl("Price per unit")
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndConsumable.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = c; currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Effect
    val effectLbl = document.createElement("label") as HTMLLabelElement
    effectLbl.textContent = "Effect"; effectLbl.style.fontWeight = "bold"
    effectLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(effectLbl)
    val effectInput = document.createElement("textarea") as HTMLTextAreaElement
    effectInput.value = existing?.effect ?: ""
    effectInput.rows = 3; effectInput.style.width = "100%"
    effectInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(effectInput)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"
    tagsLbl.style.display = "block"; tagsLbl.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val item = DndConsumable(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            type = typeSelect.value,
            quantity = maxOf(0, qtyInput.value.toIntOrNull() ?: 1),
            effect = effectInput.value,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            tags = selectedTags.toList()
        )
        dndConsumableRepo.save(item)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showWeaponModal(
    character: Character,
    existing: DndWeapon?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "550px"; modal.style.width = "90%"
    modal.style.maxHeight = "85vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Weapon" else "Add Weapon"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text; l.style.fontWeight = "bold"
        form.appendChild(l)
    }

    // Name
    lbl("Name")
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    form.appendChild(nameInput)

    // Category
    lbl("Category")
    val catSelect = document.createElement("select") as HTMLSelectElement
    DndWeapon.categories.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = c; catSelect.appendChild(o)
    }
    catSelect.value = existing?.category ?: DndWeapon.categories.first()
    form.appendChild(catSelect)

    // Weapon Type
    lbl("Weapon Type")
    val typeSelect = document.createElement("select") as HTMLSelectElement
    val emptyTypeOpt = document.createElement("option") as HTMLOptionElement
    emptyTypeOpt.value = ""; emptyTypeOpt.textContent = "-- Select --"
    typeSelect.appendChild(emptyTypeOpt)
    DndWeapon.weaponTypes.forEach { t ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = t; o.textContent = t; typeSelect.appendChild(o)
    }
    val customTypeOpt = document.createElement("option") as HTMLOptionElement
    customTypeOpt.value = "__custom__"; customTypeOpt.textContent = "Custom..."
    typeSelect.appendChild(customTypeOpt)
    if (existing != null && existing.weaponType.isNotEmpty() && existing.weaponType !in DndWeapon.weaponTypes) {
        val existOpt = document.createElement("option") as HTMLOptionElement
        existOpt.value = existing.weaponType; existOpt.textContent = existing.weaponType
        typeSelect.insertBefore(existOpt, customTypeOpt)
    }
    typeSelect.value = existing?.weaponType ?: ""
    form.appendChild(typeSelect)

    // Custom weapon type input (hidden by default)
    val customTypeInput = document.createElement("input") as HTMLInputElement
    customTypeInput.placeholder = "Custom weapon type"
    customTypeInput.style.display = "none"
    customTypeInput.style.setProperty("grid-column", "1 / -1")
    typeSelect.addEventListener("change", {
        customTypeInput.style.display = if (typeSelect.value == "__custom__") "block" else "none"
    })
    form.appendChild(customTypeInput)

    // Damage Dice
    lbl("Damage Dice")
    val dmgDiceInput = document.createElement("input") as HTMLInputElement
    dmgDiceInput.value = existing?.damageDice ?: ""
    dmgDiceInput.placeholder = "e.g. 1d8"
    form.appendChild(dmgDiceInput)

    // Damage Type
    lbl("Damage Type")
    val dmgTypeSelect = document.createElement("select") as HTMLSelectElement
    DndWeapon.damageTypes.forEach { d ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = d; o.textContent = d; dmgTypeSelect.appendChild(o)
    }
    dmgTypeSelect.value = existing?.damageType ?: DndWeapon.damageTypes.first()
    form.appendChild(dmgTypeSelect)

    // Properties checkboxes section
    val propsLbl = document.createElement("label") as HTMLLabelElement
    propsLbl.textContent = "Properties"; propsLbl.style.fontWeight = "bold"
    propsLbl.style.setProperty("grid-column", "1 / -1")
    propsLbl.style.marginTop = "8px"
    form.appendChild(propsLbl)

    val propsGrid = document.createElement("div") as HTMLDivElement
    propsGrid.style.setProperty("grid-column", "1 / -1")
    propsGrid.style.setProperty("display", "grid")
    propsGrid.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
    propsGrid.style.setProperty("gap", "6px")

    fun addCheckbox(label: String, checked: Boolean): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"; cb.checked = checked
        cb.style.marginRight = "4px"
        lbl.appendChild(cb); lbl.append(label)
        lbl.style.fontSize = "13px"
        propsGrid.appendChild(lbl)
        return cb
    }

    val ammoCb = addCheckbox("Ammunition", existing?.ammunition ?: false)
    val finesseCb = addCheckbox("Finesse", existing?.finesse ?: false)
    val heavyCb = addCheckbox("Heavy", existing?.heavy ?: false)
    val lightCb = addCheckbox("Light", existing?.light ?: false)
    val loadingCb = addCheckbox("Loading", existing?.loading ?: false)
    val rangeCb = addCheckbox("Range", existing?.range ?: false)
    val reachCb = addCheckbox("Reach", existing?.reach ?: false)
    val specialCb = addCheckbox("Special", existing?.special ?: false)
    val thrownCb = addCheckbox("Throw", existing?.thrown ?: false)
    val twoHandedCb = addCheckbox("Two-handed", existing?.twoHanded ?: false)
    val versatileCb = addCheckbox("Versatile", existing?.versatile ?: false)
    val silverCb = addCheckbox("Silver", existing?.silver ?: false)

    form.appendChild(propsGrid)

    // Range fields (shown when Range is checked)
    val rangeDiv = document.createElement("div") as HTMLDivElement
    rangeDiv.style.setProperty("grid-column", "1 / -1")
    rangeDiv.style.setProperty("display", "grid")
    rangeDiv.style.setProperty("grid-template-columns", "1fr 1fr")
    rangeDiv.style.setProperty("gap", "10px")

    val rangeLbl1 = document.createElement("label") as HTMLLabelElement
    rangeLbl1.textContent = "Range (m)"; rangeLbl1.style.fontWeight = "bold"
    rangeDiv.appendChild(rangeLbl1)
    val rangeLbl2 = document.createElement("label") as HTMLLabelElement
    rangeLbl2.textContent = "Long Range (m)"; rangeLbl2.style.fontWeight = "bold"
    rangeDiv.appendChild(rangeLbl2)
    val rangeInput = document.createElement("input") as HTMLInputElement
    rangeInput.type = "number"; rangeInput.min = "0"
    rangeInput.value = (existing?.rangeDistance ?: 0).toString()
    rangeDiv.appendChild(rangeInput)
    val rangeLongInput = document.createElement("input") as HTMLInputElement
    rangeLongInput.type = "number"; rangeLongInput.min = "0"
    rangeLongInput.value = (existing?.rangeLongDistance ?: 0).toString()
    rangeDiv.appendChild(rangeLongInput)

    fun updateRangeVisibility() {
        rangeDiv.style.display = if (rangeCb.checked || thrownCb.checked) "grid" else "none"
    }
    updateRangeVisibility()
    rangeCb.addEventListener("change", { updateRangeVisibility() })
    thrownCb.addEventListener("change", { updateRangeVisibility() })
    form.appendChild(rangeDiv)

    // Special description (shown when Special is checked)
    val specialDiv = document.createElement("div") as HTMLDivElement
    specialDiv.style.setProperty("grid-column", "1 / -1")
    val specialLbl = document.createElement("label") as HTMLLabelElement
    specialLbl.textContent = "Special Description"; specialLbl.style.fontWeight = "bold"
    specialDiv.appendChild(specialLbl)
    val specialInput = document.createElement("textarea") as HTMLTextAreaElement
    specialInput.value = existing?.specialDescription ?: ""
    specialInput.rows = 2; specialInput.style.width = "100%"
    specialDiv.appendChild(specialInput)

    fun updateSpecialVisibility() {
        specialDiv.style.display = if (specialCb.checked) "block" else "none"
    }
    updateSpecialVisibility()
    specialCb.addEventListener("change", { updateSpecialVisibility() })
    form.appendChild(specialDiv)

    // Versatile dice (shown when Versatile is checked)
    val versatileDiv = document.createElement("div") as HTMLDivElement
    versatileDiv.style.setProperty("grid-column", "1 / -1")
    val versatileLbl = document.createElement("label") as HTMLLabelElement
    versatileLbl.textContent = "Two-handed Damage Dice"; versatileLbl.style.fontWeight = "bold"
    versatileDiv.appendChild(versatileLbl)
    val versatileInput = document.createElement("input") as HTMLInputElement
    versatileInput.value = existing?.versatileDice ?: ""
    versatileInput.placeholder = "e.g. 1d10"
    versatileInput.style.width = "100%"
    versatileDiv.appendChild(versatileInput)

    fun updateVersatileVisibility() {
        versatileDiv.style.display = if (versatileCb.checked) "block" else "none"
    }
    updateVersatileVisibility()
    versatileCb.addEventListener("change", { updateVersatileVisibility() })
    form.appendChild(versatileDiv)

    // Additional Features
    val addFeatLbl = document.createElement("label") as HTMLLabelElement
    addFeatLbl.textContent = "Additional Features"; addFeatLbl.style.fontWeight = "bold"
    addFeatLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(addFeatLbl)
    val addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
    addFeatInput.value = existing?.additionalFeatures ?: ""
    addFeatInput.rows = 2; addFeatInput.style.width = "100%"
    addFeatInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(addFeatInput)

    // Weight
    lbl("Weight")
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.style.display = "flex"; weightRow.style.alignItems = "center"
    weightRow.style.setProperty("gap", "4px")
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightInput.style.width = "100%"
    weightRow.appendChild(weightInput)
    val kgSpan = document.createElement("span") as HTMLSpanElement
    kgSpan.textContent = "Kg"; kgSpan.style.fontSize = "12px"; kgSpan.style.color = "#666"
    weightRow.appendChild(kgSpan)
    form.appendChild(weightRow)

    // Price
    lbl("Price")
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndWeapon.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = c; currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"
    tagsLbl.style.display = "block"; tagsLbl.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

    // Equipped
    val eqContainer = document.createElement("div") as HTMLDivElement
    eqContainer.style.setProperty("grid-column", "1 / -1")
    val eqLbl = document.createElement("label") as HTMLLabelElement
    val eqCb = document.createElement("input") as HTMLInputElement
    eqCb.type = "checkbox"; eqCb.checked = existing?.isEquipped ?: false
    eqCb.style.marginRight = "6px"
    eqLbl.appendChild(eqCb); eqLbl.append("Equipped")
    eqLbl.style.fontWeight = "bold"
    eqContainer.appendChild(eqLbl)
    form.appendChild(eqContainer)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val weaponType = if (typeSelect.value == "__custom__") customTypeInput.value.trim() else typeSelect.value
        val weapon = DndWeapon(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            category = catSelect.value,
            weaponType = weaponType,
            damageDice = dmgDiceInput.value,
            damageType = dmgTypeSelect.value,
            ammunition = ammoCb.checked,
            finesse = finesseCb.checked,
            heavy = heavyCb.checked,
            light = lightCb.checked,
            loading = loadingCb.checked,
            range = rangeCb.checked,
            rangeDistance = rangeInput.value.toIntOrNull() ?: 0,
            rangeLongDistance = rangeLongInput.value.toIntOrNull() ?: 0,
            reach = reachCb.checked,
            special = specialCb.checked,
            specialDescription = specialInput.value,
            thrown = thrownCb.checked,
            twoHanded = twoHandedCb.checked,
            versatile = versatileCb.checked,
            versatileDice = versatileInput.value,
            silver = silverCb.checked,
            additionalFeatures = addFeatInput.value,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            tags = selectedTags.toList(),
            isEquipped = eqCb.checked
        )
        if (weapon.isEquipped) {
            dndWeaponRepo.getByCharacterId(character.id).forEach { other ->
                if (other.id != weapon.id && other.isEquipped) {
                    dndWeaponRepo.save(other.copy(isEquipped = false))
                }
            }
        }
        dndWeaponRepo.save(weapon)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showArmorModal(
    character: Character,
    existing: DndArmor?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "450px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Armor" else "Add Armor"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text; l.style.fontWeight = "bold"
        form.appendChild(l)
    }

    // Name
    lbl("Name")
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    form.appendChild(nameInput)

    // Type
    lbl("Type")
    val typeSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.types.forEach { t ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = t; o.textContent = t; typeSelect.appendChild(o)
    }
    typeSelect.value = existing?.type ?: DndArmor.types.first()
    form.appendChild(typeSelect)

    // Base AC
    val acLbl = document.createElement("label") as HTMLLabelElement
    acLbl.textContent = "Base AC"; acLbl.style.fontWeight = "bold"
    form.appendChild(acLbl)
    val acInput = document.createElement("input") as HTMLInputElement
    acInput.type = "number"; acInput.value = (existing?.baseAC ?: 10).toString()
    form.appendChild(acInput)

    // AC Modifier
    // AC Modifier
    val acModLbl = document.createElement("label") as HTMLLabelElement
    acModLbl.textContent = "AC Modifier"; acModLbl.style.fontWeight = "bold"
    form.appendChild(acModLbl)
    val acModSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.acModifiers.forEach { m ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = m; o.textContent = m; acModSelect.appendChild(o)
    }
    acModSelect.value = existing?.acModifier ?: "none"
    form.appendChild(acModSelect)

    // Minimum Strength
    val minStrLbl = document.createElement("label") as HTMLLabelElement
    minStrLbl.textContent = "Min. Strength"; minStrLbl.style.fontWeight = "bold"
    form.appendChild(minStrLbl)
    val minStrInput = document.createElement("input") as HTMLInputElement
    minStrInput.type = "number"; minStrInput.min = "0"; minStrInput.max = "20"
    minStrInput.value = (existing?.minimumStrength ?: 0).toString()
    form.appendChild(minStrInput)

    // Sneak Disadvantage
    val sneakEmptyLbl = document.createElement("label") as HTMLLabelElement
    sneakEmptyLbl.textContent = ""
    form.appendChild(sneakEmptyLbl)
    val sneakLbl = document.createElement("label") as HTMLLabelElement
    val sneakCb = document.createElement("input") as HTMLInputElement
    sneakCb.type = "checkbox"; sneakCb.checked = existing?.hasSneakDisadvantage ?: false
    sneakCb.style.marginRight = "6px"
    sneakLbl.appendChild(sneakCb); sneakLbl.append("Sneak Disadvantage")
    form.appendChild(sneakLbl)

    // Visibility logic based on type
    fun updateArmorFieldVisibility() {
        val isShield = typeSelect.value == "Shield"
        val isClothes = typeSelect.value == "Clothes"
        val isHeavy = typeSelect.value == "Heavy Armor"
        val hideAC = isShield || isClothes
        acLbl.style.display = if (hideAC) "none" else ""
        acInput.style.display = if (hideAC) "none" else ""
        acModLbl.style.display = if (hideAC) "none" else ""
        acModSelect.style.display = if (hideAC) "none" else ""
        minStrLbl.style.display = if (!isHeavy) "none" else ""
        minStrInput.style.display = if (!isHeavy) "none" else ""
        sneakEmptyLbl.style.display = if (isShield || isClothes) "none" else ""
        sneakLbl.style.display = if (isShield || isClothes) "none" else ""
    }
    updateArmorFieldVisibility()
    typeSelect.addEventListener("change", { updateArmorFieldVisibility() })

    // Weight
    lbl("Weight")
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.style.display = "flex"; weightRow.style.alignItems = "center"
    weightRow.style.setProperty("gap", "4px")
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightInput.style.width = "100%"
    weightRow.appendChild(weightInput)
    val kgLabel = document.createElement("span") as HTMLSpanElement
    kgLabel.textContent = "Kg"; kgLabel.style.fontSize = "12px"; kgLabel.style.color = "#666"
    weightRow.appendChild(kgLabel)
    form.appendChild(weightRow)

    // Price
    lbl("Price")
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = c; currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Additional Features
    val addFeatLbl = document.createElement("label") as HTMLLabelElement
    addFeatLbl.textContent = "Additional Features"; addFeatLbl.style.fontWeight = "bold"
    addFeatLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(addFeatLbl)
    val addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
    addFeatInput.value = existing?.additionalFeatures ?: ""
    addFeatInput.rows = 3; addFeatInput.style.width = "100%"
    addFeatInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(addFeatInput)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = "Tags"; tagsLbl.style.fontWeight = "bold"
    tagsLbl.style.display = "block"; tagsLbl.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshArmorTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshArmorTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshArmorTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshArmorTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

    // Equipped (last field)
    val eqContainer = document.createElement("div") as HTMLDivElement
    eqContainer.style.setProperty("grid-column", "1 / -1")
    val eqLbl = document.createElement("label") as HTMLLabelElement
    val eqCb = document.createElement("input") as HTMLInputElement
    eqCb.type = "checkbox"; eqCb.checked = existing?.isEquipped ?: false
    eqCb.style.marginRight = "6px"
    eqLbl.appendChild(eqCb); eqLbl.append("Equipped")
    eqLbl.style.fontWeight = "bold"
    eqContainer.appendChild(eqLbl)
    form.appendChild(eqContainer)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val armor = DndArmor(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            type = typeSelect.value,
            baseAC = acInput.value.toIntOrNull() ?: 10,
            acModifier = acModSelect.value,
            minimumStrength = minStrInput.value.toIntOrNull() ?: 0,
            hasSneakDisadvantage = sneakCb.checked,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            isEquipped = eqCb.checked,
            tags = selectedTags.toList(),
            additionalFeatures = addFeatInput.value
        )
        // Unequip other items in same slot if equipping this one
        // Slots: Armor (Light/Medium/Heavy), Shield, Clothes
        if (armor.isEquipped) {
            val allArmors = dndArmorRepo.getByCharacterId(character.id)
            val slot = when (armor.type) {
                "Shield" -> "Shield"
                "Clothes" -> "Clothes"
                else -> "Armor"
            }
            allArmors.forEach { other ->
                if (other.id != armor.id && other.isEquipped) {
                    val otherSlot = when (other.type) {
                        "Shield" -> "Shield"
                        "Clothes" -> "Clothes"
                        else -> "Armor"
                    }
                    if (slot == otherSlot) {
                        dndArmorRepo.save(other.copy(isEquipped = false))
                    }
                }
            }
        }
        dndArmorRepo.save(armor)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showChangeWeaponModal(
    character: Character,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "400px"; modal.style.width = "90%"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = "Change Weapon"
    modal.appendChild(titleEl)

    val weapons = dndWeaponRepo.getByCharacterId(character.id)
    val currentEquipped = weapons.firstOrNull { it.isEquipped }

    val select = document.createElement("select") as HTMLSelectElement
    select.style.width = "100%"; select.style.padding = "8px"; select.style.fontSize = "14px"

    val noneOpt = document.createElement("option") as HTMLOptionElement
    noneOpt.value = ""; noneOpt.textContent = "-- None --"
    select.appendChild(noneOpt)

    weapons.forEach { w ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = w.id.toString()
        opt.textContent = "${w.name} (${w.damageDice} ${w.damageType})"
        select.appendChild(opt)
    }
    select.value = currentEquipped?.id?.toString() ?: ""
    modal.appendChild(select)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val selectedId = select.value.toLongOrNull()
        // Unequip all
        weapons.filter { it.isEquipped }.forEach { dndWeaponRepo.save(it.copy(isEquipped = false)) }
        // Equip selected
        if (selectedId != null) {
            val toEquip = weapons.firstOrNull { it.id == selectedId }
            if (toEquip != null) dndWeaponRepo.save(toEquip.copy(isEquipped = true))
        }
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun renderDndPlayingTab(character: Character, container: HTMLDivElement) {
    val stats = dndBaseStatsRepo.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
    val maxLife = stats.maxLife ?: 0

    val lifeKey = "dnd_playing_life_${character.id}"
    val tempLifeKey = "dnd_playing_templife_${character.id}"
    val deathSaveKey = "dnd_playing_deathsave_${character.id}"
    val deathSuccessKey = "dnd_playing_deathsuccess_${character.id}"
    val deathFailKey = "dnd_playing_deathfail_${character.id}"
    var currentLife = localStorage.getItem(lifeKey)?.toIntOrNull() ?: maxLife
    var currentTempLife = localStorage.getItem(tempLifeKey)?.toIntOrNull() ?: 0
    val isInDeathSaves = localStorage.getItem(deathSaveKey) == "true"
    var deathSuccesses = localStorage.getItem(deathSuccessKey)?.toIntOrNull() ?: 0
    var deathFails = localStorage.getItem(deathFailKey)?.toIntOrNull() ?: 0

    fun saveLife() { localStorage.setItem(lifeKey, currentLife.toString()) }
    fun saveTempLife() { localStorage.setItem(tempLifeKey, currentTempLife.toString()) }
    fun saveDeathState() {
        localStorage.setItem(deathSaveKey, isInDeathSaves.toString())
        localStorage.setItem(deathSuccessKey, deathSuccesses.toString())
        localStorage.setItem(deathFailKey, deathFails.toString())
    }

    // Layout: 3/4 left + 1/4 right
    val layout = document.createElement("div") as HTMLDivElement
    layout.style.display = "flex"
    layout.style.setProperty("gap", "16px")

    val leftPanel = document.createElement("div") as HTMLDivElement
    leftPanel.style.setProperty("flex", "3")

    val rightPanel = document.createElement("div") as HTMLDivElement
    rightPanel.style.setProperty("flex", "1")

    // === LEFT PANEL ===

    // Life Tracker box
    val lifeBox = document.createElement("div") as HTMLDivElement
    lifeBox.style.border = "1px solid #ccc"
    lifeBox.style.borderRadius = "8px"
    lifeBox.style.padding = "12px"
    lifeBox.style.marginBottom = "16px"

    val lifeBoxTitle = document.createElement("h4") as HTMLHeadingElement
    lifeBoxTitle.textContent = "Life Tracker"
    lifeBoxTitle.style.margin = "0 0 10px 0"
    lifeBox.appendChild(lifeBoxTitle)

    // Life Points section
    val lifeSection = document.createElement("div") as HTMLDivElement
    lifeSection.style.display = "flex"
    lifeSection.style.setProperty("gap", "24px")
    lifeSection.style.alignItems = "center"
    lifeSection.style.marginBottom = "12px"

    val lifeCol = document.createElement("div") as HTMLDivElement
    lifeCol.style.textAlign = "center"
    val lifeLbl = document.createElement("div") as HTMLDivElement
    lifeLbl.textContent = "Life Points"
    lifeLbl.style.fontWeight = "bold"; lifeLbl.style.fontSize = "12px"; lifeLbl.style.marginBottom = "4px"
    lifeCol.appendChild(lifeLbl)
    val lifeVal = document.createElement("span") as HTMLSpanElement
    lifeVal.textContent = currentLife.toString()
    lifeVal.style.fontSize = "28px"; lifeVal.style.fontWeight = "bold"
    lifeCol.appendChild(lifeVal)
    lifeSection.appendChild(lifeCol)

    val sep = document.createElement("span") as HTMLSpanElement
    sep.textContent = "/"; sep.style.fontSize = "24px"; sep.style.color = "#666"
    lifeSection.appendChild(sep)

    val maxCol = document.createElement("div") as HTMLDivElement
    maxCol.style.textAlign = "center"
    val maxLbl = document.createElement("div") as HTMLDivElement
    maxLbl.textContent = "Max"
    maxLbl.style.fontWeight = "bold"; maxLbl.style.fontSize = "12px"; maxLbl.style.marginBottom = "4px"
    maxCol.appendChild(maxLbl)
    val maxValSpan = document.createElement("span") as HTMLSpanElement
    maxValSpan.textContent = maxLife.toString()
    maxValSpan.style.fontSize = "28px"; maxValSpan.style.fontWeight = "bold"
    maxCol.appendChild(maxValSpan)
    lifeSection.appendChild(maxCol)

    val sep2 = document.createElement("span") as HTMLSpanElement
    sep2.textContent = "+"; sep2.style.fontSize = "24px"; sep2.style.color = "#666"
    lifeSection.appendChild(sep2)

    val tempCol = document.createElement("div") as HTMLDivElement
    tempCol.style.textAlign = "center"
    val tempLbl = document.createElement("div") as HTMLDivElement
    tempLbl.textContent = "Temp HP"
    tempLbl.style.fontWeight = "bold"; tempLbl.style.fontSize = "12px"; tempLbl.style.marginBottom = "4px"
    tempCol.appendChild(tempLbl)
    val tempVal = document.createElement("span") as HTMLSpanElement
    tempVal.textContent = currentTempLife.toString()
    tempVal.style.fontSize = "28px"; tempVal.style.fontWeight = "bold"
    tempCol.appendChild(tempVal)
    lifeSection.appendChild(tempCol)

    lifeBox.appendChild(lifeSection)

    var deathBtnRef: HTMLButtonElement? = null

    fun updateDisplay() {
        lifeVal.textContent = currentLife.toString()
        tempVal.textContent = currentTempLife.toString()
        deathBtnRef?.disabled = currentLife > 0
    }

    fun showValueModal(title: String, onConfirm: (Int) -> Unit) {
        val overlay = document.createElement("div") as HTMLDivElement
        overlay.style.position = "fixed"
        overlay.style.top = "0"; overlay.style.left = "0"
        overlay.style.width = "100%"; overlay.style.height = "100%"
        overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
        overlay.style.display = "flex"
        overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
        overlay.style.setProperty("z-index", "1000")

        val modal = document.createElement("div") as HTMLDivElement
        modal.style.backgroundColor = "white"
        modal.style.borderRadius = "8px"; modal.style.padding = "24px"
        modal.style.maxWidth = "300px"; modal.style.width = "90%"

        val titleEl = document.createElement("h3") as HTMLHeadingElement
        titleEl.textContent = title
        modal.appendChild(titleEl)

        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"; input.min = "0"; input.value = "0"
        input.style.width = "100%"; input.style.padding = "8px"
        input.style.fontSize = "18px"; input.style.textAlign = "center"
        modal.appendChild(input)

        val btnRow = document.createElement("div") as HTMLDivElement
        btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
        btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

        val cancelBtn = document.createElement("button") as HTMLButtonElement
        cancelBtn.textContent = "Cancel"
        cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
        btnRow.appendChild(cancelBtn)

        val confirmBtn = document.createElement("button") as HTMLButtonElement
        confirmBtn.textContent = "Confirm"
        confirmBtn.addEventListener("click", {
            val value = input.value.toIntOrNull() ?: 0
            if (value > 0) onConfirm(value)
            document.body?.removeChild(overlay)
        })
        btnRow.appendChild(confirmBtn)

        modal.appendChild(btnRow)
        overlay.appendChild(modal)
        document.body?.appendChild(overlay)
        input.focus()
    }

    // Buttons
    val btnSection = document.createElement("div") as HTMLDivElement
    btnSection.style.display = "flex"
    btnSection.style.setProperty("gap", "8px")
    btnSection.style.marginBottom = "8px"

    val btnSection2 = document.createElement("div") as HTMLDivElement
    btnSection2.style.display = "flex"
    btnSection2.style.setProperty("gap", "8px")
    btnSection2.style.marginBottom = "16px"

    if (isInDeathSaves) {
        // Death Saving Throws mode
        lifeSection.style.display = "none"

        // Auto-stabilize on 3 successes
        if (deathSuccesses >= 3) {
            localStorage.setItem(deathSaveKey, "false")
            localStorage.setItem(deathSuccessKey, "0")
            localStorage.setItem(deathFailKey, "0")
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
            return
        }

        val deathSection = document.createElement("div") as HTMLDivElement
        deathSection.style.marginBottom = "12px"

        val deathTitle = document.createElement("div") as HTMLDivElement
        deathTitle.textContent = "\u2620\uFE0F Death Saving Throws"
        deathTitle.style.fontWeight = "bold"
        deathTitle.style.marginBottom = "10px"
        deathSection.appendChild(deathTitle)

        // Grid for aligned rows
        val deathGrid = document.createElement("div") as HTMLDivElement
        deathGrid.style.setProperty("display", "grid")
        deathGrid.style.setProperty("grid-template-columns", "100px 1fr")
        deathGrid.style.setProperty("gap", "6px 12px")
        deathGrid.style.alignItems = "center"
        deathGrid.style.marginBottom = "8px"

        // Success row
        val successLabel = document.createElement("span") as HTMLSpanElement
        successLabel.textContent = "Successes"
        successLabel.style.fontSize = "14px"
        successLabel.style.fontWeight = "bold"
        deathGrid.appendChild(successLabel)
        val successIcons = document.createElement("span") as HTMLSpanElement
        successIcons.style.fontSize = "20px"
        successIcons.textContent = (1..3).joinToString(" ") { i -> if (i <= deathSuccesses) "\u2764\uFE0F" else "\u2B1C" }
        deathGrid.appendChild(successIcons)

        // Fail row
        val failLabel = document.createElement("span") as HTMLSpanElement
        failLabel.textContent = "Failures"
        failLabel.style.fontSize = "14px"
        failLabel.style.fontWeight = "bold"
        deathGrid.appendChild(failLabel)
        val failIcons = document.createElement("span") as HTMLSpanElement
        failIcons.style.fontSize = "20px"
        failIcons.textContent = (1..3).joinToString(" ") { i -> if (i <= deathFails) "\u2620\uFE0F" else "\u2B1C" }
        deathGrid.appendChild(failIcons)

        deathSection.appendChild(deathGrid)
        lifeBox.insertBefore(deathSection, lifeBox.children[1])

        // Mark Fail button
        val dmgBtn = document.createElement("button") as HTMLButtonElement
        dmgBtn.textContent = "\u2620\uFE0F Mark Fail"
        dmgBtn.disabled = deathFails >= 3
        dmgBtn.addEventListener("click", {
            deathFails = minOf(deathFails + 1, 3)
            localStorage.setItem(deathFailKey, deathFails.toString())
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection.appendChild(dmgBtn)

        // Success button
        val successBtn = document.createElement("button") as HTMLButtonElement
        successBtn.textContent = "\u2764\uFE0F Success"
        successBtn.disabled = deathSuccesses >= 3
        successBtn.addEventListener("click", {
            deathSuccesses = minOf(deathSuccesses + 1, 3)
            localStorage.setItem(deathSuccessKey, deathSuccesses.toString())
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection.appendChild(successBtn)

        // Stable Creature button
        val stableBtn = document.createElement("button") as HTMLButtonElement
        stableBtn.textContent = "\uD83D\uDC9A Stable"
        stableBtn.addEventListener("click", {
            localStorage.setItem(deathSaveKey, "false")
            localStorage.setItem(deathSuccessKey, "0")
            localStorage.setItem(deathFailKey, "0")
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection.appendChild(stableBtn)
    } else {
        // Normal mode
        val cureBtn = document.createElement("button") as HTMLButtonElement
        cureBtn.textContent = "\u2764\uFE0F Cure"
        cureBtn.addEventListener("click", {
            showValueModal("Cure") { value ->
                currentLife = minOf(currentLife + value, maxLife)
                saveLife(); updateDisplay()
            }
        })
        btnSection.appendChild(cureBtn)

        val dmgBtn = document.createElement("button") as HTMLButtonElement
        dmgBtn.textContent = "\u2694\uFE0F Dmg"
        dmgBtn.addEventListener("click", {
            showValueModal("Damage") { value ->
                var remaining = value
                if (currentTempLife > 0) {
                    val absorbed = minOf(remaining, currentTempLife)
                    currentTempLife -= absorbed
                    remaining -= absorbed
                    saveTempLife()
                }
                if (remaining > 0) {
                    currentLife = maxOf(currentLife - remaining, 0)
                    saveLife()
                }
                updateDisplay()
            }
        })
        btnSection.appendChild(dmgBtn)

        val tempBtn = document.createElement("button") as HTMLButtonElement
        tempBtn.textContent = "\uD83D\uDEE1\uFE0F Temp HP"
        tempBtn.addEventListener("click", {
            showValueModal("Add Temporary HP") { value ->
                currentTempLife += value
                saveTempLife(); updateDisplay()
            }
        })
        btnSection.appendChild(tempBtn)

        // Second row: Reset + Death Saves
        val resetBtn = document.createElement("button") as HTMLButtonElement
        resetBtn.textContent = "\u21BA Reset"
        resetBtn.addEventListener("click", {
            currentLife = maxLife
            currentTempLife = 0
            saveLife(); saveTempLife(); updateDisplay()
        })
        btnSection2.appendChild(resetBtn)

        val deathBtn = document.createElement("button") as HTMLButtonElement
        deathBtn.textContent = "\u2620\uFE0F"
        deathBtn.disabled = currentLife > 0
        deathBtn.addEventListener("click", {
            localStorage.setItem(deathSaveKey, "true")
            localStorage.setItem(deathSuccessKey, "0")
            localStorage.setItem(deathFailKey, "0")
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection2.appendChild(deathBtn)
        deathBtnRef = deathBtn
    }

    lifeBox.appendChild(btnSection)
    if (!isInDeathSaves) {
        lifeBox.appendChild(btnSection2)
    }

    // Hit Dice section
    val hitDiceSection = document.createElement("div") as HTMLDivElement
    hitDiceSection.style.borderTop = "1px solid #ccc"
    hitDiceSection.style.marginTop = "12px"
    hitDiceSection.style.paddingTop = "12px"

    val hitDiceTitle = document.createElement("h5") as HTMLHeadingElement
    hitDiceTitle.textContent = "Life Hit Dice"
    hitDiceTitle.style.margin = "0 0 8px 0"
    hitDiceSection.appendChild(hitDiceTitle)

    // Build hit dice info per class
    data class HitDiceInfo(val className: String, val die: String, val maxDice: Int, val storageKey: String)

    val hitDiceInfos = mutableListOf<HitDiceInfo>()
    val mainClass = mainInfo?.mainClass
    val mainLevel = mainInfo?.mainClassLevel ?: 0
    if (mainClass != null && mainLevel > 0) {
        hitDiceInfos.add(HitDiceInfo(mainClass, DungeonsAndDragons.hitDieFor(mainClass), mainLevel, "dnd_hitdice_main_${character.id}"))
    }
    val secClass = mainInfo?.secondaryClass
    val secLevel = mainInfo?.secondaryClassLevel ?: 0
    if (secClass != null && secLevel > 0) {
        hitDiceInfos.add(HitDiceInfo(secClass, DungeonsAndDragons.hitDieFor(secClass), secLevel, "dnd_hitdice_sec_${character.id}"))
    }

    if (hitDiceInfos.isEmpty()) {
        val noInfo = document.createElement("p") as HTMLParagraphElement
        noInfo.textContent = "Set class and level in Main tab."
        noInfo.style.color = "#999"; noInfo.style.fontSize = "12px"
        hitDiceSection.appendChild(noInfo)
    } else {
        hitDiceInfos.forEach { hd ->
            var usedDice = localStorage.getItem(hd.storageKey)?.toIntOrNull() ?: 0
            val remaining = hd.maxDice - usedDice

            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.alignItems = "center"
            row.style.setProperty("gap", "8px")
            row.style.marginBottom = "6px"

            val infoSpan = document.createElement("span") as HTMLSpanElement
            infoSpan.textContent = "${hd.className} (${hd.die}): $remaining / ${hd.maxDice}"
            infoSpan.style.fontSize = "13px"
            infoSpan.style.fontWeight = "bold"
            if (remaining <= 0) infoSpan.style.color = "#c00"
            row.appendChild(infoSpan)

            val useHdBtn = document.createElement("button") as HTMLButtonElement
            useHdBtn.textContent = "Use"
            useHdBtn.style.fontSize = "11px"
            useHdBtn.disabled = remaining <= 0
            useHdBtn.addEventListener("click", {
                showHitDiceModal(hd.die, hd.storageKey, usedDice, character) {
                    container.innerHTML = ""
                    renderDndPlayingTab(character, container)
                }
            })
            row.appendChild(useHdBtn)

            val recoverBtn = document.createElement("button") as HTMLButtonElement
            recoverBtn.textContent = "Recover"
            recoverBtn.style.fontSize = "11px"
            recoverBtn.disabled = usedDice <= 0
            recoverBtn.addEventListener("click", {
                usedDice -= 1
                localStorage.setItem(hd.storageKey, usedDice.toString())
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            row.appendChild(recoverBtn)

            hitDiceSection.appendChild(row)
        }
    }

    lifeBox.appendChild(hitDiceSection)

    // Top row: Life Tracker + Attacks side by side
    val topRow = document.createElement("div") as HTMLDivElement
    topRow.style.display = "flex"
    topRow.style.setProperty("gap", "16px")
    topRow.style.marginBottom = "16px"

    topRow.appendChild(lifeBox)

    // Attacks box
    val atkBox = document.createElement("div") as HTMLDivElement
    atkBox.style.border = "1px solid #ccc"
    atkBox.style.borderRadius = "8px"
    atkBox.style.padding = "12px"
    atkBox.style.setProperty("flex", "1")

    val atkBoxTitle = document.createElement("h4") as HTMLHeadingElement
    atkBoxTitle.textContent = "Attacks"
    atkBoxTitle.style.margin = "0"

    val atkHeader = document.createElement("div") as HTMLDivElement
    atkHeader.style.display = "flex"
    atkHeader.style.justifyContent = "space-between"
    atkHeader.style.alignItems = "center"
    atkHeader.style.marginBottom = "10px"
    atkHeader.appendChild(atkBoxTitle)

    val changeWeaponBtn = document.createElement("button") as HTMLButtonElement
    changeWeaponBtn.textContent = "Change Weapon"
    changeWeaponBtn.style.fontSize = "11px"
    changeWeaponBtn.addEventListener("click", {
        showChangeWeaponModal(character) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    atkHeader.appendChild(changeWeaponBtn)
    atkBox.appendChild(atkHeader)

    // Gather weapon proficiencies
    val features = dndFeaturesRepo.getByCharacterId(character.id)
    val weaponCatProfs = mutableSetOf<String>()
    val weaponSpecificProfs = mutableSetOf<String>()
    features.filter { it.type == "Weapon/Armor Proficiency" }.forEach { f ->
        f.description.split(",").map { it.trim() }.forEach { entry ->
            when {
                entry.startsWith("weapon_cat:") -> weaponCatProfs.add(entry.removePrefix("weapon_cat:"))
                entry.startsWith("weapon:") -> weaponSpecificProfs.add(entry.removePrefix("weapon:"))
            }
        }
    }

    val weapons = dndWeaponRepo.getByCharacterId(character.id)
    val equippedWeapon = weapons.firstOrNull { it.isEquipped }

    if (equippedWeapon != null) {
        val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
        val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
        val profBonus = calcProficiency(totalLevel)
        val strMod = stats.strValue?.let { calcModifier(it) } ?: 0
        val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0

        val hasProfEq = equippedWeapon.category in weaponCatProfs
                || equippedWeapon.weaponType in weaponSpecificProfs
        val isRanged = equippedWeapon.range && !equippedWeapon.thrown

        val atkTable = document.createElement("table") as HTMLTableElement
        atkTable.style.width = "100%"
        atkTable.style.fontSize = "12px"
        atkTable.style.borderCollapse = "collapse"

        val thead = document.createElement("thead")
        val headerRow = document.createElement("tr") as HTMLTableRowElement
        listOf("Name", "Range", "Test", "Damage", "Notes").forEach { h ->
            val th = document.createElement("th") as HTMLTableCellElement
            th.textContent = h
            th.style.textAlign = "left"
            th.style.padding = "4px"
            th.style.borderBottom = "1px solid #ccc"
            th.style.fontSize = "11px"
            th.style.color = "#666"
            headerRow.appendChild(th)
        }
        thead.appendChild(headerRow)
        atkTable.appendChild(thead)

        val tbody = document.createElement("tbody")

        fun addAtkRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
            val mod = if (useDex) dexMod else strMod
            val atkMod = mod + (if (hasProfEq) profBonus else 0)

            val tr = document.createElement("tr") as HTMLTableRowElement

            val tdName = document.createElement("td") as HTMLTableCellElement
            tdName.style.padding = "3px 4px"; tdName.style.fontWeight = "bold"
            val statLabel = if (equippedWeapon.finesse) " (${if (useDex) "Dex" else "Str"})" else ""
            val handLabel = if (twoHanded) " [2H]" else ""
            val thrownLabel = if (thrown) " [Thrown]" else ""
            tdName.textContent = "${equippedWeapon.name}$statLabel$handLabel$thrownLabel"
            tr.appendChild(tdName)

            val tdRange = document.createElement("td") as HTMLTableCellElement
            tdRange.style.padding = "3px 4px"
            tdRange.textContent = if (isRanged || thrown) "${equippedWeapon.rangeDistance}/${equippedWeapon.rangeLongDistance}m" else "Melee"
            tr.appendChild(tdRange)

            val tdTest = document.createElement("td") as HTMLTableCellElement
            tdTest.style.padding = "3px 4px"
            val testSign = if (atkMod >= 0) "+" else ""
            tdTest.textContent = "$testSign$atkMod"
            tr.appendChild(tdTest)

            val tdDmg = document.createElement("td") as HTMLTableCellElement
            tdDmg.style.padding = "3px 4px"
            val dice = if (twoHanded) equippedWeapon.versatileDice else equippedWeapon.damageDice
            val dmgStr = if (isRanged || thrown) {
                "$dice ${equippedWeapon.damageType}"
            } else {
                val modSign = if (mod >= 0) "+" else ""
                "$dice$modSign$mod ${equippedWeapon.damageType}"
            }
            tdDmg.textContent = dmgStr
            tr.appendChild(tdDmg)

            val tdNotes = document.createElement("td") as HTMLTableCellElement
            tdNotes.style.padding = "3px 4px"; tdNotes.style.color = "#888"
            val icons = mutableListOf<String>()
            if (equippedWeapon.silver) icons.add("\uD83E\uDD48")
            if (equippedWeapon.heavy) icons.add("\u2693")
            if (equippedWeapon.light) icons.add("\uD83E\uDEB6")
            if (equippedWeapon.reach) icons.add("\uD83D\uDCAB")
            if (equippedWeapon.ammunition) icons.add("\uD83C\uDFF9")
            if (equippedWeapon.loading) icons.add("\u23F3")
            val extras = mutableListOf<String>()
            if (icons.isNotEmpty()) extras.add(icons.joinToString(""))
            if (equippedWeapon.additionalFeatures.isNotEmpty()) {
                val maxLen = 50 - (extras.firstOrNull()?.length ?: 0)
                val feat = if (equippedWeapon.additionalFeatures.length > maxLen)
                    equippedWeapon.additionalFeatures.take(maxLen) + "..." else equippedWeapon.additionalFeatures
                extras.add(feat)
            }
            tdNotes.textContent = extras.joinToString(" ")
            tr.appendChild(tdNotes)

            tbody.appendChild(tr)
        }

        if (isRanged) {
            addAtkRow(useDex = true)
        } else if (equippedWeapon.finesse) {
            addAtkRow(useDex = false)
            if (equippedWeapon.versatile) addAtkRow(useDex = false, twoHanded = true)
            addAtkRow(useDex = true)
            if (equippedWeapon.versatile) addAtkRow(useDex = true, twoHanded = true)
            if (equippedWeapon.thrown) {
                addAtkRow(useDex = false, thrown = true)
                addAtkRow(useDex = true, thrown = true)
            }
        } else {
            addAtkRow(useDex = false)
            if (equippedWeapon.versatile) addAtkRow(useDex = false, twoHanded = true)
            if (equippedWeapon.thrown) addAtkRow(useDex = false, thrown = true)
        }

        // Disarmed Attack row
        val disarmedTr = document.createElement("tr") as HTMLTableRowElement
        val disName = document.createElement("td") as HTMLTableCellElement
        disName.style.padding = "3px 4px"; disName.style.fontWeight = "bold"
        disName.textContent = "Disarmed Attack"
        disarmedTr.appendChild(disName)
        val disRange = document.createElement("td") as HTMLTableCellElement
        disRange.style.padding = "3px 4px"; disRange.textContent = "Melee"
        disarmedTr.appendChild(disRange)
        val disTest = document.createElement("td") as HTMLTableCellElement
        disTest.style.padding = "3px 4px"
        val disAtkMod = strMod + profBonus
        val disTestSign = if (disAtkMod >= 0) "+" else ""
        disTest.textContent = "$disTestSign$disAtkMod"
        disarmedTr.appendChild(disTest)
        val disDmg = document.createElement("td") as HTMLTableCellElement
        disDmg.style.padding = "3px 4px"
        val disDmgStr = if (stats.disarmedDice == "Normal") {
            "${maxOf(1, 1 + strMod)} Bludgeoning"
        } else {
            val modSign = if (strMod >= 0) "+" else ""
            "${stats.disarmedDice}$modSign$strMod Bludgeoning"
        }
        disDmg.textContent = disDmgStr
        disarmedTr.appendChild(disDmg)
        val disNotes = document.createElement("td") as HTMLTableCellElement
        disNotes.style.padding = "3px 4px"
        disarmedTr.appendChild(disNotes)
        tbody.appendChild(disarmedTr)

        // Add spell attack rows to the same tbody
        val attackSpells = dndSpellRepo.getByCharacterId(character.id).filter { it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters) }
        attackSpells.forEach { spell ->
            val tr = document.createElement("tr") as HTMLTableRowElement
            val tdName = document.createElement("td") as HTMLTableCellElement
            tdName.style.padding = "3px 4px"; tdName.style.fontWeight = "bold"
            val circleStr = if (spell.circle == "Cantrip") "" else " (${spell.circle})"
            tdName.textContent = "\u2728 ${spell.name}$circleStr"
            tr.appendChild(tdName)
            val tdRange = document.createElement("td") as HTMLTableCellElement
            tdRange.style.padding = "3px 4px"
            tdRange.textContent = if (spell.range.isNotEmpty()) "${spell.range}m" else "\u2014"
            tr.appendChild(tdRange)
            val tdTest = document.createElement("td") as HTMLTableCellElement
            tdTest.style.padding = "3px 4px"
            if (spell.needsSavingThrow) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val tl = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                val dc = abMod + calcProficiency(tl) + 8
                tdTest.textContent = "${spell.savingThrowAbility} Save (DC $dc)"
            } else if (spell.isAttack) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val tl = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                val atkMod = abMod + calcProficiency(tl)
                tdTest.textContent = if (atkMod >= 0) "+$atkMod" else "$atkMod"
            }
            tr.appendChild(tdTest)
            val tdDmg = document.createElement("td") as HTMLTableCellElement
            tdDmg.style.padding = "3px 4px"
            tdDmg.textContent = if (spell.attackDamageDice.isNotEmpty()) "${spell.attackDamageDice} ${spell.attackDamageType}" else "\u2014"
            tr.appendChild(tdDmg)
            val tdNotes = document.createElement("td") as HTMLTableCellElement
            tdNotes.style.padding = "3px 4px"; tdNotes.style.fontSize = "10px"; tdNotes.style.color = "#666"
            val notes = mutableListOf<String>()
            val components = mutableListOf<String>()
            if (spell.hasVerbal) components.add("V")
            if (spell.hasSomatic) components.add("S")
            if (spell.hasMaterial) components.add("M")
            if (components.isNotEmpty()) notes.add(components.joinToString(""))
            if (spell.hasMaterial && spell.materialComponents.isNotEmpty()) notes.add(spell.materialComponents)
            if (spell.needsConcentration) notes.add("Conc.")
            if (spell.canBeRitual) notes.add("Ritual")
            if (spell.higherCircles.isNotEmpty()) notes.add("⬆️ Higher")
            tdNotes.textContent = notes.joinToString(", ")
            tr.appendChild(tdNotes)
            tbody.appendChild(tr)
        }

        atkTable.appendChild(tbody)
        atkBox.appendChild(atkTable)
    } else {
        // No weapon equipped - still show disarmed attack
        val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)
        val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
        val profBonus = calcProficiency(totalLevel)
        val strMod = stats.strValue?.let { calcModifier(it) } ?: 0

        val atkTable = document.createElement("table") as HTMLTableElement
        atkTable.style.width = "100%"
        atkTable.style.fontSize = "12px"
        atkTable.style.borderCollapse = "collapse"

        val thead = document.createElement("thead")
        val headerRow = document.createElement("tr") as HTMLTableRowElement
        listOf("Name", "Range", "Test", "Damage", "Notes").forEach { h ->
            val th = document.createElement("th") as HTMLTableCellElement
            th.textContent = h
            th.style.textAlign = "left"
            th.style.padding = "4px"
            th.style.borderBottom = "1px solid #ccc"
            th.style.fontSize = "11px"
            th.style.color = "#666"
            headerRow.appendChild(th)
        }
        thead.appendChild(headerRow)
        atkTable.appendChild(thead)

        val tbody = document.createElement("tbody")
        val disarmedTr = document.createElement("tr") as HTMLTableRowElement
        val disName = document.createElement("td") as HTMLTableCellElement
        disName.style.padding = "3px 4px"; disName.style.fontWeight = "bold"
        disName.textContent = "Disarmed Attack"
        disarmedTr.appendChild(disName)
        val disRange = document.createElement("td") as HTMLTableCellElement
        disRange.style.padding = "3px 4px"; disRange.textContent = "Melee"
        disarmedTr.appendChild(disRange)
        val disTest = document.createElement("td") as HTMLTableCellElement
        disTest.style.padding = "3px 4px"
        val disAtkMod = strMod + profBonus
        val disTestSign = if (disAtkMod >= 0) "+" else ""
        disTest.textContent = "$disTestSign$disAtkMod"
        disarmedTr.appendChild(disTest)
        val disDmg = document.createElement("td") as HTMLTableCellElement
        disDmg.style.padding = "3px 4px"
        val disDmgStr2 = if (stats.disarmedDice == "Normal") {
            "${maxOf(1, 1 + strMod)} Bludgeoning"
        } else {
            val modSign = if (strMod >= 0) "+" else ""
            "${stats.disarmedDice}$modSign$strMod Bludgeoning"
        }
        disDmg.textContent = disDmgStr2
        disarmedTr.appendChild(disDmg)
        val disNotes = document.createElement("td") as HTMLTableCellElement
        disNotes.style.padding = "3px 4px"
        disarmedTr.appendChild(disNotes)
        tbody.appendChild(disarmedTr)

        // Add spell attack rows to the same tbody
        val attackSpells2 = dndSpellRepo.getByCharacterId(character.id).filter { it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters) }
        attackSpells2.forEach { spell ->
            val tr = document.createElement("tr") as HTMLTableRowElement
            val tdName = document.createElement("td") as HTMLTableCellElement
            tdName.style.padding = "3px 4px"; tdName.style.fontWeight = "bold"
            val circleStr = if (spell.circle == "Cantrip") "" else " (${spell.circle})"
            tdName.textContent = "\u2728 ${spell.name}$circleStr"
            tr.appendChild(tdName)
            val tdRange = document.createElement("td") as HTMLTableCellElement
            tdRange.style.padding = "3px 4px"
            tdRange.textContent = if (spell.range.isNotEmpty()) "${spell.range}m" else "\u2014"
            tr.appendChild(tdRange)
            val tdTest = document.createElement("td") as HTMLTableCellElement
            tdTest.style.padding = "3px 4px"
            if (spell.needsSavingThrow) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val dc = abMod + profBonus + 8
                tdTest.textContent = "${spell.savingThrowAbility} Save (DC $dc)"
            } else if (spell.isAttack) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val atkMod = abMod + profBonus
                tdTest.textContent = if (atkMod >= 0) "+$atkMod" else "$atkMod"
            }
            tr.appendChild(tdTest)
            val tdDmg = document.createElement("td") as HTMLTableCellElement
            tdDmg.style.padding = "3px 4px"
            tdDmg.textContent = if (spell.attackDamageDice.isNotEmpty()) "${spell.attackDamageDice} ${spell.attackDamageType}" else "\u2014"
            tr.appendChild(tdDmg)
            val tdNotes = document.createElement("td") as HTMLTableCellElement
            tdNotes.style.padding = "3px 4px"; tdNotes.style.fontSize = "10px"; tdNotes.style.color = "#666"
            val notes = mutableListOf<String>()
            val components = mutableListOf<String>()
            if (spell.hasVerbal) components.add("V")
            if (spell.hasSomatic) components.add("S")
            if (spell.hasMaterial) components.add("M")
            if (components.isNotEmpty()) notes.add(components.joinToString(""))
            if (spell.hasMaterial && spell.materialComponents.isNotEmpty()) notes.add(spell.materialComponents)
            if (spell.needsConcentration) notes.add("Conc.")
            if (spell.canBeRitual) notes.add("Ritual")
            if (spell.higherCircles.isNotEmpty()) notes.add("⬆️ Higher")
            tdNotes.textContent = notes.joinToString(", ")
            tr.appendChild(tdNotes)
            tbody.appendChild(tr)
        }

        atkTable.appendChild(tbody)
        atkBox.appendChild(atkTable)
    }

    // Ammunition section
    val ammoItems = dndConsumableRepo.getByCharacterId(character.id).filter { it.type == "Ammunition" }
    if (ammoItems.isNotEmpty()) {
        val ammoSection = document.createElement("div") as HTMLDivElement
        ammoSection.style.borderTop = "1px solid #eee"
        ammoSection.style.marginTop = "10px"
        ammoSection.style.paddingTop = "8px"

        val ammoTitle = document.createElement("div") as HTMLDivElement
        ammoTitle.textContent = "Ammunition"
        ammoTitle.style.fontWeight = "bold"
        ammoTitle.style.fontSize = "12px"
        ammoTitle.style.marginBottom = "6px"
        ammoSection.appendChild(ammoTitle)

        ammoItems.forEach { ammo ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.alignItems = "center"
            row.style.setProperty("gap", "8px")
            row.style.marginBottom = "4px"

            val nameSpan = document.createElement("span") as HTMLSpanElement
            nameSpan.textContent = ammo.name
            nameSpan.style.fontSize = "12px"
            nameSpan.style.setProperty("flex", "1")
            row.appendChild(nameSpan)

            val minusBtn = document.createElement("button") as HTMLButtonElement
            minusBtn.textContent = "\u2212"
            minusBtn.style.fontSize = "11px"
            minusBtn.style.width = "24px"
            minusBtn.disabled = ammo.quantity <= 0
            minusBtn.addEventListener("click", {
                dndConsumableRepo.save(ammo.copy(quantity = maxOf(ammo.quantity - 1, 0)))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            row.appendChild(minusBtn)

            val qtySpan = document.createElement("span") as HTMLSpanElement
            qtySpan.textContent = ammo.quantity.toString()
            qtySpan.style.fontSize = "13px"
            qtySpan.style.fontWeight = "bold"
            qtySpan.style.minWidth = "20px"
            qtySpan.style.textAlign = "center"
            row.appendChild(qtySpan)

            val plusBtn = document.createElement("button") as HTMLButtonElement
            plusBtn.textContent = "+"
            plusBtn.style.fontSize = "11px"
            plusBtn.style.width = "24px"
            plusBtn.addEventListener("click", {
                dndConsumableRepo.save(ammo.copy(quantity = ammo.quantity + 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            row.appendChild(plusBtn)

            ammoSection.appendChild(row)
        }
        atkBox.appendChild(ammoSection)
    }

    topRow.appendChild(atkBox)

    // Special Actions box (right of Attacks)
    val specialBox = document.createElement("div") as HTMLDivElement
    specialBox.style.border = "1px solid #ccc"
    specialBox.style.borderRadius = "8px"
    specialBox.style.padding = "12px"
    specialBox.style.setProperty("flex", "0.4")

    val specialTitle = document.createElement("h4") as HTMLHeadingElement
    specialTitle.textContent = "Special Actions"
    specialTitle.style.margin = "0 0 10px 0"
    specialBox.appendChild(specialTitle)

    val rechargeables = features.filter { it.type == "Rechargable Feature" }
    if (rechargeables.isEmpty()) {
        val placeholder = document.createElement("p") as HTMLParagraphElement
        placeholder.textContent = "No rechargeable features."
        placeholder.style.color = "#999"
        placeholder.style.fontSize = "13px"
        specialBox.appendChild(placeholder)
    } else {
        rechargeables.forEach { feat ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"

            val infoDiv = document.createElement("div") as HTMLDivElement
            val nameSpan = document.createElement("span") as HTMLSpanElement
            nameSpan.textContent = feat.name
            nameSpan.style.fontWeight = "bold"
            nameSpan.style.fontSize = "13px"
            infoDiv.appendChild(nameSpan)

            val usageSpan = document.createElement("span") as HTMLSpanElement
            val remaining = (feat.maxQuantity ?: 0) - feat.currentUsages
            usageSpan.textContent = " ($remaining / ${feat.maxQuantity ?: 0})"
            usageSpan.style.fontSize = "12px"
            usageSpan.style.color = if (remaining <= 0) "#c00" else "#666"
            infoDiv.appendChild(usageSpan)

            row.appendChild(infoDiv)

            val btnsDiv = document.createElement("div") as HTMLDivElement
            btnsDiv.style.display = "flex"
            btnsDiv.style.setProperty("gap", "4px")

            val useBtn = document.createElement("button") as HTMLButtonElement
            useBtn.textContent = "\u25BC"
            useBtn.style.fontSize = "11px"
            useBtn.disabled = remaining <= 0
            useBtn.addEventListener("click", {
                val updated = feat.copy(currentUsages = feat.currentUsages + 1)
                dndFeaturesRepo.save(updated)
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(useBtn)

            val rechargeBtn = document.createElement("button") as HTMLButtonElement
            rechargeBtn.textContent = "\u21BA"
            rechargeBtn.style.fontSize = "11px"
            rechargeBtn.disabled = feat.currentUsages <= 0
            rechargeBtn.addEventListener("click", {
                val updated = feat.copy(currentUsages = 0)
                dndFeaturesRepo.save(updated)
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(rechargeBtn)

            row.appendChild(btnsDiv)

            specialBox.appendChild(row)
        }
    }

    // Healing & Magic Potions section
    val allPotions = dndConsumableRepo.getByCharacterId(character.id).filter { it.type == "Healing Potion" || it.type == "Magic Potion" }
    if (allPotions.isNotEmpty()) {
        allPotions.forEach { potion ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "4px 0"
            row.style.borderBottom = "1px solid #f5f5f5"

            val infoDiv = document.createElement("div") as HTMLDivElement
            val nameSpan = document.createElement("span") as HTMLSpanElement
            val displayText = if (potion.type == "Healing Potion" && potion.effect.isNotEmpty()) {
                "Drink ${potion.name} (${potion.effect})"
            } else {
                "Drink ${potion.name}"
            }
            nameSpan.textContent = displayText
            nameSpan.style.fontSize = "12px"
            infoDiv.appendChild(nameSpan)
            val qtySpan = document.createElement("span") as HTMLSpanElement
            qtySpan.textContent = " x${potion.quantity}"
            qtySpan.style.fontSize = "11px"
            qtySpan.style.color = "#666"
            infoDiv.appendChild(qtySpan)
            row.appendChild(infoDiv)

            val btnsDiv = document.createElement("div") as HTMLDivElement
            btnsDiv.style.display = "flex"
            btnsDiv.style.setProperty("gap", "4px")

            val drinkBtn = document.createElement("button") as HTMLButtonElement
            drinkBtn.textContent = "\uD83E\uDDEA"
            drinkBtn.title = "Drink 1"
            drinkBtn.style.fontSize = "11px"
            drinkBtn.disabled = potion.quantity <= 0
            drinkBtn.addEventListener("click", {
                dndConsumableRepo.save(potion.copy(quantity = potion.quantity - 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(drinkBtn)

            val addBtn = document.createElement("button") as HTMLButtonElement
            addBtn.textContent = "\u2795"
            addBtn.title = "Add 1 to stock"
            addBtn.style.fontSize = "11px"
            addBtn.addEventListener("click", {
                dndConsumableRepo.save(potion.copy(quantity = potion.quantity + 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(addBtn)

            row.appendChild(btnsDiv)
            specialBox.appendChild(row)
        }
    }

    // Other consumable items
    val otherItems = dndConsumableRepo.getByCharacterId(character.id).filter { it.type == "Other" }
    if (otherItems.isNotEmpty()) {
        otherItems.forEach { item ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"

            val infoDiv = document.createElement("div") as HTMLDivElement
            val nameSpan = document.createElement("span") as HTMLSpanElement
            nameSpan.textContent = "Use ${item.name}"
            nameSpan.style.fontSize = "12px"
            infoDiv.appendChild(nameSpan)
            val qtySpan = document.createElement("span") as HTMLSpanElement
            qtySpan.textContent = " x${item.quantity}"
            qtySpan.style.fontSize = "11px"
            qtySpan.style.color = "#666"
            infoDiv.appendChild(qtySpan)
            row.appendChild(infoDiv)

            val btnsDiv = document.createElement("div") as HTMLDivElement
            btnsDiv.style.display = "flex"
            btnsDiv.style.setProperty("gap", "4px")

            val useBtn = document.createElement("button") as HTMLButtonElement
            useBtn.textContent = "\u25BC"
            useBtn.title = "Use 1"
            useBtn.style.fontSize = "11px"
            useBtn.disabled = item.quantity <= 0
            useBtn.addEventListener("click", {
                dndConsumableRepo.save(item.copy(quantity = item.quantity - 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(useBtn)

            val addBtn = document.createElement("button") as HTMLButtonElement
            addBtn.textContent = "\u2795"
            addBtn.title = "Add 1 to stock"
            addBtn.style.fontSize = "11px"
            addBtn.addEventListener("click", {
                dndConsumableRepo.save(item.copy(quantity = item.quantity + 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(addBtn)

            row.appendChild(btnsDiv)
            specialBox.appendChild(row)
        }
    }

    // Non-attack spells (prepared or always available)
    val nonAttackSpells = dndSpellRepo.getByCharacterId(character.id).filter {
        !it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters)
    }
    nonAttackSpells.forEach { spell ->
        val row = document.createElement("div") as HTMLDivElement
        row.style.padding = "4px 0"
        row.style.borderBottom = "1px solid #f5f5f5"
        row.style.fontSize = "12px"

        val text = if (spell.circle == "Cantrip") {
            "\u2728 Cast ${spell.name}"
        } else {
            val higherStr = if (spell.higherCircles.isNotEmpty()) " \uD83C\uDD99" else ""
            val ritualStr = if (spell.canBeRitual) "/Ritual" else ""
            "\u2728 Cast ${spell.name} (${spell.circle}$higherStr$ritualStr)"
        }
        row.textContent = text
        specialBox.appendChild(row)
    }

    topRow.appendChild(specialBox)
    leftPanel.appendChild(topRow)

    // === DEFENSE AND STATUS SECTION ===
    val defenseBox = document.createElement("div") as HTMLDivElement
    defenseBox.style.border = "1px solid #ccc"
    defenseBox.style.borderRadius = "8px"
    defenseBox.style.padding = "12px"
    defenseBox.style.setProperty("flex", "1")

    // Header
    val defHeader = document.createElement("div") as HTMLDivElement
    defHeader.style.display = "flex"
    defHeader.style.justifyContent = "space-between"
    defHeader.style.alignItems = "center"
    defHeader.style.marginBottom = "10px"
    val defTitle = document.createElement("h4") as HTMLHeadingElement
    defTitle.textContent = "Defense and Status"; defTitle.style.margin = "0"
    defHeader.appendChild(defTitle)
    val changeArmorBtn = document.createElement("button") as HTMLButtonElement
    changeArmorBtn.textContent = "Change Armor"
    changeArmorBtn.style.fontSize = "11px"
    changeArmorBtn.addEventListener("click", {
        showChangeArmorModal(character) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    defHeader.appendChild(changeArmorBtn)
    defenseBox.appendChild(defHeader)

    // Numbers row: AC + Initiative
    val armors = dndArmorRepo.getByCharacterId(character.id)
    val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
    val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
    val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0
    val noArmorAC = stats.emptyArmorClass ?: 10
    val shieldAC = equippedShield?.baseAC ?: 0

    val totalAC = if (equippedArmor == null) {
        noArmorAC + shieldAC
    } else {
        val modBonus = when (equippedArmor.acModifier) {
            "Dex" -> dexMod
            "Dex (Max: 2)" -> minOf(dexMod, 2)
            else -> 0
        }
        equippedArmor.baseAC + modBonus + shieldAC
    }

    val numbersRow = document.createElement("div") as HTMLDivElement
    numbersRow.style.display = "flex"
    numbersRow.style.setProperty("gap", "24px")
    numbersRow.style.marginBottom = "12px"

    // AC
    val acCol = document.createElement("div") as HTMLDivElement
    acCol.style.textAlign = "center"
    val acNum = document.createElement("div") as HTMLDivElement
    acNum.textContent = totalAC.toString()
    acNum.style.fontSize = "32px"; acNum.style.fontWeight = "bold"
    acCol.appendChild(acNum)
    val acIcons = document.createElement("div") as HTMLDivElement
    acIcons.style.fontSize = "11px"; acIcons.style.color = "#666"
    val armorIcon = if (equippedArmor != null) "\uD83E\uDE96" else "\uD83D\uDC55"
    val shieldIcon = if (equippedShield != null) " \uD83D\uDEE1\uFE0F" else ""
    acIcons.textContent = "AC $armorIcon$shieldIcon"
    acCol.appendChild(acIcons)
    numbersRow.appendChild(acCol)

    // Initiative
    val initCol = document.createElement("div") as HTMLDivElement
    initCol.style.textAlign = "center"
    val initNum = document.createElement("div") as HTMLDivElement
    val initVal = stats.dexValue?.let { calcModifier(it) } ?: 0
    initNum.textContent = if (initVal >= 0) "+$initVal" else "$initVal"
    initNum.style.fontSize = "32px"; initNum.style.fontWeight = "bold"
    initCol.appendChild(initNum)
    val initLabel = document.createElement("div") as HTMLDivElement
    initLabel.textContent = "Initiative"
    initLabel.style.fontSize = "11px"; initLabel.style.color = "#666"
    initCol.appendChild(initLabel)
    numbersRow.appendChild(initCol)

    defenseBox.appendChild(numbersRow)

    // Status section
    val statusHeader = document.createElement("div") as HTMLDivElement
    statusHeader.style.display = "flex"
    statusHeader.style.justifyContent = "space-between"
    statusHeader.style.alignItems = "center"
    statusHeader.style.marginBottom = "6px"
    statusHeader.style.borderTop = "1px solid #eee"
    statusHeader.style.paddingTop = "8px"
    val statusTitle = document.createElement("span") as HTMLSpanElement
    statusTitle.textContent = "Status"; statusTitle.style.fontWeight = "bold"; statusTitle.style.fontSize = "13px"
    statusHeader.appendChild(statusTitle)
    val addStatusBtn = document.createElement("button") as HTMLButtonElement
    addStatusBtn.textContent = "+ Add"
    addStatusBtn.style.fontSize = "11px"
    addStatusBtn.addEventListener("click", {
        showAddStatusModal(character) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    statusHeader.appendChild(addStatusBtn)
    defenseBox.appendChild(statusHeader)

    val statusKey = "dnd_playing_status_${character.id}"
    val statuses = localStorage.getItem(statusKey)?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    if (statuses.isEmpty()) {
        val noStatus = document.createElement("span") as HTMLSpanElement
        noStatus.textContent = "No active status"
        noStatus.style.color = "#999"; noStatus.style.fontSize = "12px"
        defenseBox.appendChild(noStatus)
    } else {
        val statusList = document.createElement("div") as HTMLDivElement
        statusList.style.display = "flex"
        statusList.style.setProperty("flex-wrap", "wrap")
        statusList.style.setProperty("gap", "4px")
        statuses.forEach { status ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$status \u00D7"
            badge.style.backgroundColor = "#ffe0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "12px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", {
                val updated = statuses.filter { it != status }
                localStorage.setItem(statusKey, updated.joinToString(","))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            statusList.appendChild(badge)
        }
        defenseBox.appendChild(statusList)
    }

    // Bottom row: Defense + Magics side by side
    val bottomRow = document.createElement("div") as HTMLDivElement
    bottomRow.style.display = "flex"
    bottomRow.style.setProperty("gap", "16px")
    bottomRow.style.marginBottom = "16px"

    bottomRow.appendChild(defenseBox)

    // Magics box
    val magicsBox = document.createElement("div") as HTMLDivElement
    magicsBox.style.border = "1px solid #ccc"
    magicsBox.style.borderRadius = "8px"
    magicsBox.style.padding = "12px"
    magicsBox.style.setProperty("flex", "1")

    val magicsTitle = document.createElement("h4") as HTMLHeadingElement
    magicsTitle.textContent = "Magics"
    magicsTitle.style.margin = "0 0 10px 0"
    magicsBox.appendChild(magicsTitle)

    // Get spellcasting info
    val magicMainClass = mainInfo?.mainClass
    val magicMainSub = mainInfo?.mainSubClass
    val magicAbility = DungeonsAndDragons.spellcastingAbilityFor(magicMainClass, magicMainSub)
        ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_$magicMainClass")?.takeIf { it != "__none__" }

    if (magicAbility == null) {
        val noMagic = document.createElement("p") as HTMLParagraphElement
        noMagic.textContent = "No Magic"
        noMagic.style.color = "#999"; noMagic.style.fontSize = "13px"
        magicsBox.appendChild(noMagic)
    } else {
        val abilityMod = when (magicAbility) {
            "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
            "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
            "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
            else -> 0
        }
        val spellMod = abilityMod + calcProficiency((mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0))
        val spellDC = spellMod + 8
        val abilityName = when (magicAbility) { "Cha" -> "Cha"; "Int" -> "Int"; "Wis" -> "Wis"; else -> magicAbility }

        // Stats row
        val magicStats = document.createElement("div") as HTMLDivElement
        magicStats.style.display = "flex"
        magicStats.style.setProperty("gap", "16px")
        magicStats.style.marginBottom = "8px"
        magicStats.style.fontSize = "12px"

        val abSpan = document.createElement("span") as HTMLSpanElement
        abSpan.textContent = "\u2728 $abilityName"; abSpan.style.fontWeight = "bold"
        magicStats.appendChild(abSpan)
        val modSpan = document.createElement("span") as HTMLSpanElement
        val modStr = if (spellMod >= 0) "+$spellMod" else "$spellMod"
        modSpan.textContent = "Mod: $modStr"
        magicStats.appendChild(modSpan)
        val dcSpan = document.createElement("span") as HTMLSpanElement
        dcSpan.textContent = "DC: $spellDC"
        magicStats.appendChild(dcSpan)
        magicsBox.appendChild(magicStats)

        // Spell slots
        val mainLevel = mainInfo?.mainClassLevel ?: 1
        val slots = DungeonsAndDragons.spellSlotsFor(magicMainClass, magicMainSub, mainLevel)

        if (slots.isNotEmpty()) {
            val slotsDiv = document.createElement("div") as HTMLDivElement
            slotsDiv.style.marginBottom = "8px"

            slots.forEachIndexed { idx, baseSlots ->
                val circleNum = idx + 1
                val usedKey = "dnd_spell_slots_${character.id}_${magicMainClass}_$circleNum"
                val tempKey = "dnd_spell_slots_temp_${character.id}_${magicMainClass}_$circleNum"
                val usedSlots = localStorage.getItem(usedKey)?.toIntOrNull() ?: 0
                val tempSlots = localStorage.getItem(tempKey)?.toIntOrNull() ?: 0
                val availableSlots = baseSlots + tempSlots

                val slotRow = document.createElement("div") as HTMLDivElement
                slotRow.style.display = "flex"
                slotRow.style.alignItems = "center"
                slotRow.style.setProperty("gap", "4px")
                slotRow.style.marginBottom = "3px"
                slotRow.style.fontSize = "11px"

                val label = document.createElement("span") as HTMLSpanElement
                label.textContent = "${circleNum}\u00BA"
                label.style.fontWeight = "bold"
                label.style.width = "22px"
                slotRow.appendChild(label)

                val badge = document.createElement("span") as HTMLSpanElement
                val tempStr = if (tempSlots > 0) " (+$tempSlots)" else ""
                badge.textContent = "$usedSlots / $availableSlots$tempStr"
                badge.style.padding = "1px 4px"
                badge.style.borderRadius = "3px"
                badge.style.backgroundColor = if (usedSlots >= availableSlots) "#ffe0e0" else "#e0f0e0"
                badge.style.minWidth = "50px"
                badge.style.textAlign = "center"
                slotRow.appendChild(badge)

                // Increase Used
                val useBtn = document.createElement("button") as HTMLButtonElement
                useBtn.textContent = "\u25B2"; useBtn.title = "Use Slot"; useBtn.style.fontSize = "9px"
                useBtn.disabled = usedSlots >= availableSlots
                useBtn.addEventListener("click", {
                    localStorage.setItem(usedKey, (usedSlots + 1).toString())
                    container.innerHTML = ""; renderDndPlayingTab(character, container)
                })
                slotRow.appendChild(useBtn)

                // Decrease Used (or remove temp if used is 0)
                val restoreBtn = document.createElement("button") as HTMLButtonElement
                restoreBtn.textContent = "\u25BC"; restoreBtn.title = "Restore Slot"; restoreBtn.style.fontSize = "9px"
                restoreBtn.disabled = usedSlots <= 0 && tempSlots <= 0
                restoreBtn.addEventListener("click", {
                    if (usedSlots > 0) {
                        localStorage.setItem(usedKey, (usedSlots - 1).toString())
                    } else if (tempSlots > 0) {
                        localStorage.setItem(tempKey, (tempSlots - 1).toString())
                    }
                    container.innerHTML = ""; renderDndPlayingTab(character, container)
                })
                slotRow.appendChild(restoreBtn)

                // Add Temporary Slot
                val tempBtn = document.createElement("button") as HTMLButtonElement
                tempBtn.textContent = "\u2795"; tempBtn.title = "Add Temp Slot"; tempBtn.style.fontSize = "9px"
                tempBtn.addEventListener("click", {
                    localStorage.setItem(tempKey, (tempSlots + 1).toString())
                    container.innerHTML = ""; renderDndPlayingTab(character, container)
                })
                slotRow.appendChild(tempBtn)

                slotsDiv.appendChild(slotRow)
            }
            magicsBox.appendChild(slotsDiv)
        }

        // Prepared Spells button
        val magicBtns = document.createElement("div") as HTMLDivElement
        magicBtns.style.display = "flex"
        magicBtns.style.setProperty("gap", "4px")

        // Change Prepared Spells
        val prepBtn = document.createElement("button") as HTMLButtonElement
        prepBtn.textContent = "\uD83D\uDCCB Prepared"
        prepBtn.title = "Change Prepared Spells"
        prepBtn.style.fontSize = "11px"
        val classNeedsPrepared = (magicMainClass ?: "") in DungeonsAndDragons.preparedCasters
        prepBtn.disabled = !classNeedsPrepared
        prepBtn.addEventListener("click", {
            showPreparedSpellsModal(character) {
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            }
        })
        magicBtns.appendChild(prepBtn)

        // Reset all slots
        val resetSlotsBtn = document.createElement("button") as HTMLButtonElement
        resetSlotsBtn.textContent = "\u21BA"
        resetSlotsBtn.title = "Reset all spell slots"
        resetSlotsBtn.style.fontSize = "11px"
        resetSlotsBtn.addEventListener("click", {
            slots.forEachIndexed { idx, _ ->
                val circleNum = idx + 1
                localStorage.removeItem("dnd_spell_slots_${character.id}_${magicMainClass}_$circleNum")
                localStorage.removeItem("dnd_spell_slots_temp_${character.id}_${magicMainClass}_$circleNum")
            }
            container.innerHTML = ""; renderDndPlayingTab(character, container)
        })
        magicBtns.appendChild(resetSlotsBtn)

        magicsBox.appendChild(magicBtns)
    }

    bottomRow.appendChild(magicsBox)

    // Loot and Notes box
    val lootBox = document.createElement("div") as HTMLDivElement
    lootBox.style.border = "1px solid #ccc"
    lootBox.style.borderRadius = "8px"
    lootBox.style.padding = "12px"
    lootBox.style.setProperty("flex", "1")

    val lootTitle = document.createElement("h4") as HTMLHeadingElement
    lootTitle.textContent = "Loot and Notes"
    lootTitle.style.margin = "0 0 10px 0"
    lootBox.appendChild(lootTitle)

    // Money display
    val money = dndMoneyRepo.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)
    val moneyGrid = document.createElement("div") as HTMLDivElement
    moneyGrid.style.setProperty("display", "grid")
    moneyGrid.style.setProperty("grid-template-columns", "repeat(5, 1fr)")
    moneyGrid.style.setProperty("gap", "8px")
    moneyGrid.style.textAlign = "center"
    moneyGrid.style.marginBottom = "12px"

    data class CoinDisplay(val label: String, val abbr: String, val value: Int)
    val coins = listOf(
        CoinDisplay("Copper", "pc", money.copper),
        CoinDisplay("Silver", "ps", money.silver),
        CoinDisplay("Electrum", "pe", money.electrum),
        CoinDisplay("Gold", "pg", money.gold),
        CoinDisplay("Platinum", "pp", money.platinum)
    )

    coins.forEach { coin ->
        val col = document.createElement("div") as HTMLDivElement
        val numSpan = document.createElement("div") as HTMLDivElement
        numSpan.textContent = coin.value.toString()
        numSpan.style.fontSize = "18px"
        numSpan.style.fontWeight = "bold"
        col.appendChild(numSpan)
        val abbrSpan = document.createElement("div") as HTMLDivElement
        abbrSpan.textContent = coin.abbr
        abbrSpan.style.fontSize = "10px"
        abbrSpan.style.color = "#666"
        col.appendChild(abbrSpan)
        moneyGrid.appendChild(col)
    }
    lootBox.appendChild(moneyGrid)

    // Add / Subtract money buttons
    val moneyBtns = document.createElement("div") as HTMLDivElement
    moneyBtns.style.display = "flex"
    moneyBtns.style.setProperty("gap", "8px")
    moneyBtns.style.justifyContent = "center"

    val addMoneyBtn = document.createElement("button") as HTMLButtonElement
    addMoneyBtn.textContent = "\uD83D\uDCB0 Add"
    addMoneyBtn.style.fontSize = "11px"
    addMoneyBtn.addEventListener("click", {
        showMoneyModal(character, "Add Money", true) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    moneyBtns.appendChild(addMoneyBtn)

    val subMoneyBtn = document.createElement("button") as HTMLButtonElement
    subMoneyBtn.textContent = "\uD83D\uDCB8 Spend"
    subMoneyBtn.style.fontSize = "11px"
    subMoneyBtn.addEventListener("click", {
        showMoneyModal(character, "Spend Money", false) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    moneyBtns.appendChild(subMoneyBtn)

    lootBox.appendChild(moneyBtns)

    // Loot buttons
    val lootBtnSection = document.createElement("div") as HTMLDivElement
    lootBtnSection.style.borderTop = "1px solid #eee"
    lootBtnSection.style.marginTop = "10px"
    lootBtnSection.style.paddingTop = "8px"

    val lootBtnTitle = document.createElement("div") as HTMLDivElement
    lootBtnTitle.textContent = "Add Loot"
    lootBtnTitle.style.fontWeight = "bold"
    lootBtnTitle.style.fontSize = "12px"
    lootBtnTitle.style.marginBottom = "6px"
    lootBtnSection.appendChild(lootBtnTitle)

    val lootBtnRow = document.createElement("div") as HTMLDivElement
    lootBtnRow.style.display = "flex"
    lootBtnRow.style.setProperty("flex-wrap", "wrap")
    lootBtnRow.style.setProperty("gap", "4px")

    fun addLootBtn(label: String, onClick: () -> Unit) {
        val btn = document.createElement("button") as HTMLButtonElement
        btn.textContent = label
        btn.style.fontSize = "10px"
        btn.addEventListener("click", { onClick() })
        lootBtnRow.appendChild(btn)
    }

    addLootBtn("\uD83E\uDE96 Armor") {
        showArmorModal(character, null) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\u2694\uFE0F Weapon") {
        showWeaponModal(character, null) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\u2728 Magic Item") {
        showMagicItemModal(character, null) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\uD83E\uDDEA Consumable") {
        showAddConsumableLootModal(character) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\uD83D\uDCE6 Key Item") {
        showInventoryItemModal(character, "Key Items, Loot and others", null) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    lootBtnSection.appendChild(lootBtnRow)

    // Add Note button
    val addNoteBtn = document.createElement("button") as HTMLButtonElement
    addNoteBtn.textContent = "\uD83D\uDCDD Add Note"
    addNoteBtn.style.fontSize = "10px"
    addNoteBtn.style.marginTop = "8px"
    addNoteBtn.addEventListener("click", {
        showNoteModal(character, null) {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    lootBtnSection.appendChild(addNoteBtn)

    lootBox.appendChild(lootBtnSection)
    bottomRow.appendChild(lootBox)
    leftPanel.appendChild(bottomRow)

    // === RIGHT PANEL (Search) ===
    val searchInput = document.createElement("input") as HTMLInputElement
    searchInput.type = "text"
    searchInput.placeholder = "Search..."
    searchInput.style.width = "100%"
    searchInput.style.padding = "8px"
    searchInput.style.boxSizing = "border-box"
    rightPanel.appendChild(searchInput)

    layout.appendChild(leftPanel)
    layout.appendChild(rightPanel)
    container.appendChild(layout)
}

private fun showAddConsumableLootModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "400px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = "Add Consumable Loot"
    modal.appendChild(titleEl)

    val desc = document.createElement("p") as HTMLParagraphElement
    desc.textContent = "Select an existing item to increase quantity, or add a new one."
    desc.style.fontSize = "13px"; desc.style.color = "#666"
    modal.appendChild(desc)

    val existingItems = dndConsumableRepo.getByCharacterId(character.id)

    if (existingItems.isNotEmpty()) {
        existingItems.forEach { item ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"

            val info = document.createElement("span") as HTMLSpanElement
            info.textContent = "${item.name} (x${item.quantity})"
            info.style.fontSize = "13px"
            row.appendChild(info)

            val addOneBtn = document.createElement("button") as HTMLButtonElement
            addOneBtn.textContent = "+1"
            addOneBtn.style.fontSize = "11px"
            addOneBtn.addEventListener("click", {
                dndConsumableRepo.save(item.copy(quantity = item.quantity + 1))
                document.body?.removeChild(overlay)
                onDone()
            })
            row.appendChild(addOneBtn)
            modal.appendChild(row)
        }
    }

    // New item button
    val newBtn = document.createElement("button") as HTMLButtonElement
    newBtn.textContent = "\u2795 New Consumable"
    newBtn.style.marginTop = "12px"
    newBtn.style.width = "100%"
    newBtn.addEventListener("click", {
        document.body?.removeChild(overlay)
        showConsumableModal(character, null) { onDone() }
    })
    modal.appendChild(newBtn)

    // Cancel
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.style.marginTop = "8px"
    cancelBtn.style.width = "100%"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)

    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showUseSlotModal(character: Character, className: String, slots: List<Int>, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"; overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"; overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")
    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"; modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "300px"; modal.style.width = "90%"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Use Spell Slot"; modal.appendChild(title)
    slots.forEachIndexed { idx, total ->
        val circleNum = idx + 1
        val slotKey = "dnd_spell_slots_${character.id}_${className}_$circleNum"
        val used = localStorage.getItem(slotKey)?.toIntOrNull() ?: 0
        if (used < total) {
            val btn = document.createElement("button") as HTMLButtonElement
            btn.textContent = "${circleNum}\u00BA Circle (${total - used} left)"
            btn.style.display = "block"; btn.style.width = "100%"; btn.style.marginBottom = "6px"
            btn.addEventListener("click", {
                localStorage.setItem(slotKey, (used + 1).toString())
                document.body?.removeChild(overlay); onDone()
            })
            modal.appendChild(btn)
        }
    }
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"; cancelBtn.style.width = "100%"; cancelBtn.style.marginTop = "8px"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

private fun showRestoreSlotModal(character: Character, className: String, slots: List<Int>, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"; overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"; overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")
    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"; modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "300px"; modal.style.width = "90%"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Restore Spell Slot"; modal.appendChild(title)
    slots.forEachIndexed { idx, _ ->
        val circleNum = idx + 1
        val slotKey = "dnd_spell_slots_${character.id}_${className}_$circleNum"
        val used = localStorage.getItem(slotKey)?.toIntOrNull() ?: 0
        if (used > 0) {
            val btn = document.createElement("button") as HTMLButtonElement
            btn.textContent = "${circleNum}\u00BA Circle ($used used)"
            btn.style.display = "block"; btn.style.width = "100%"; btn.style.marginBottom = "6px"
            btn.addEventListener("click", {
                localStorage.setItem(slotKey, (used - 1).toString())
                document.body?.removeChild(overlay); onDone()
            })
            modal.appendChild(btn)
        }
    }
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"; cancelBtn.style.width = "100%"; cancelBtn.style.marginTop = "8px"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

private fun showTempSlotModal(character: Character, className: String, slots: List<Int>, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"; overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"; overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")
    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"; modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "300px"; modal.style.width = "90%"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Add Temporary Slot"; modal.appendChild(title)
    slots.forEachIndexed { idx, total ->
        val circleNum = idx + 1
        val slotKey = "dnd_spell_slots_${character.id}_${className}_$circleNum"
        val btn = document.createElement("button") as HTMLButtonElement
        btn.textContent = "${circleNum}\u00BA Circle"
        btn.style.display = "block"; btn.style.width = "100%"; btn.style.marginBottom = "6px"
        btn.addEventListener("click", {
            // Decrease used by 1 (effectively adding a temp slot)
            val used = localStorage.getItem(slotKey)?.toIntOrNull() ?: 0
            localStorage.setItem(slotKey, (used - 1).toString())
            document.body?.removeChild(overlay); onDone()
        })
        modal.appendChild(btn)
    }
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"; cancelBtn.style.width = "100%"; cancelBtn.style.marginTop = "8px"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

private fun showPreparedSpellsModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"; overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"; overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")
    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"; modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "400px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Change Prepared Spells"; modal.appendChild(title)

    val allSpells = dndSpellRepo.getByCharacterId(character.id).filter { it.circle != "Cantrip" }
    if (allSpells.isEmpty()) {
        val empty = document.createElement("p") as HTMLParagraphElement
        empty.textContent = "No spells to prepare."; empty.style.color = "#999"
        modal.appendChild(empty)
    } else {
        allSpells.sortedBy { it.circle }.forEach { spell ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"; row.style.alignItems = "center"; row.style.setProperty("gap", "8px")
            row.style.padding = "4px 0"; row.style.borderBottom = "1px solid #eee"
            val cb = document.createElement("input") as HTMLInputElement
            cb.type = "checkbox"; cb.checked = spell.isPrepared
            cb.addEventListener("change", {
                dndSpellRepo.save(spell.copy(isPrepared = cb.checked))
            })
            row.appendChild(cb)
            val info = document.createElement("span") as HTMLSpanElement
            info.textContent = "${spell.name} (${spell.circle})"
            info.style.fontSize = "13px"
            row.appendChild(info)
            modal.appendChild(row)
        }
    }

    val closeBtn = document.createElement("button") as HTMLButtonElement
    closeBtn.textContent = "Done"; closeBtn.style.width = "100%"; closeBtn.style.marginTop = "12px"
    closeBtn.addEventListener("click", { document.body?.removeChild(overlay); onDone() })
    modal.appendChild(closeBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

private fun showMoneyModal(character: Character, title: String, isAdd: Boolean, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "350px"; modal.style.width = "90%"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = title
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "8px")

    data class CoinInput(val label: String, val abbr: String, val input: HTMLInputElement)
    val coinInputs = mutableListOf<CoinInput>()

    listOf("Copper" to "pc", "Silver" to "ps", "Electrum" to "pe", "Gold" to "pg", "Platinum" to "pp").forEach { (label, abbr) ->
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = "$label ($abbr)"; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "13px"
        form.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"; input.min = "0"; input.value = "0"
        input.style.padding = "4px"
        form.appendChild(input)
        coinInputs.add(CoinInput(label, abbr, input))
    }
    modal.appendChild(form)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val confirmBtn = document.createElement("button") as HTMLButtonElement
    confirmBtn.textContent = if (isAdd) "Add" else "Spend"
    confirmBtn.addEventListener("click", {
        val money = dndMoneyRepo.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)
        val cp = coinInputs[0].input.value.toIntOrNull() ?: 0
        val sp = coinInputs[1].input.value.toIntOrNull() ?: 0
        val ep = coinInputs[2].input.value.toIntOrNull() ?: 0
        val gp = coinInputs[3].input.value.toIntOrNull() ?: 0
        val pp = coinInputs[4].input.value.toIntOrNull() ?: 0
        val updated = if (isAdd) {
            money.copy(
                copper = money.copper + cp, silver = money.silver + sp,
                electrum = money.electrum + ep, gold = money.gold + gp, platinum = money.platinum + pp
            )
        } else {
            money.copy(
                copper = maxOf(money.copper - cp, 0), silver = maxOf(money.silver - sp, 0),
                electrum = maxOf(money.electrum - ep, 0), gold = maxOf(money.gold - gp, 0),
                platinum = maxOf(money.platinum - pp, 0)
            )
        }
        dndMoneyRepo.save(updated)
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(confirmBtn)
    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showChangeArmorModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "400px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Change Equipment"
    modal.appendChild(title)

    val armors = dndArmorRepo.getByCharacterId(character.id)
    val armorItems = armors.filter { it.type != "Shield" && it.type != "Clothes" }
    val shields = armors.filter { it.type == "Shield" }
    val clothes = armors.filter { it.type == "Clothes" }

    fun buildSelect(label: String, items: List<DndArmor>): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.display = "block"
        lbl.style.marginBottom = "4px"; lbl.style.marginTop = "10px"
        modal.appendChild(lbl)
        val sel = document.createElement("select") as HTMLSelectElement
        sel.style.width = "100%"; sel.style.padding = "4px"
        val noneOpt = document.createElement("option") as HTMLOptionElement
        noneOpt.value = "-1"; noneOpt.textContent = "-- None --"
        sel.appendChild(noneOpt)
        items.forEachIndexed { idx, item ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = idx.toString()
            opt.textContent = item.name
            if (item.isEquipped) opt.selected = true
            sel.appendChild(opt)
        }
        modal.appendChild(sel)
        return sel
    }

    val armorSelect = buildSelect("Armor", armorItems)
    val shieldSelect = buildSelect("Shield", shields)
    val clothesSelect = buildSelect("Clothes", clothes)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Apply"
    saveBtn.addEventListener("click", {
        // Unequip all, then equip selected
        armors.filter { it.isEquipped }.forEach { dndArmorRepo.save(it.copy(isEquipped = false)) }
        val armorIdx = armorSelect.value.toIntOrNull() ?: -1
        if (armorIdx >= 0) dndArmorRepo.save(armorItems[armorIdx].copy(isEquipped = true))
        val shieldIdx = shieldSelect.value.toIntOrNull() ?: -1
        if (shieldIdx >= 0) dndArmorRepo.save(shields[shieldIdx].copy(isEquipped = true))
        val clothesIdx = clothesSelect.value.toIntOrNull() ?: -1
        if (clothesIdx >= 0) dndArmorRepo.save(clothes[clothesIdx].copy(isEquipped = true))
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(saveBtn)
    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showAddStatusModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "350px"; modal.style.width = "90%"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Add Status"
    modal.appendChild(title)

    val defaultStatuses = listOf("Poisoned", "Confused", "Flying", "Frightened", "Blinded", "Charmed", "Deafened", "Grappled", "Incapacitated", "Invisible", "Paralyzed", "Petrified", "Prone", "Restrained", "Stunned", "Unconscious", "Exhaustion")

    val sel = document.createElement("select") as HTMLSelectElement
    sel.style.width = "100%"; sel.style.padding = "6px"; sel.style.marginBottom = "8px"
    val emptyOpt = document.createElement("option") as HTMLOptionElement
    emptyOpt.value = ""; emptyOpt.textContent = "-- Select status --"
    sel.appendChild(emptyOpt)
    defaultStatuses.forEach { s ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = s; opt.textContent = s
        sel.appendChild(opt)
    }
    val customOpt = document.createElement("option") as HTMLOptionElement
    customOpt.value = "__custom__"; customOpt.textContent = "Custom..."
    sel.appendChild(customOpt)
    modal.appendChild(sel)

    val customInput = document.createElement("input") as HTMLInputElement
    customInput.placeholder = "Custom status"
    customInput.style.width = "100%"; customInput.style.padding = "6px"
    customInput.style.display = "none"; customInput.style.marginBottom = "8px"
    modal.appendChild(customInput)

    sel.addEventListener("change", {
        customInput.style.display = if (sel.value == "__custom__") "block" else "none"
    })

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val addBtn = document.createElement("button") as HTMLButtonElement
    addBtn.textContent = "Add"
    addBtn.addEventListener("click", {
        val status = if (sel.value == "__custom__") customInput.value.trim() else sel.value
        if (status.isNotEmpty()) {
            val statusKey = "dnd_playing_status_${character.id}"
            val current = localStorage.getItem(statusKey)?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            if (status !in current) current.add(status)
            localStorage.setItem(statusKey, current.joinToString(","))
            document.body?.removeChild(overlay)
            onDone()
        }
    })
    btnRow.appendChild(addBtn)
    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun showHitDiceModal(
    die: String,
    storageKey: String,
    currentUsed: Int,
    character: Character,
    onDone: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "300px"; modal.style.width = "90%"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = "Use Hit Die ($die)"
    modal.appendChild(title)

    val desc = document.createElement("p") as HTMLParagraphElement
    desc.textContent = "Roll your $die and enter the value:"
    desc.style.fontSize = "14px"
    modal.appendChild(desc)

    val input = document.createElement("input") as HTMLInputElement
    input.type = "number"; input.min = "1"
    input.placeholder = "Rolled value"
    input.style.width = "100%"; input.style.padding = "8px"
    input.style.marginBottom = "16px"
    modal.appendChild(input)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val confirmBtn = document.createElement("button") as HTMLButtonElement
    confirmBtn.textContent = "Apply"
    confirmBtn.addEventListener("click", {
        val rolled = input.value.toIntOrNull()
        if (rolled != null && rolled > 0) {
            // Add 1 usage
            localStorage.setItem(storageKey, (currentUsed + 1).toString())
            // Add rolled value to current life
            val lifeKey = "dnd_playing_life_${character.id}"
            val stats = dndBaseStatsRepo.getByCharacterId(character.id)
            val maxLife = stats?.maxLife ?: 0
            val currentLife = localStorage.getItem(lifeKey)?.toIntOrNull() ?: maxLife
            val newLife = minOf(currentLife + rolled, maxLife)
            localStorage.setItem(lifeKey, newLife.toString())
            document.body?.removeChild(overlay)
            onDone()
        }
    })
    btnRow.appendChild(confirmBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun renderDndFeaturesTab(character: Character, container: HTMLDivElement) {
    val mainInfo = dndMainInfoRepo.getByCharacterId(character.id)

    fun buildFeatureCard(f: DndFeature, onRefresh: () -> Unit): HTMLDivElement {
        val card = document.createElement("div") as HTMLDivElement
        card.style.border = "1px solid #ccc"
        card.style.borderRadius = "8px"
        card.style.padding = "12px"
        card.style.marginBottom = "10px"

        val nameEl = document.createElement("h4") as HTMLHeadingElement
        nameEl.textContent = when (f.type) {
            "Idiom", "Tool Proficiency", "Weapon/Armor Proficiency" -> f.type
            else -> f.name.ifEmpty { "(Unnamed)" }
        }
        nameEl.style.margin = "0 0 6px 0"
        card.appendChild(nameEl)

        val header = document.createElement("div") as HTMLDivElement
        header.style.display = "flex"
        header.style.justifyContent = "space-between"
        header.style.alignItems = "center"
        header.style.marginBottom = "8px"

        val left = document.createElement("div") as HTMLDivElement
        val sourceBadge = document.createElement("span") as HTMLSpanElement
        sourceBadge.textContent = f.source
        sourceBadge.style.backgroundColor = when (f.source) {
            "Class" -> "#4a90d9"
            "Origin" -> "#d9a34a"
            "Race" -> "#4ad97a"
            else -> "#999"
        }
        sourceBadge.style.color = "white"
        sourceBadge.style.padding = "2px 8px"
        sourceBadge.style.borderRadius = "4px"
        sourceBadge.style.fontSize = "12px"
        sourceBadge.style.marginRight = "8px"
        left.appendChild(sourceBadge)

        val sourceDetail = document.createElement("span") as HTMLSpanElement
        sourceDetail.textContent = featureSourceLabel(f)
        sourceDetail.style.fontSize = "13px"
        sourceDetail.style.color = "#666"
        left.appendChild(sourceDetail)
        header.appendChild(left)

        val typeBadge = document.createElement("span") as HTMLSpanElement
        typeBadge.textContent = f.type
        typeBadge.style.fontSize = "12px"
        typeBadge.style.fontStyle = "italic"
        header.appendChild(typeBadge)
        card.appendChild(header)

        val desc = document.createElement("p") as HTMLParagraphElement
        if (f.type == "Idiom") {
            desc.textContent = "Languages: ${f.description}"
        } else if (f.type == "Tool Proficiency") {
            desc.textContent = "Tools: ${f.description}"
        } else if (f.type == "Weapon/Armor Proficiency") {
            val parts = f.description.split(",").map { it.trim() }
            val armors = parts.filter { it.startsWith("armor:") }.map { it.removePrefix("armor:") }
            val cats = parts.filter { it.startsWith("weapon_cat:") }.map { it.removePrefix("weapon_cat:") }
            val weapons = parts.filter { it.startsWith("weapon:") }.map { it.removePrefix("weapon:") }
            val lines = mutableListOf<String>()
            if (armors.isNotEmpty()) lines.add("Armor: ${armors.joinToString(", ")}")
            if (cats.isNotEmpty()) lines.add("Weapon Categories: ${cats.joinToString(", ")}")
            if (weapons.isNotEmpty()) lines.add("Weapons: ${weapons.joinToString(", ")}")
            desc.textContent = lines.joinToString(" | ")
        } else {
            desc.textContent = f.description
        }
        desc.style.margin = "0 0 8px 0"
        desc.style.fontSize = "14px"
        card.appendChild(desc)

        if (f.type == "Rechargable Feature" && f.maxQuantity != null) {
            val rechargeInfo = document.createElement("p") as HTMLParagraphElement
            rechargeInfo.textContent = "Uses: ${f.maxQuantity} | Recharge: ${f.reloadRule ?: "\u2014"}"
            rechargeInfo.style.fontSize = "13px"
            rechargeInfo.style.color = "#555"
            rechargeInfo.style.margin = "0 0 8px 0"
            card.appendChild(rechargeInfo)
        }

        if (f.tags.isNotEmpty()) {
            val tagsDiv = document.createElement("div") as HTMLDivElement
            tagsDiv.style.marginBottom = "8px"
            f.tags.forEach { tag ->
                val tagSpan = document.createElement("span") as HTMLSpanElement
                tagSpan.textContent = tag
                tagSpan.style.backgroundColor = "#eee"
                tagSpan.style.padding = "2px 6px"
                tagSpan.style.borderRadius = "4px"
                tagSpan.style.fontSize = "11px"
                tagSpan.style.marginRight = "4px"
                tagsDiv.appendChild(tagSpan)
            }
            card.appendChild(tagsDiv)
        }

        val actions = document.createElement("div") as HTMLDivElement
        actions.style.display = "flex"
        actions.style.setProperty("gap", "8px")

        val editBtn = document.createElement("button") as HTMLButtonElement
        editBtn.textContent = "Edit"
        editBtn.addEventListener("click", { showFeatureModal(character, f, mainInfo) { onRefresh() } })
        actions.appendChild(editBtn)

        val deleteBtn = document.createElement("button") as HTMLButtonElement
        deleteBtn.textContent = "Delete"
        deleteBtn.style.color = "red"
        deleteBtn.addEventListener("click", {
            dndFeaturesRepo.delete(f.id)
            onRefresh()
        })
        actions.appendChild(deleteBtn)

        card.appendChild(actions)
        return card
    }

    fun refreshList() {
        container.innerHTML = ""

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add New Feature"
        addBtn.style.marginBottom = "16px"
        addBtn.addEventListener("click", { showFeatureModal(character, null, mainInfo) { refreshList() } })
        container.appendChild(addBtn)

        val featureSort = compareBy<DndFeature> { featureTypeSortOrder(it) }
            .thenBy { featureSourceSortOrder(it) }
            .thenBy { it.sourceClassLevel ?: 0 }

        val allFeatures = dndFeaturesRepo.getByCharacterId(character.id).sortedWith(featureSort)

        val proficiencyTypes = setOf("Weapon/Armor Proficiency", "Tool Proficiency", "Idiom")
        val leftFeatures = allFeatures.filter { it.type in proficiencyTypes }
        val rightFeatures = allFeatures.filter { it.type !in proficiencyTypes }

        val columns = document.createElement("div") as HTMLDivElement
        columns.style.display = "flex"
        columns.style.setProperty("gap", "16px")

        // Left: Origin, Race, Custom
        val leftCol = document.createElement("div") as HTMLDivElement
        leftCol.style.setProperty("flex", "1")
        val leftTitle = document.createElement("h3") as HTMLHeadingElement
        leftTitle.textContent = "Proficiencies & Idioms"
        leftTitle.style.marginTop = "0"
        leftCol.appendChild(leftTitle)
        if (leftFeatures.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = "No proficiencies yet."
            leftCol.appendChild(empty)
        } else {
            leftFeatures.forEach { leftCol.appendChild(buildFeatureCard(it) { refreshList() }) }
        }
        columns.appendChild(leftCol)

        // Right: Class
        val rightCol = document.createElement("div") as HTMLDivElement
        rightCol.style.setProperty("flex", "1")
        val rightTitle = document.createElement("h3") as HTMLHeadingElement
        rightTitle.textContent = "Features"
        rightTitle.style.marginTop = "0"
        rightCol.appendChild(rightTitle)
        if (rightFeatures.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = "No features yet."
            rightCol.appendChild(empty)
        } else {
            rightFeatures.forEach { rightCol.appendChild(buildFeatureCard(it) { refreshList() }) }
        }
        columns.appendChild(rightCol)

        container.appendChild(columns)
    }

    refreshList()
}

private fun showFeatureModal(
    character: Character,
    existing: DndFeature?,
    mainInfo: DndMainInfo?,
    onSave: () -> Unit
) {
    // Overlay
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"
    overlay.style.top = "0"
    overlay.style.left = "0"
    overlay.style.width = "100%"
    overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
    overlay.style.display = "flex"
    overlay.style.justifyContent = "center"
    overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")

    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"
    modal.style.borderRadius = "8px"
    modal.style.padding = "24px"
    modal.style.maxWidth = "500px"
    modal.style.width = "90%"
    modal.style.maxHeight = "80vh"
    modal.style.overflowY = "auto"

    val titleEl = document.createElement("h2") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) "Edit Feature" else "Add New Feature"
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun addLabel(text: String) {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = text
        lbl.style.fontWeight = "bold"
        form.appendChild(lbl)
    }

    // Type (first field)
    addLabel("Type")
    val typeSelect = document.createElement("select") as HTMLSelectElement
    DndFeature.types.forEach { t ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = t; opt.textContent = t; typeSelect.appendChild(opt)
    }
    typeSelect.value = existing?.type ?: "Feature"
    if (existing != null) typeSelect.disabled = true
    form.appendChild(typeSelect)

    // Name input (will be placed in dynamic content area)
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    nameInput.placeholder = "Feature name"

    // Source
    addLabel("Source")
    val sourceSelect = document.createElement("select") as HTMLSelectElement
    DndFeature.sources.forEach { s ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = s; opt.textContent = s; sourceSelect.appendChild(opt)
    }
    sourceSelect.value = existing?.source ?: "Class"
    form.appendChild(sourceSelect)

    // Source Extended container
    val sourceExtDiv = document.createElement("div") as HTMLDivElement
    sourceExtDiv.style.setProperty("grid-column", "1 / -1")

    // Source extended fields (will be rebuilt on source change)
    var sourceClassSelect: HTMLSelectElement? = null
    var sourceClassLevelInput: HTMLInputElement? = null
    var sourceOriginInput: HTMLInputElement? = null
    var sourceRaceInput: HTMLInputElement? = null
    var sourceSubRaceInput: HTMLInputElement? = null
    var sourceCustomInput: HTMLInputElement? = null

    fun buildSourceExtended() {
        sourceExtDiv.innerHTML = ""
        val inner = document.createElement("div") as HTMLDivElement
        inner.style.setProperty("display", "grid")
        inner.style.setProperty("grid-template-columns", "1fr 1fr")
        inner.style.setProperty("gap", "10px")

        when (sourceSelect.value) {
            "Class" -> {
                val lbl1 = document.createElement("label") as HTMLLabelElement
                lbl1.textContent = "Class"; lbl1.style.fontWeight = "bold"; inner.appendChild(lbl1)

                val mainClass = mainInfo?.mainClass
                val secClass = mainInfo?.secondaryClass
                val classes = listOfNotNull(mainClass, secClass).filter { it.isNotBlank() }

                if (classes.size <= 1) {
                    // Single class — show as label
                    val classVal = classes.firstOrNull() ?: ""
                    val classSpan = document.createElement("span") as HTMLSpanElement
                    classSpan.textContent = classVal
                    inner.appendChild(classSpan)
                    val inp = document.createElement("input") as HTMLInputElement
                    inp.type = "hidden"; inp.value = classVal
                    inner.appendChild(inp)
                    sourceClassSelect = null
                    // Hack: reuse sourceCustomInput to carry the value
                    val hiddenSel = document.createElement("select") as HTMLSelectElement
                    val o = document.createElement("option") as HTMLOptionElement
                    o.value = classVal; o.textContent = classVal; hiddenSel.appendChild(o)
                    hiddenSel.value = classVal
                    hiddenSel.style.display = "none"
                    inner.appendChild(hiddenSel)
                    sourceClassSelect = hiddenSel
                } else {
                    // Multiple classes — show dropdown limited to main + secondary
                    val sel = document.createElement("select") as HTMLSelectElement
                    classes.forEach { c ->
                        val o = document.createElement("option") as HTMLOptionElement
                        o.value = c; o.textContent = c; sel.appendChild(o)
                    }
                    sel.value = existing?.sourceClass ?: (mainClass ?: "")
                    inner.appendChild(sel)
                    sourceClassSelect = sel
                }

                val lbl2 = document.createElement("label") as HTMLLabelElement
                lbl2.textContent = "Level"; lbl2.style.fontWeight = "bold"; inner.appendChild(lbl2)
                val lvl = document.createElement("input") as HTMLInputElement
                lvl.type = "number"; lvl.min = "1"; lvl.max = "20"
                lvl.value = existing?.sourceClassLevel?.toString() ?: "1"
                inner.appendChild(lvl)
                sourceClassLevelInput = lvl
            }
            "Origin" -> {
                val lbl = document.createElement("label") as HTMLLabelElement
                lbl.textContent = "Origin"; lbl.style.fontWeight = "bold"; inner.appendChild(lbl)
                val valSpan = document.createElement("span") as HTMLSpanElement
                val originVal = existing?.sourceOrigin ?: (mainInfo?.origin ?: "")
                valSpan.textContent = originVal
                inner.appendChild(valSpan)
                // Hidden input to carry the value
                val inp = document.createElement("input") as HTMLInputElement
                inp.type = "hidden"; inp.value = originVal
                inner.appendChild(inp)
                sourceOriginInput = inp
            }
            "Race" -> {
                val lbl1 = document.createElement("label") as HTMLLabelElement
                lbl1.textContent = "Race"; lbl1.style.fontWeight = "bold"; inner.appendChild(lbl1)
                val raceVal = existing?.sourceRace ?: (mainInfo?.race ?: "")
                val raceSpan = document.createElement("span") as HTMLSpanElement
                raceSpan.textContent = raceVal
                inner.appendChild(raceSpan)
                val inp1 = document.createElement("input") as HTMLInputElement
                inp1.type = "hidden"; inp1.value = raceVal
                inner.appendChild(inp1)
                sourceRaceInput = inp1

                val lbl2 = document.createElement("label") as HTMLLabelElement
                lbl2.textContent = "Sub-race"; lbl2.style.fontWeight = "bold"; inner.appendChild(lbl2)
                val subRaceVal = existing?.sourceSubRace ?: (mainInfo?.subRace ?: "")
                val subRaceSpan = document.createElement("span") as HTMLSpanElement
                subRaceSpan.textContent = subRaceVal
                inner.appendChild(subRaceSpan)
                val inp2 = document.createElement("input") as HTMLInputElement
                inp2.type = "hidden"; inp2.value = subRaceVal
                inner.appendChild(inp2)
                sourceSubRaceInput = inp2
            }
            "Custom" -> {
                val lbl = document.createElement("label") as HTMLLabelElement
                lbl.textContent = "Source"; lbl.style.fontWeight = "bold"; inner.appendChild(lbl)
                val inp = document.createElement("input") as HTMLInputElement
                inp.value = existing?.sourceCustom ?: ""
                inner.appendChild(inp)
                sourceCustomInput = inp
            }
        }
        sourceExtDiv.appendChild(inner)
    }
    buildSourceExtended()
    sourceSelect.addEventListener("change", { buildSourceExtended() })
    form.appendChild(sourceExtDiv)

    // Rechargable fields container
    val rechargeDiv = document.createElement("div") as HTMLDivElement
    rechargeDiv.style.setProperty("grid-column", "1 / -1")

    var maxQtyInput: HTMLInputElement? = null
    var reloadSelect: HTMLSelectElement? = null

    fun buildRechargeFields() {
        rechargeDiv.innerHTML = ""
        if (typeSelect.value == "Rechargable Feature") {
            val inner = document.createElement("div") as HTMLDivElement
            inner.style.setProperty("display", "grid")
            inner.style.setProperty("grid-template-columns", "1fr 1fr")
            inner.style.setProperty("gap", "10px")

            val lbl1 = document.createElement("label") as HTMLLabelElement
            lbl1.textContent = "Max. Quantity"; lbl1.style.fontWeight = "bold"; inner.appendChild(lbl1)
            val qty = document.createElement("input") as HTMLInputElement
            qty.type = "number"; qty.value = existing?.maxQuantity?.toString() ?: "1"
            inner.appendChild(qty)
            maxQtyInput = qty

            val lbl2 = document.createElement("label") as HTMLLabelElement
            lbl2.textContent = "Reload Rule"; lbl2.style.fontWeight = "bold"; inner.appendChild(lbl2)
            val sel = document.createElement("select") as HTMLSelectElement
            DndFeature.reloadRules.forEach { r ->
                val o = document.createElement("option") as HTMLOptionElement
                o.value = r; o.textContent = r; sel.appendChild(o)
            }
            sel.value = existing?.reloadRule ?: "Long Rest"
            inner.appendChild(sel)
            reloadSelect = sel

            rechargeDiv.appendChild(inner)
        }
    }
    buildRechargeFields()
    typeSelect.addEventListener("change", { buildRechargeFields() })
    form.appendChild(rechargeDiv)

    // Dynamic content area (changes based on type)
    val dynamicDiv = document.createElement("div") as HTMLDivElement
    dynamicDiv.style.setProperty("grid-column", "1 / -1")

    val defaultIdioms = listOf("Common", "Dwarvish", "Elvish", "Giant", "Gnomish", "Goblin", "Halfling", "Orc", "Draconic")
    val defaultTools = listOf("Alchemist's Supplies", "Brewer's Supplies", "Calligrapher's Supplies", "Carpenter's Tools", "Cartographer's Tools", "Cobbler's Tools", "Cook's Utensils", "Glassblower's Tools", "Jeweler's Tools", "Leatherworker's Tools", "Mason's Tools", "Painter's Supplies", "Potter's Tools", "Smith's Tools", "Tinker's Tools", "Weaver's Tools", "Woodcarver's Tools", "Disguise Kit", "Forgery Kit", "Herbalism Kit", "Navigator's Tools", "Poisoner's Kit", "Thieves' Tools")
    val defaultWeapons = listOf("Club", "Dagger", "Greatclub", "Handaxe", "Javelin", "Light Hammer", "Mace", "Quarterstaff", "Sickle", "Spear", "Crossbow (Light)", "Dart", "Shortbow", "Sling", "Battleaxe", "Flail", "Glaive", "Greataxe", "Greatsword", "Halberd", "Lance", "Longsword", "Maul", "Morningstar", "Pike", "Rapier", "Scimitar", "Shortsword", "Trident", "War Pick", "Warhammer", "Whip", "Blowgun", "Crossbow (Hand)", "Crossbow (Heavy)", "Longbow", "Net")
    val selectedItems = mutableListOf<String>()
    val isListType = existing?.type == "Idiom" || existing?.type == "Tool Proficiency"
    if (isListType && existing?.description?.isNotBlank() == true) {
        selectedItems.addAll(existing.description.split(",").map { it.trim() }.filter { it.isNotEmpty() })
    }

    // Weapon/Armor proficiency state
    val armorChecks = mutableMapOf("Light" to false, "Medium" to false, "Heavy" to false, "Shields" to false)
    val weaponCatChecks = mutableMapOf("Simple" to false, "Martial" to false)
    var weaponCatOther = ""
    val selectedWeapons = mutableListOf<String>()
    if (existing?.type == "Weapon/Armor Proficiency" && existing.description.isNotBlank()) {
        existing.description.split(",").map { it.trim() }.forEach { entry ->
            when {
                entry.startsWith("armor:") -> armorChecks[entry.removePrefix("armor:")] = true
                entry.startsWith("weapon_cat:") -> {
                    val cat = entry.removePrefix("weapon_cat:")
                    if (cat == "Simple" || cat == "Martial") weaponCatChecks[cat] = true
                    else weaponCatOther = cat
                }
                entry.startsWith("weapon:") -> selectedWeapons.add(entry.removePrefix("weapon:"))
            }
        }
    }

    var descInput: HTMLTextAreaElement? = null

    fun buildDynamicContent() {
        dynamicDiv.innerHTML = ""
        if (typeSelect.value == "Idiom" || typeSelect.value == "Tool Proficiency") {
            val isIdiomType = typeSelect.value == "Idiom"
            val itemLabel = if (isIdiomType) "Languages" else "Tools"
            val defaultList = if (isIdiomType) defaultIdioms else defaultTools
            val placeholder = if (isIdiomType) "Custom language" else "Custom tool"
            val addPlaceholder = if (isIdiomType) "-- Add language --" else "-- Add tool --"

            val label = document.createElement("label") as HTMLLabelElement
            label.textContent = itemLabel
            label.style.fontWeight = "bold"
            label.style.display = "block"
            label.style.marginBottom = "6px"
            dynamicDiv.appendChild(label)

            val selectedDiv = document.createElement("div") as HTMLDivElement
            selectedDiv.style.marginBottom = "8px"
            selectedDiv.style.display = "flex"
            selectedDiv.style.setProperty("flex-wrap", "wrap")
            selectedDiv.style.setProperty("gap", "4px")

            fun refreshTags() {
                selectedDiv.innerHTML = ""
                selectedItems.forEach { item ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = "$item \u00D7"
                    badge.style.backgroundColor = "#e0e0e0"
                    badge.style.padding = "2px 8px"
                    badge.style.borderRadius = "4px"
                    badge.style.fontSize = "13px"
                    badge.style.cursor = "pointer"
                    badge.addEventListener("click", {
                        selectedItems.remove(item)
                        refreshTags()
                    })
                    selectedDiv.appendChild(badge)
                }
            }
            refreshTags()
            dynamicDiv.appendChild(selectedDiv)

            val addRow = document.createElement("div") as HTMLDivElement
            addRow.style.display = "flex"
            addRow.style.setProperty("gap", "8px")
            addRow.style.alignItems = "center"

            val itemSelect = document.createElement("select") as HTMLSelectElement
            val emptyOpt = document.createElement("option") as HTMLOptionElement
            emptyOpt.value = ""; emptyOpt.textContent = addPlaceholder
            itemSelect.appendChild(emptyOpt)
            defaultList.forEach { item ->
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = item; opt.textContent = item
                itemSelect.appendChild(opt)
            }
            val customOpt = document.createElement("option") as HTMLOptionElement
            customOpt.value = "__custom__"; customOpt.textContent = "Custom..."
            itemSelect.appendChild(customOpt)
            addRow.appendChild(itemSelect)

            val customInput = document.createElement("input") as HTMLInputElement
            customInput.placeholder = placeholder
            customInput.style.display = "none"
            customInput.style.padding = "4px"
            addRow.appendChild(customInput)

            val addBtn = document.createElement("button") as HTMLButtonElement
            addBtn.textContent = "Add"
            addRow.appendChild(addBtn)

            itemSelect.addEventListener("change", {
                customInput.style.display = if (itemSelect.value == "__custom__") "inline" else "none"
            })

            addBtn.addEventListener("click", {
                val item = if (itemSelect.value == "__custom__") {
                    customInput.value.trim().also { customInput.value = "" }
                } else {
                    itemSelect.value
                }
                if (item.isNotEmpty() && item !in selectedItems) {
                    selectedItems.add(item)
                    refreshTags()
                }
                itemSelect.value = ""
                customInput.style.display = "none"
            })

            dynamicDiv.appendChild(addRow)
        } else if (typeSelect.value == "Weapon/Armor Proficiency") {
            // Armor checkboxes
            val armorLabel = document.createElement("label") as HTMLLabelElement
            armorLabel.textContent = "Armor"
            armorLabel.style.fontWeight = "bold"
            armorLabel.style.display = "block"
            armorLabel.style.marginBottom = "6px"
            dynamicDiv.appendChild(armorLabel)

            val armorRow = document.createElement("div") as HTMLDivElement
            armorRow.style.display = "flex"
            armorRow.style.setProperty("gap", "16px")
            armorRow.style.marginBottom = "12px"

            val armorCbs = mutableMapOf<String, HTMLInputElement>()
            listOf("Light", "Medium", "Heavy", "Shields").forEach { armor ->
                val lbl = document.createElement("label") as HTMLLabelElement
                val cb = document.createElement("input") as HTMLInputElement
                cb.type = "checkbox"
                cb.checked = armorChecks[armor] == true
                cb.style.marginRight = "4px"
                lbl.appendChild(cb)
                lbl.append(armor)
                armorRow.appendChild(lbl)
                armorCbs[armor] = cb
            }
            dynamicDiv.appendChild(armorRow)

            // Weapon category checkboxes
            val weaponCatLabel = document.createElement("label") as HTMLLabelElement
            weaponCatLabel.textContent = "Weapon Categories"
            weaponCatLabel.style.fontWeight = "bold"
            weaponCatLabel.style.display = "block"
            weaponCatLabel.style.marginBottom = "6px"
            dynamicDiv.appendChild(weaponCatLabel)

            val weaponCatRow = document.createElement("div") as HTMLDivElement
            weaponCatRow.style.display = "flex"
            weaponCatRow.style.setProperty("gap", "16px")
            weaponCatRow.style.alignItems = "center"
            weaponCatRow.style.marginBottom = "8px"

            val weaponCatCbs = mutableMapOf<String, HTMLInputElement>()
            listOf("Simple", "Martial").forEach { cat ->
                val lbl = document.createElement("label") as HTMLLabelElement
                val cb = document.createElement("input") as HTMLInputElement
                cb.type = "checkbox"
                cb.checked = weaponCatChecks[cat] == true
                cb.style.marginRight = "4px"
                lbl.appendChild(cb)
                lbl.append(cat)
                weaponCatRow.appendChild(lbl)
                weaponCatCbs[cat] = cb
            }

            // Others checkbox + text
            val othersLbl = document.createElement("label") as HTMLLabelElement
            val othersCb = document.createElement("input") as HTMLInputElement
            othersCb.type = "checkbox"
            othersCb.checked = weaponCatOther.isNotEmpty()
            othersCb.style.marginRight = "4px"
            othersLbl.appendChild(othersCb)
            othersLbl.append("Others")
            weaponCatRow.appendChild(othersLbl)

            val othersInput = document.createElement("input") as HTMLInputElement
            othersInput.value = weaponCatOther
            othersInput.placeholder = "Specify..."
            othersInput.style.padding = "4px"
            othersInput.style.display = if (weaponCatOther.isNotEmpty()) "inline" else "none"
            weaponCatRow.appendChild(othersInput)

            othersCb.addEventListener("change", {
                othersInput.style.display = if (othersCb.checked) "inline" else "none"
                if (!othersCb.checked) othersInput.value = ""
            })

            dynamicDiv.appendChild(weaponCatRow)

            // Individual weapons list
            val weaponListLabel = document.createElement("label") as HTMLLabelElement
            weaponListLabel.textContent = "Individual Weapons"
            weaponListLabel.style.fontWeight = "bold"
            weaponListLabel.style.display = "block"
            weaponListLabel.style.marginBottom = "6px"
            weaponListLabel.style.marginTop = "8px"
            dynamicDiv.appendChild(weaponListLabel)

            val weaponSelectedDiv = document.createElement("div") as HTMLDivElement
            weaponSelectedDiv.style.marginBottom = "8px"
            weaponSelectedDiv.style.display = "flex"
            weaponSelectedDiv.style.setProperty("flex-wrap", "wrap")
            weaponSelectedDiv.style.setProperty("gap", "4px")

            fun refreshWeaponTags() {
                weaponSelectedDiv.innerHTML = ""
                selectedWeapons.forEach { w ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = "$w \u00D7"
                    badge.style.backgroundColor = "#e0e0e0"
                    badge.style.padding = "2px 8px"
                    badge.style.borderRadius = "4px"
                    badge.style.fontSize = "13px"
                    badge.style.cursor = "pointer"
                    badge.addEventListener("click", {
                        selectedWeapons.remove(w)
                        refreshWeaponTags()
                    })
                    weaponSelectedDiv.appendChild(badge)
                }
            }
            refreshWeaponTags()
            dynamicDiv.appendChild(weaponSelectedDiv)

            val weaponAddRow = document.createElement("div") as HTMLDivElement
            weaponAddRow.style.display = "flex"
            weaponAddRow.style.setProperty("gap", "8px")
            weaponAddRow.style.alignItems = "center"

            val weaponSelect = document.createElement("select") as HTMLSelectElement
            val wEmptyOpt = document.createElement("option") as HTMLOptionElement
            wEmptyOpt.value = ""; wEmptyOpt.textContent = "-- Add weapon --"
            weaponSelect.appendChild(wEmptyOpt)
            defaultWeapons.forEach { w ->
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = w; opt.textContent = w
                weaponSelect.appendChild(opt)
            }
            val wCustomOpt = document.createElement("option") as HTMLOptionElement
            wCustomOpt.value = "__custom__"; wCustomOpt.textContent = "Custom..."
            weaponSelect.appendChild(wCustomOpt)
            weaponAddRow.appendChild(weaponSelect)

            val wCustomInput = document.createElement("input") as HTMLInputElement
            wCustomInput.placeholder = "Custom weapon"
            wCustomInput.style.display = "none"
            wCustomInput.style.padding = "4px"
            weaponAddRow.appendChild(wCustomInput)

            val wAddBtn = document.createElement("button") as HTMLButtonElement
            wAddBtn.textContent = "Add"
            weaponAddRow.appendChild(wAddBtn)

            weaponSelect.addEventListener("change", {
                wCustomInput.style.display = if (weaponSelect.value == "__custom__") "inline" else "none"
            })

            wAddBtn.addEventListener("click", {
                val w = if (weaponSelect.value == "__custom__") {
                    wCustomInput.value.trim().also { wCustomInput.value = "" }
                } else {
                    weaponSelect.value
                }
                if (w.isNotEmpty() && w !in selectedWeapons) {
                    selectedWeapons.add(w)
                    refreshWeaponTags()
                }
                weaponSelect.value = ""
                wCustomInput.style.display = "none"
            })

            dynamicDiv.appendChild(weaponAddRow)

            // Store references for save
            armorCbs.forEach { (k, cb) -> cb.addEventListener("change", { armorChecks[k] = cb.checked }) }
            weaponCatCbs.forEach { (k, cb) -> cb.addEventListener("change", { weaponCatChecks[k] = cb.checked }) }
        } else {
            // Name field
            val nameLbl = document.createElement("label") as HTMLLabelElement
            nameLbl.textContent = "Name"
            nameLbl.style.fontWeight = "bold"
            dynamicDiv.appendChild(nameLbl)
            nameInput.style.width = "100%"
            nameInput.style.marginBottom = "8px"
            dynamicDiv.appendChild(nameInput)

            // Description textarea
            val lbl = document.createElement("label") as HTMLLabelElement
            lbl.textContent = "Description"
            lbl.style.fontWeight = "bold"
            dynamicDiv.appendChild(lbl)
            val ta = document.createElement("textarea") as HTMLTextAreaElement
            ta.value = if (existing?.type != "Idiom") (existing?.description ?: "") else ""
            ta.rows = 4
            ta.style.width = "100%"
            dynamicDiv.appendChild(ta)
            descInput = ta
        }
    }
    buildDynamicContent()
    typeSelect.addEventListener("change", { buildDynamicContent() })
    form.appendChild(dynamicDiv)

    // Tags (hidden for Idiom/Tool/Weapon-Armor)
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")

    val tagsLabel = document.createElement("label") as HTMLLabelElement
    tagsLabel.textContent = "Tags"
    tagsLabel.style.fontWeight = "bold"
    tagsLabel.style.display = "block"
    tagsLabel.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLabel)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", {
                selectedTags.remove(tag)
                refreshTagBadges()
            })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"

    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."
    tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)

    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = "Add"
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) {
            selectedTags.add(tag)
            refreshTagBadges()
        }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)

    form.appendChild(tagsContainer)

    fun updateTagsVisibility() {
        val hideTagsAndName = typeSelect.value == "Idiom" || typeSelect.value == "Tool Proficiency" || typeSelect.value == "Weapon/Armor Proficiency"
        tagsContainer.style.display = if (hideTagsAndName) "none" else ""
    }
    updateTagsVisibility()
    typeSelect.addEventListener("change", { updateTagsVisibility() })

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"
    btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"
    btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = "Cancel"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Save"
    saveBtn.addEventListener("click", {
        val isListType = typeSelect.value == "Idiom" || typeSelect.value == "Tool Proficiency"
        val isWeaponArmor = typeSelect.value == "Weapon/Armor Proficiency"
        val tags = if (isListType || isWeaponArmor) emptyList() else selectedTags.toList()
        val descriptionValue = when {
            isListType -> selectedItems.joinToString(", ")
            isWeaponArmor -> {
                val parts = mutableListOf<String>()
                armorChecks.forEach { (k, v) -> if (v) parts.add("armor:$k") }
                weaponCatChecks.forEach { (k, v) -> if (v) parts.add("weapon_cat:$k") }
                val othersVal = (dynamicDiv.querySelector("input[placeholder=\"Specify...\"]") as? HTMLInputElement)?.value?.trim() ?: ""
                if (othersVal.isNotEmpty()) parts.add("weapon_cat:$othersVal")
                selectedWeapons.forEach { parts.add("weapon:$it") }
                parts.joinToString(", ")
            }
            else -> descInput?.value ?: ""
        }
        val feature = DndFeature(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = if (isListType || isWeaponArmor) typeSelect.value else nameInput.value,
            source = sourceSelect.value,
            sourceClass = sourceClassSelect?.value,
            sourceClassLevel = sourceClassLevelInput?.value?.toIntOrNull(),
            sourceOrigin = sourceOriginInput?.value,
            sourceRace = sourceRaceInput?.value,
            sourceSubRace = sourceSubRaceInput?.value,
            sourceCustom = sourceCustomInput?.value,
            type = typeSelect.value,
            description = descriptionValue,
            maxQuantity = if (typeSelect.value == "Rechargable Feature") maxQtyInput?.value?.toIntOrNull() else null,
            reloadRule = if (typeSelect.value == "Rechargable Feature") reloadSelect?.value else null,
            tags = tags
        )
        dndFeaturesRepo.save(feature)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

private fun renderDndMainTab(character: Character, container: HTMLDivElement) {
    val info = dndMainInfoRepo.getByCharacterId(character.id) ?: DndMainInfo(characterId = character.id)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "12px")
    form.style.maxWidth = "600px"

    val customClasses = mutableListOf<String>()

    fun addField(label: String, value: String?): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        val input = document.createElement("input") as HTMLInputElement
        input.value = value ?: ""
        input.style.padding = "4px"
        form.appendChild(lbl)
        form.appendChild(input)
        return input
    }

    fun addNumberField(label: String, value: Int?): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"
        input.value = value?.toString() ?: ""
        input.style.padding = "4px"
        form.appendChild(lbl)
        form.appendChild(input)
        return input
    }

    fun buildSubClassSelect(className: String?, currentValue: String?): HTMLDivElement {
        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement
        select.style.padding = "4px"
        select.style.width = "100%"

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = "-- Select --"
        select.appendChild(emptyOpt)

        val subClasses = DungeonsAndDragons.subClassesFor(className)
        subClasses.forEach { sc ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = sc
            opt.textContent = sc
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = "Custom..."
        select.appendChild(customOpt)

        if (currentValue != null && currentValue !in subClasses) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = currentValue
            existingCustom.textContent = "$currentValue (Custom)"
            select.insertBefore(existingCustom, customOpt)
        }

        select.value = currentValue ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = "Enter custom sub-class"
        customInput.style.padding = "4px"
        customInput.style.width = "100%"
        customInput.style.display = "none"
        customInput.style.marginTop = "4px"

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.style.display = "none"
        addBtn.style.marginTop = "4px"

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.style.display = "block"
                addBtn.style.display = "inline-block"
            } else {
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue (Custom)"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        return wrapper
    }

    fun addSubClassSelect(label: String, className: String?, value: String?): HTMLDivElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        form.appendChild(lbl)
        val wrapper = buildSubClassSelect(className, value)
        form.appendChild(wrapper)
        return wrapper
    }

    fun addClassSelect(label: String, value: String?, subClassWrapper: () -> HTMLDivElement?): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        form.appendChild(lbl)

        val wrapper = document.createElement("div") as HTMLDivElement

        val select = document.createElement("select") as HTMLSelectElement
        select.style.padding = "4px"
        select.style.width = "100%"

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = "-- Select --"
        select.appendChild(emptyOpt)

        DungeonsAndDragons.defaultClasses.forEach { cls ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = cls
            opt.textContent = cls
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = "Custom..."
        select.appendChild(customOpt)

        customClassRepo.getAll().forEach { cls ->
            if (cls != value) {
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = cls
                opt.textContent = "$cls (Custom)"
                select.insertBefore(opt, customOpt)
            }
        }

        if (value != null && value !in DungeonsAndDragons.defaultClasses && value !in customClassRepo.getAll()) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = value
            existingCustom.textContent = "$value (Custom)"
            select.insertBefore(existingCustom, customOpt)
            customClasses.add(value)
        }

        select.value = value ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = "Enter custom class"
        customInput.style.padding = "4px"
        customInput.style.width = "100%"
        customInput.style.display = "none"
        customInput.style.marginTop = "4px"

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.style.display = "none"
        addBtn.style.marginTop = "4px"

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.style.display = "block"
                addBtn.style.display = "inline-block"
            } else {
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
            // Rebuild sub-class dropdown
            val scWrapper = subClassWrapper()
            if (scWrapper != null) {
                val selectedClass = if (select.value == "__custom__") null else select.value
                val newContent = buildSubClassSelect(selectedClass, null)
                scWrapper.innerHTML = ""
                while (newContent.firstChild != null) {
                    scWrapper.appendChild(newContent.firstChild!!)
                }
            }
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                customClassRepo.add(customValue)
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue (Custom)"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.style.display = "none"
                addBtn.style.display = "none"
                // Rebuild sub-class for custom class (only Custom option)
                val scWrapper = subClassWrapper()
                if (scWrapper != null) {
                    val newContent = buildSubClassSelect(customValue, null)
                    scWrapper.innerHTML = ""
                    while (newContent.firstChild != null) {
                        scWrapper.appendChild(newContent.firstChild!!)
                    }
                }
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        form.appendChild(wrapper)

        return select
    }

    // We need references to sub-class wrappers, but they're created after the class selects.
    var mainSubClassWrapper: HTMLDivElement? = null
    var secondarySubClassWrapper: HTMLDivElement? = null

    // --- Main Class row (3 columns) ---
    val mainRow = document.createElement("div") as HTMLDivElement
    mainRow.style.setProperty("display", "grid")
    mainRow.style.setProperty("grid-template-columns", "2fr 2fr 1fr")
    mainRow.style.setProperty("gap", "12px")
    mainRow.style.marginBottom = "12px"

    fun addClassSelectTo(parent: HTMLDivElement, label: String, value: String?, subClassRef: () -> HTMLDivElement?): HTMLSelectElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        col.appendChild(lbl)
        // Reuse addClassSelect logic but append to col instead of form
        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement
        select.style.padding = "4px"
        select.style.width = "100%"
        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = "-- Select --"
        select.appendChild(emptyOpt)
        DungeonsAndDragons.defaultClasses.forEach { cls ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = cls
            opt.textContent = cls
            select.appendChild(opt)
        }
        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = "Custom..."
        select.appendChild(customOpt)
        customClassRepo.getAll().forEach { cls ->
            if (cls != value) {
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = cls
                opt.textContent = "$cls (Custom)"
                select.insertBefore(opt, customOpt)
            }
        }
        if (value != null && value !in DungeonsAndDragons.defaultClasses && value !in customClassRepo.getAll()) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = value
            existingCustom.textContent = "$value (Custom)"
            select.insertBefore(existingCustom, customOpt)
        }
        select.value = value ?: ""
        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = "Enter custom class"
        customInput.style.padding = "4px"
        customInput.style.width = "100%"
        customInput.style.display = "none"
        customInput.style.marginTop = "4px"
        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.style.display = "none"
        addBtn.style.marginTop = "4px"
        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.style.display = "block"
                addBtn.style.display = "inline-block"
            } else {
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
            val scWrapper = subClassRef()
            if (scWrapper != null) {
                val selectedClass = if (select.value == "__custom__") null else select.value
                val newContent = buildSubClassSelect(selectedClass, null)
                scWrapper.innerHTML = ""
                while (newContent.firstChild != null) { scWrapper.appendChild(newContent.firstChild!!) }
            }
        })
        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                customClassRepo.add(customValue)
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue (Custom)"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.style.display = "none"
                addBtn.style.display = "none"
                val scWrapper = subClassRef()
                if (scWrapper != null) {
                    val newContent = buildSubClassSelect(customValue, null)
                    scWrapper.innerHTML = ""
                    while (newContent.firstChild != null) { scWrapper.appendChild(newContent.firstChild!!) }
                }
            }
        })
        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        col.appendChild(wrapper)
        parent.appendChild(col)
        return select
    }

    fun addSubClassSelectTo(parent: HTMLDivElement, label: String, className: String?, value: String?): HTMLDivElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        col.appendChild(lbl)
        val wrapper = buildSubClassSelect(className, value)
        col.appendChild(wrapper)
        parent.appendChild(col)
        return wrapper
    }

    fun addNumberFieldTo(parent: HTMLDivElement, label: String, value: Int?): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"
        input.value = value?.toString() ?: ""
        input.style.padding = "4px"
        input.style.width = "100%"
        col.appendChild(input)
        parent.appendChild(col)
        return input
    }

    val mainClassSelect = addClassSelectTo(mainRow, "Class", info.mainClass) { mainSubClassWrapper }
    mainSubClassWrapper = addSubClassSelectTo(mainRow, "Sub-class", info.mainClass, info.mainSubClass)
    val mainClassLevelInput = addNumberFieldTo(mainRow, "Level", info.mainClassLevel)
    mainClassLevelInput.min = "1"
    mainClassLevelInput.max = "20"
    container.appendChild(mainRow)

    // --- Secondary Class row (3 columns) ---
    val secRow = document.createElement("div") as HTMLDivElement
    secRow.style.setProperty("display", "grid")
    secRow.style.setProperty("grid-template-columns", "2fr 2fr 1fr")
    secRow.style.setProperty("gap", "12px")
    secRow.style.marginBottom = "12px"

    val secondaryClassSelect = addClassSelectTo(secRow, "Secondary Class", info.secondaryClass) { secondarySubClassWrapper }
    secondarySubClassWrapper = addSubClassSelectTo(secRow, "Sub-class", info.secondaryClass, info.secondarySubClass)
    val secondaryClassLevelInput = addNumberFieldTo(secRow, "Level", info.secondaryClassLevel)
    secondaryClassLevelInput.min = "0"
    secondaryClassLevelInput.max = "20"
    container.appendChild(secRow)

    // Enforce sum <= 20
    val levelError = document.createElement("span") as HTMLSpanElement
    levelError.style.color = "red"
    levelError.style.display = "none"
    levelError.textContent = "Sum of levels must be 20 or less"

    fun validateLevels() {
        val main = mainClassLevelInput.value.toIntOrNull() ?: 0
        val sec = secondaryClassLevelInput.value.toIntOrNull() ?: 0
        if (main + sec > 20) {
            levelError.style.display = "inline"
        } else {
            levelError.style.display = "none"
        }
    }

    mainClassLevelInput.addEventListener("input", { validateLevels() })
    secondaryClassLevelInput.addEventListener("input", { validateLevels() })

    // Insert error span after the grid (will be added to container later)
    validateLevels()
    var subRaceWrapper: HTMLDivElement? = null

    fun buildSubRaceSelect(raceName: String?, currentValue: String?): HTMLDivElement {
        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement
        select.style.padding = "4px"
        select.style.width = "100%"

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = "-- Select --"
        select.appendChild(emptyOpt)

        val subRaces = DungeonsAndDragons.subRacesFor(raceName)
        subRaces.forEach { sr ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = sr
            opt.textContent = sr
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = "Custom..."
        select.appendChild(customOpt)

        if (currentValue != null && currentValue !in subRaces) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = currentValue
            existingCustom.textContent = "$currentValue (Custom)"
            select.insertBefore(existingCustom, customOpt)
        }

        select.value = currentValue ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = "Enter custom sub-race"
        customInput.style.padding = "4px"
        customInput.style.width = "100%"
        customInput.style.display = "none"
        customInput.style.marginTop = "4px"

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.style.display = "none"
        addBtn.style.marginTop = "4px"

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.style.display = "block"
                addBtn.style.display = "inline-block"
            } else {
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue (Custom)"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        return wrapper
    }

    // Race select
    fun addRaceSelect(label: String, value: String?): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        form.appendChild(lbl)

        val wrapper = document.createElement("div") as HTMLDivElement
        val select = document.createElement("select") as HTMLSelectElement
        select.style.padding = "4px"
        select.style.width = "100%"

        val emptyOpt = document.createElement("option") as HTMLOptionElement
        emptyOpt.value = ""
        emptyOpt.textContent = "-- Select --"
        select.appendChild(emptyOpt)

        DungeonsAndDragons.defaultRaces.forEach { r ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = r
            opt.textContent = r
            select.appendChild(opt)
        }

        val customOpt = document.createElement("option") as HTMLOptionElement
        customOpt.value = "__custom__"
        customOpt.textContent = "Custom..."
        select.appendChild(customOpt)

        if (value != null && value !in DungeonsAndDragons.defaultRaces) {
            val existingCustom = document.createElement("option") as HTMLOptionElement
            existingCustom.value = value
            existingCustom.textContent = "$value (Custom)"
            select.insertBefore(existingCustom, customOpt)
        }

        select.value = value ?: ""

        val customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = "Enter custom race"
        customInput.style.padding = "4px"
        customInput.style.width = "100%"
        customInput.style.display = "none"
        customInput.style.marginTop = "4px"

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "Add"
        addBtn.style.display = "none"
        addBtn.style.marginTop = "4px"

        fun rebuildSubRace(raceName: String?) {
            val srw = subRaceWrapper ?: return
            val newContent = buildSubRaceSelect(raceName, null)
            srw.innerHTML = ""
            while (newContent.firstChild != null) {
                srw.appendChild(newContent.firstChild!!)
            }
        }

        select.addEventListener("change", {
            if (select.value == "__custom__") {
                customInput.style.display = "block"
                addBtn.style.display = "inline-block"
            } else {
                customInput.style.display = "none"
                addBtn.style.display = "none"
            }
            rebuildSubRace(if (select.value == "__custom__") null else select.value)
        })

        addBtn.addEventListener("click", {
            val customValue = customInput.value.trim()
            if (customValue.isNotEmpty()) {
                val newOpt = document.createElement("option") as HTMLOptionElement
                newOpt.value = customValue
                newOpt.textContent = "$customValue (Custom)"
                select.insertBefore(newOpt, customOpt)
                select.value = customValue
                customInput.value = ""
                customInput.style.display = "none"
                addBtn.style.display = "none"
                rebuildSubRace(customValue)
            }
        })

        wrapper.appendChild(select)
        wrapper.appendChild(customInput)
        wrapper.appendChild(addBtn)
        form.appendChild(wrapper)
        return select
    }

    fun addSubRaceSelect(label: String, raceName: String?, value: String?): HTMLDivElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        lbl.style.fontWeight = "bold"
        form.appendChild(lbl)
        val wrapper = buildSubRaceSelect(raceName, value)
        form.appendChild(wrapper)
        return wrapper
    }

    val raceSelect = addRaceSelect("Race", info.race)
    subRaceWrapper = addSubRaceSelect("Sub-race", info.race, info.subRace)
    val originInput = addField("Origin", info.origin)
    // Alignment select
    val alignmentLbl = document.createElement("label") as HTMLLabelElement
    alignmentLbl.textContent = "Alignment"
    alignmentLbl.style.fontWeight = "bold"
    form.appendChild(alignmentLbl)
    val alignmentSelect = document.createElement("select") as HTMLSelectElement
    alignmentSelect.style.padding = "4px"
    alignmentSelect.style.width = "100%"
    val alignEmptyOpt = document.createElement("option") as HTMLOptionElement
    alignEmptyOpt.value = ""
    alignEmptyOpt.textContent = "-- Select --"
    alignmentSelect.appendChild(alignEmptyOpt)
    DungeonsAndDragons.defaultAlignments.forEach { a ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = a
        opt.textContent = a
        alignmentSelect.appendChild(opt)
    }
    alignmentSelect.value = info.alignment ?: ""
    form.appendChild(alignmentSelect)

    container.appendChild(form)
    container.appendChild(levelError)

    // Auto-save status indicator
    val statusEl = document.createElement("span") as HTMLSpanElement
    statusEl.style.marginTop = "8px"
    statusEl.style.display = "block"
    statusEl.style.fontSize = "14px"
    container.appendChild(statusEl)

    var saveTimeout = 0

    fun autoSave() {
        val mainLevel = mainClassLevelInput.value.toIntOrNull() ?: 0
        val secLevel = secondaryClassLevelInput.value.toIntOrNull() ?: 0
        if (mainLevel + secLevel > 20) return

        statusEl.textContent = "Saving..."
        statusEl.style.color = "gray"

        if (saveTimeout != 0) window.clearTimeout(saveTimeout)
        saveTimeout = window.setTimeout({
            val mainSubSelect = mainSubClassWrapper?.querySelector("select") as? HTMLSelectElement
            val secSubSelect = secondarySubClassWrapper?.querySelector("select") as? HTMLSelectElement
            val updated = DndMainInfo(
                characterId = character.id,
                mainClass = mainClassSelect.value.ifBlank { null },
                mainSubClass = mainSubSelect?.value?.ifBlank { null },
                mainClassLevel = mainClassLevelInput.value.toIntOrNull(),
                secondaryClass = secondaryClassSelect.value.ifBlank { null },
                secondarySubClass = secSubSelect?.value?.ifBlank { null },
                secondaryClassLevel = secondaryClassLevelInput.value.toIntOrNull(),
                race = raceSelect.value.ifBlank { null },
                subRace = (subRaceWrapper?.querySelector("select") as? HTMLSelectElement)?.value?.ifBlank { null },
                origin = originInput.value.ifBlank { null },
                alignment = alignmentSelect.value.ifBlank { null }
            )
            dndMainInfoRepo.save(updated)
            statusEl.textContent = "\u2713 Saved"
            statusEl.style.color = "green"
            null
        }, 500)
    }

    // Attach auto-save to all inputs
    container.addEventListener("input", { autoSave() })
    container.addEventListener("change", { autoSave() })

    // Track previous values for feature-deletion warnings
    var prevMainClass = info.mainClass ?: ""
    var prevSecondaryClass = info.secondaryClass ?: ""
    var prevRace = info.race ?: ""
    var prevOrigin = info.origin ?: ""
    var prevMainSubClass = info.mainSubClass
    var prevSecondarySubClass = info.secondarySubClass
    var prevSubRace = info.subRace
    var prevMainLevel = info.mainClassLevel ?: 1
    var prevSecondaryLevel = info.secondaryClassLevel ?: 0

    fun checkFeatureWarning(source: String, oldValue: String, newValue: String, matchFn: (DndFeature) -> Boolean, revert: () -> Unit) {
        if (oldValue == newValue || newValue.isBlank()) return
        val features = dndFeaturesRepo.getByCharacterId(character.id)
        val affected = features.filter(matchFn)
        if (affected.isEmpty()) return
        val names = affected.joinToString(", ") { it.name.ifEmpty { "(Unnamed)" } }
        val confirmed = window.confirm("Changing $source will delete ${affected.size} feature(s): $names\n\nProceed?")
        if (confirmed) {
            affected.forEach { dndFeaturesRepo.delete(it.id) }
        } else {
            revert()
        }
    }

    fun rebuildSubClass(wrapper: HTMLDivElement?, className: String?, currentValue: String?) {
        if (wrapper == null) return
        val newContent = buildSubClassSelect(className, currentValue)
        wrapper.innerHTML = ""
        while (newContent.firstChild != null) { wrapper.appendChild(newContent.firstChild!!) }
    }

    fun rebuildSubRaceWrapper(raceName: String?, currentValue: String?) {
        val srw = subRaceWrapper ?: return
        val newContent = buildSubRaceSelect(raceName, currentValue)
        srw.innerHTML = ""
        while (newContent.firstChild != null) { srw.appendChild(newContent.firstChild!!) }
    }

    mainClassSelect.addEventListener("change", {
        val newVal = mainClassSelect.value
        checkFeatureWarning("Main Class", prevMainClass, newVal,
            { it.source == "Class" && it.sourceClass == prevMainClass },
            {
                mainClassSelect.value = prevMainClass
                rebuildSubClass(mainSubClassWrapper, prevMainClass, prevMainSubClass)
            }
        )
        prevMainClass = mainClassSelect.value
        prevMainSubClass = (mainSubClassWrapper?.querySelector("select") as? HTMLSelectElement)?.value
    })

    secondaryClassSelect.addEventListener("change", {
        val newVal = secondaryClassSelect.value
        checkFeatureWarning("Secondary Class", prevSecondaryClass, newVal,
            { it.source == "Class" && it.sourceClass == prevSecondaryClass },
            {
                secondaryClassSelect.value = prevSecondaryClass
                rebuildSubClass(secondarySubClassWrapper, prevSecondaryClass, prevSecondarySubClass)
            }
        )
        prevSecondaryClass = secondaryClassSelect.value
        prevSecondarySubClass = (secondarySubClassWrapper?.querySelector("select") as? HTMLSelectElement)?.value
    })

    raceSelect.addEventListener("change", {
        val newVal = raceSelect.value
        if (newVal != "__custom__") {
            checkFeatureWarning("Race", prevRace, newVal,
                { it.source == "Race" },
                {
                    raceSelect.value = prevRace
                    rebuildSubRaceWrapper(prevRace, prevSubRace)
                }
            )
            prevRace = raceSelect.value
            prevSubRace = (subRaceWrapper?.querySelector("select") as? HTMLSelectElement)?.value
        }
    })

    originInput.addEventListener("change", {
        val newVal = originInput.value
        checkFeatureWarning("Origin", prevOrigin, newVal,
            { it.source == "Origin" },
            { originInput.value = prevOrigin }
        )
        prevOrigin = originInput.value
    })

    mainClassLevelInput.addEventListener("change", {
        val newLevel = mainClassLevelInput.value.toIntOrNull() ?: 1
        if (newLevel < prevMainLevel) {
            val currentClass = mainClassSelect.value
            val features = dndFeaturesRepo.getByCharacterId(character.id)
            val affected = features.filter {
                it.source == "Class" && it.sourceClass == currentClass
                    && (it.sourceClassLevel ?: 0) > newLevel
            }
            if (affected.isNotEmpty()) {
                val names = affected.joinToString(", ") { it.name.ifEmpty { "(Unnamed)" } }
                val confirmed = window.confirm(
                    "Lowering ${currentClass} level to $newLevel will delete ${affected.size} feature(s) above that level: $names\n\nProceed?"
                )
                if (confirmed) {
                    affected.forEach { dndFeaturesRepo.delete(it.id) }
                } else {
                    mainClassLevelInput.value = prevMainLevel.toString()
                    return@addEventListener
                }
            }
        }
        prevMainLevel = mainClassLevelInput.value.toIntOrNull() ?: 1
    })

    secondaryClassLevelInput.addEventListener("change", {
        val newLevel = secondaryClassLevelInput.value.toIntOrNull() ?: 0
        if (newLevel < prevSecondaryLevel) {
            val currentClass = secondaryClassSelect.value
            val features = dndFeaturesRepo.getByCharacterId(character.id)
            val affected = features.filter {
                it.source == "Class" && it.sourceClass == currentClass
                    && (it.sourceClassLevel ?: 0) > newLevel
            }
            if (affected.isNotEmpty()) {
                val names = affected.joinToString(", ") { it.name.ifEmpty { "(Unnamed)" } }
                val confirmed = window.confirm(
                    "Lowering ${currentClass} level to $newLevel will delete ${affected.size} feature(s) above that level: $names\n\nProceed?"
                )
                if (confirmed) {
                    affected.forEach { dndFeaturesRepo.delete(it.id) }
                } else {
                    secondaryClassLevelInput.value = prevSecondaryLevel.toString()
                    return@addEventListener
                }
            }
        }
        prevSecondaryLevel = secondaryClassLevelInput.value.toIntOrNull() ?: 0
    })
}

private fun showCharacterDetail(character: Character) {
    app.innerHTML = ""
    app.style.display = "flex"
    app.style.height = "100vh"

    // Left panel (3/4)
    val leftPanel = document.createElement("div") as HTMLDivElement
    leftPanel.style.width = "100%"
    leftPanel.style.padding = "16px"
    leftPanel.style.overflowY = "auto"

    // Header
    val header = document.createElement("div") as HTMLDivElement
    header.style.display = "flex"
    header.style.alignItems = "center"
    header.style.marginBottom = "16px"

    val headerImgKey = "char_image_${character.id}"
    val headerImage = localStorage.getItem(headerImgKey) ?: character.imageBase64
    if (headerImage != null) {
        val img = document.createElement("img") as HTMLImageElement
        img.src = headerImage
        img.id = "header-char-img"
        img.style.width = "80px"
        img.style.height = "80px"
        img.style.marginRight = "16px"
        img.style.borderRadius = "8px"
        header.appendChild(img)
    }

    val info = document.createElement("div") as HTMLDivElement
    val nameEl = document.createElement("h1")
    nameEl.id = "header-char-name"
    val charNameKey = "char_name_${character.id}"
    nameEl.textContent = localStorage.getItem(charNameKey) ?: character.name
    info.appendChild(nameEl)
    val modelEl = document.createElement("p")
    modelEl.textContent = "Sheet: ${character.sheetModel.name}"
    info.appendChild(modelEl)
    header.appendChild(info)

    leftPanel.appendChild(header)

    // Tabs
    val tabBar = document.createElement("div") as HTMLDivElement
    tabBar.style.display = "flex"
    tabBar.style.borderBottom = "1px solid #ccc"
    tabBar.style.marginBottom = "16px"

    val tabContent = document.createElement("div") as HTMLDivElement

    character.sheetModel.tabs.forEachIndexed { index, tabName ->
        val tabBtn = document.createElement("button") as HTMLButtonElement
        tabBtn.textContent = tabName
        tabBtn.style.padding = "8px 16px"
        tabBtn.style.border = "none"
        tabBtn.style.cursor = "pointer"
        tabBtn.style.backgroundColor = if (index == 0) "#e0e0e0" else "transparent"
        tabBtn.addEventListener("click", {
            val buttons = tabBar.querySelectorAll("button")
            for (i in 0 until buttons.length) {
                (buttons.item(i) as? HTMLButtonElement)?.style?.backgroundColor = "transparent"
            }
            tabBtn.style.backgroundColor = "#e0e0e0"
            renderTabContent(tabName, character, tabContent)
        })
        tabBar.appendChild(tabBtn)
    }

    leftPanel.appendChild(tabBar)

    // Default tab content (first tab)
    renderTabContent(character.sheetModel.tabs.first(), character, tabContent)
    leftPanel.appendChild(tabContent)

    // Back button
    val backBtn = document.createElement("button") as HTMLButtonElement
    backBtn.textContent = "Back to selection"
    backBtn.style.marginTop = "16px"
    backBtn.addEventListener("click", {
        app.style.display = ""
        app.style.height = ""
        showListScreen()
    })
    leftPanel.appendChild(backBtn)

    app.appendChild(leftPanel)
}

private fun showListScreen() {
    app.innerHTML = ""

    val title = document.createElement("h1")
    title.textContent = "Dungeons And Deigo"
    app.appendChild(title)

    // Character list
    val listDiv = document.createElement("div") as HTMLDivElement
    app.appendChild(listDiv)

    val characters = repo.getAll()
    if (characters.isEmpty()) {
        listDiv.textContent = "No characters created yet."
    } else {
        val ul = document.createElement("ul") as HTMLUListElement
        ul.style.listStyle = "none"
        ul.style.padding = "0"
        characters.forEach { c ->
            val li = document.createElement("li") as HTMLLIElement
            li.style.display = "flex"
            li.style.alignItems = "center"
            li.style.marginBottom = "8px"
            li.style.cursor = "pointer"
            if (c.imageBase64 != null) {
                val img = document.createElement("img") as HTMLImageElement
                img.src = c.imageBase64!!
                img.style.width = "40px"
                img.style.height = "40px"
                img.style.marginRight = "8px"
                img.style.borderRadius = "4px"
                li.appendChild(img)
            }
            val span = document.createElement("span")
            span.textContent = "${c.name} (${c.sheetModel.name})"
            li.appendChild(span)
            li.addEventListener("click", { showCharacterDetail(c) })
            ul.appendChild(li)
        }
        listDiv.appendChild(ul)
    }

    // Create button
    val createBtn = document.createElement("button") as HTMLButtonElement
    createBtn.textContent = "Create a new Character"
    app.appendChild(createBtn)

    val formDiv = document.createElement("div") as HTMLDivElement
    formDiv.style.display = "none"
    app.appendChild(formDiv)

    createBtn.addEventListener("click", { formDiv.style.display = "block" })

    // Character Name
    val nameLabel = document.createElement("label")
    nameLabel.textContent = "Character Name: "
    val nameInput = document.createElement("input") as HTMLInputElement
    formDiv.appendChild(nameLabel)
    formDiv.appendChild(nameInput)
    formDiv.appendChild(document.createElement("br"))

    // Character Model dropdown
    val modelLabel = document.createElement("label")
    modelLabel.textContent = "Character Model: "
    val modelSelect = document.createElement("select") as HTMLSelectElement
    availableSheetModels.forEachIndexed { index, model ->
        val option = document.createElement("option") as HTMLOptionElement
        option.value = index.toString()
        option.textContent = model.name
        modelSelect.appendChild(option)
    }
    formDiv.appendChild(modelLabel)
    formDiv.appendChild(modelSelect)
    formDiv.appendChild(document.createElement("br"))

    // Image upload
    val imageLabel = document.createElement("label")
    imageLabel.textContent = "Character Image: "
    val imageInput = document.createElement("input") as HTMLInputElement
    imageInput.type = "file"
    imageInput.accept = "image/*"
    formDiv.appendChild(imageLabel)
    formDiv.appendChild(imageInput)
    formDiv.appendChild(document.createElement("br"))

    var imageBase64: String? = null
    imageInput.addEventListener("change", {
        val file = imageInput.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result
                if (result != null) {
                    imageBase64 = result.toString()
                }
                Unit
            }
            reader.readAsDataURL(file)
        }
    })

    // Submit
    val submitBtn = document.createElement("button") as HTMLButtonElement
    submitBtn.textContent = "Submit"
    formDiv.appendChild(submitBtn)

    submitBtn.addEventListener("click", {
        val charName = nameInput.value
        val selectedModel = availableSheetModels[modelSelect.value.toInt()]
        val character = Character(name = charName, sheetModelName = selectedModel.name, imageBase64 = imageBase64)
        val id = repo.insert(character)
        showCharacterDetail(character.copy(id = id))
    })
}
