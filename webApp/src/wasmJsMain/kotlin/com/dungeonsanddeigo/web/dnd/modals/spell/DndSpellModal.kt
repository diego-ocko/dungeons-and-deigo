package com.dungeonsanddeigo.web.dnd.modals.spell

import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showSpellModal(character: Character, existing: com.dungeonsanddeigo.model.DndSpell?, onDone: () -> Unit) {
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
    titleEl.textContent = if (existing != null) t("magic.editSpell") else t("magic.addSpell")
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("gap", "10px")

    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
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

    fun labeledSelect(parent: HTMLDivElement, label: String, options: List<String>, value: String, translator: ((String) -> String)? = null): HTMLSelectElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.fontSize = "11px"; lbl.style.display = "block"
        col.appendChild(lbl)
        val sel = document.createElement("select") as HTMLSelectElement
        sel.style.width = "100%"; sel.style.padding = "4px"
        options.forEach { o -> val opt = document.createElement("option") as HTMLOptionElement; opt.value = o; opt.textContent = translator?.invoke(o) ?: o; sel.appendChild(opt) }
        sel.value = value
        col.appendChild(sel)
        parent.appendChild(col)
        return sel
    }

    val nameInput = labeledInput(row1, t("label.name"), existing?.name ?: "")
    val originClassSel = labeledSelect(row1, t("magic.originClass"), classes.ifEmpty { listOf("") }, existing?.originClass ?: classes.firstOrNull() ?: "") { tDnd("class", it) }
    val originLevelSel = labeledSelect(row1, t("magic.originLevel"), com.dungeonsanddeigo.model.DndSpell.originLevels, existing?.originLevel ?: "1") { tOriginLevel(it) }
    form.appendChild(row1)

    // Row 2: Circle, School, Prepared
    val row2 = document.createElement("div") as HTMLDivElement
    row2.style.setProperty("display", "grid")
    row2.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
    row2.style.setProperty("gap", "8px")

    val circleSel = labeledSelect(row2, t("magic.circle"), com.dungeonsanddeigo.model.DndSpell.circles, existing?.circle ?: "Cantrip") { tCircle(it) }
    val schoolSel = labeledSelect(row2, t("magic.school"), com.dungeonsanddeigo.model.DndSpell.schools, existing?.school ?: com.dungeonsanddeigo.model.DndSpell.schools.first()) { tSchool(it) }

    val prepCol = document.createElement("div") as HTMLDivElement
    val prepLbl = document.createElement("label") as HTMLLabelElement
    prepLbl.textContent = t("magic.prepared"); prepLbl.style.fontWeight = "bold"; prepLbl.style.fontSize = "11px"; prepLbl.style.display = "block"
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

    val castingTimeInput = labeledInput(row3, t("magic.castingTime"), existing?.castingTime ?: "")
    val durationInput = labeledInput(row3, t("magic.duration"), existing?.duration ?: "")

    // Range with m suffix
    val rangeCol = document.createElement("div") as HTMLDivElement
    val rangeLbl = document.createElement("label") as HTMLLabelElement
    rangeLbl.textContent = t("magic.range"); rangeLbl.style.fontWeight = "bold"; rangeLbl.style.fontSize = "11px"; rangeLbl.style.display = "block"
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
    ritualLbl.appendChild(ritualCb); ritualLbl.append(t("magic.ritual"))
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

    val concentrationCb = inlineCb(row4, t("magic.concentration"), existing?.needsConcentration ?: false)
    val verbalCb = inlineCb(row4, "V", existing?.hasVerbal ?: false)
    val somaticCb = inlineCb(row4, "S", existing?.hasSomatic ?: false)
    val materialCb = inlineCb(row4, "M", existing?.hasMaterial ?: false)

    val materialInput = document.createElement("input") as HTMLInputElement
    materialInput.value = existing?.materialComponents ?: ""
    materialInput.placeholder = t("magic.materialComponents")
    materialInput.style.padding = "4px"; materialInput.style.width = "100%"
    materialInput.style.display = if (existing?.hasMaterial == true) "" else "none"
    row4.appendChild(materialInput)

    materialCb.addEventListener("change", {
        materialInput.style.display = if (materialCb.checked) "" else "none"
    })

    form.appendChild(row4)

    // Description
    val descLbl = document.createElement("label") as HTMLLabelElement
    descLbl.textContent = t("label.description"); descLbl.style.fontWeight = "bold"; descLbl.style.fontSize = "11px"
    form.appendChild(descLbl)
    val descInput = document.createElement("textarea") as HTMLTextAreaElement
    descInput.value = existing?.description ?: ""; descInput.rows = 4; descInput.style.width = "100%"
    form.appendChild(descInput)

    // Higher Circles
    val higherLbl = document.createElement("label") as HTMLLabelElement
    higherLbl.textContent = t("magic.higherCircles"); higherLbl.style.fontWeight = "bold"; higherLbl.style.fontSize = "11px"
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
    attackLbl.appendChild(attackCb); attackLbl.append(t("magic.isAttack")); attackLbl.style.fontWeight = "bold"; attackLbl.style.fontSize = "12px"
    attackRow.appendChild(attackLbl)

    val savingThrowLbl = document.createElement("label") as HTMLLabelElement
    val savingThrowCb = document.createElement("input") as HTMLInputElement
    savingThrowCb.type = "checkbox"; savingThrowCb.checked = existing?.needsSavingThrow ?: false; savingThrowCb.style.marginRight = "4px"
    savingThrowLbl.appendChild(savingThrowCb); savingThrowLbl.append(t("magic.needSavingThrow")); savingThrowLbl.style.fontWeight = "bold"; savingThrowLbl.style.fontSize = "12px"
    attackRow.appendChild(savingThrowLbl)
    form.appendChild(attackRow)

    // Attack details row (Damage Dice + Damage Type)
    val atkDetailsRow = document.createElement("div") as HTMLDivElement
    atkDetailsRow.style.setProperty("display", "grid")
    atkDetailsRow.style.setProperty("grid-template-columns", "1fr 1fr")
    atkDetailsRow.style.setProperty("gap", "8px")
    atkDetailsRow.style.display = if (existing?.isAttack == true) "grid" else "none"

    val diceInput = labeledInput(atkDetailsRow, t("inv.damageDice"), existing?.attackDamageDice ?: "")
    val dmgTypeSel = labeledSelect(atkDetailsRow, t("inv.damageType"), com.dungeonsanddeigo.model.DndSpell.damageTypes, existing?.attackDamageType ?: "Fire") { tSpellDamageType(it) }
    form.appendChild(atkDetailsRow)

    // Saving Throw details (Save Ability)
    val saveDetailsRow = document.createElement("div") as HTMLDivElement
    saveDetailsRow.style.setProperty("display", "grid")
    saveDetailsRow.style.setProperty("grid-template-columns", "1fr")
    saveDetailsRow.style.setProperty("gap", "8px")
    saveDetailsRow.style.maxWidth = "200px"
    saveDetailsRow.style.display = if (existing?.needsSavingThrow == true) "grid" else "none"

    val saveSel = labeledSelect(saveDetailsRow, t("magic.saveAbility"), com.dungeonsanddeigo.model.DndSpell.savingThrowAbilities, existing?.savingThrowAbility ?: "Dex") { tStat(it) }
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
    tagsLbl.textContent = t("label.tags"); tagsLbl.style.fontWeight = "bold"; tagsLbl.style.fontSize = "11px"
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
    tagAddBtn.textContent = t("btn.add")
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
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = t("btn.save")
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
        Repos.spell.save(spell)
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

