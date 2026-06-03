package com.dungeonsanddeigo.web.dnd.modals.spell

import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndSpell
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import kotlinx.browser.document
import org.w3c.dom.*

class DndSpellModal(
    private val character: Character,
    private val existing: DndSpell?
) : DndModal(
    title = if (existing != null) t("magic.editSpell") else t("magic.addSpell"),
    size = "xl"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var originClassSel: HTMLSelectElement
    private lateinit var originLevelSel: HTMLSelectElement
    private lateinit var circleSel: HTMLSelectElement
    private lateinit var schoolSel: HTMLSelectElement
    private lateinit var preparedCb: HTMLInputElement
    private lateinit var castingTimeInput: HTMLInputElement
    private lateinit var durationInput: HTMLInputElement
    private lateinit var rangeInput: HTMLInputElement
    private lateinit var ritualCb: HTMLInputElement
    private lateinit var concentrationCb: HTMLInputElement
    private lateinit var verbalCb: HTMLInputElement
    private lateinit var somaticCb: HTMLInputElement
    private lateinit var materialCb: HTMLInputElement
    private lateinit var materialInput: HTMLInputElement
    private lateinit var descInput: HTMLTextAreaElement
    private lateinit var higherInput: HTMLTextAreaElement
    private lateinit var attackCb: HTMLInputElement
    private lateinit var savingThrowCb: HTMLInputElement
    private lateinit var diceInput: HTMLInputElement
    private lateinit var dmgTypeSel: HTMLSelectElement
    private lateinit var saveSel: HTMLSelectElement
    private lateinit var tagsField: TagsField

    private fun labeledInput(parent: HTMLDivElement, label: String, value: String): HTMLInputElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val input = document.createElement("input") as HTMLInputElement
        input.value = value
        col.appendChild(input)
        parent.appendChild(col)
        return input
    }

    private fun labeledSelect(parent: HTMLDivElement, label: String, options: List<String>, value: String, translator: ((String) -> String)? = null): HTMLSelectElement {
        val col = document.createElement("div") as HTMLDivElement
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label
        col.appendChild(lbl)
        val sel = document.createElement("select") as HTMLSelectElement
        options.forEach { o -> val opt = document.createElement("option") as HTMLOptionElement; opt.value = o; opt.textContent = translator?.invoke(o) ?: o; sel.appendChild(opt) }
        sel.value = value
        col.appendChild(sel)
        parent.appendChild(col)
        return sel
    }

    private fun inlineCb(parent: HTMLDivElement, label: String, checked: Boolean): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"; cb.checked = checked
        lbl.appendChild(cb); lbl.append(label)
        parent.appendChild(lbl)
        return cb
    }

    override fun buildForm(form: HTMLDivElement) {
        val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
        val classes = listOfNotNull(mainInfo?.mainClass, mainInfo?.secondaryClass).filter { it.isNotBlank() }

        // Row 1: Name, Origin Class, Origin Level
        val row1 = document.createElement("div") as HTMLDivElement
        row1.className = "spell-modal__row3"
        nameInput = labeledInput(row1, t("label.name"), existing?.name ?: "")
        originClassSel = labeledSelect(row1, t("magic.originClass"), classes.ifEmpty { listOf("") }, existing?.originClass ?: classes.firstOrNull() ?: "") { tDnd("class", it) }
        originLevelSel = labeledSelect(row1, t("magic.originLevel"), DndSpell.originLevels, existing?.originLevel ?: "1") { tOriginLevel(it) }
        form.appendChild(row1)

        // Row 2: Circle, School, Prepared
        val row2 = document.createElement("div") as HTMLDivElement
        row2.className = "spell-modal__row3"
        circleSel = labeledSelect(row2, t("magic.circle"), DndSpell.circles, existing?.circle ?: "Cantrip") { tCircle(it) }
        schoolSel = labeledSelect(row2, t("magic.school"), DndSpell.schools, existing?.school ?: DndSpell.schools.first()) { tSchool(it) }

        val prepCol = document.createElement("div") as HTMLDivElement
        val prepLbl = document.createElement("label") as HTMLLabelElement
        prepLbl.textContent = t("magic.prepared")
        prepCol.appendChild(prepLbl)
        preparedCb = document.createElement("input") as HTMLInputElement
        preparedCb.type = "checkbox"; preparedCb.checked = existing?.isPrepared ?: false
        prepCol.appendChild(preparedCb)
        row2.appendChild(prepCol)

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
        row3.className = "spell-modal__row4"
        castingTimeInput = labeledInput(row3, t("magic.castingTime"), existing?.castingTime ?: "")
        durationInput = labeledInput(row3, t("magic.duration"), existing?.duration ?: "")

        val rangeCol = document.createElement("div") as HTMLDivElement
        val rangeLbl = document.createElement("label") as HTMLLabelElement
        rangeLbl.textContent = t("inv.rangeMeter")
        rangeCol.appendChild(rangeLbl)
        rangeInput = document.createElement("input") as HTMLInputElement
        rangeInput.type = "number"; rangeInput.min = "0"
        rangeInput.value = existing?.range ?: ""
        rangeCol.appendChild(rangeInput)
        row3.appendChild(rangeCol)

        val ritualCol = document.createElement("div") as HTMLDivElement
        ritualCb = document.createElement("input") as HTMLInputElement
        ritualCb.type = "checkbox"; ritualCb.checked = existing?.canBeRitual ?: false
        val ritualLbl = document.createElement("label") as HTMLLabelElement
        ritualLbl.appendChild(ritualCb); ritualLbl.append(t("magic.ritual"))
        ritualCol.appendChild(ritualLbl)
        row3.appendChild(ritualCol)
        form.appendChild(row3)

        // Row 4: Concentration + V, S, M, Material Components
        val row4 = document.createElement("div") as HTMLDivElement
        row4.className = "spell-modal__components"
        concentrationCb = inlineCb(row4, t("magic.concentration"), existing?.needsConcentration ?: false)
        verbalCb = inlineCb(row4, "V", existing?.hasVerbal ?: false)
        somaticCb = inlineCb(row4, "S", existing?.hasSomatic ?: false)
        materialCb = inlineCb(row4, "M", existing?.hasMaterial ?: false)

        materialInput = document.createElement("input") as HTMLInputElement
        materialInput.value = existing?.materialComponents ?: ""
        materialInput.placeholder = t("magic.materialComponents")
        materialInput.style.display = if (existing?.hasMaterial == true) "" else "none"
        row4.appendChild(materialInput)
        materialCb.addEventListener("change", { materialInput.style.display = if (materialCb.checked) "" else "none" })
        form.appendChild(row4)

        // Description
        descInput = addTextarea(form, t("magic.description"), existing?.description ?: "", rows = 4, classes = arrayOf("modal__full-width")).input

        // Higher Circles
        higherInput = addTextarea(form, t("magic.higherShort"), existing?.higherCircles ?: "", rows = 2, classes = arrayOf("modal__full-width")).input

        // Attack / Saving Throw
        val attackRow = document.createElement("div") as HTMLDivElement
        attackRow.className = "spell-modal__attack-row"
        attackCb = inlineCb(attackRow, t("magic.isAttack"), existing?.isAttack ?: false)
        savingThrowCb = inlineCb(attackRow, t("magic.needsSavingThrow"), existing?.needsSavingThrow ?: false)
        form.appendChild(attackRow)

        // Attack details
        val atkDetailsRow = document.createElement("div") as HTMLDivElement
        atkDetailsRow.className = "spell-modal__details-row"
        atkDetailsRow.style.display = if (existing?.isAttack == true) "grid" else "none"
        diceInput = labeledInput(atkDetailsRow, t("inv.damageDice"), existing?.attackDamageDice ?: "")
        dmgTypeSel = labeledSelect(atkDetailsRow, t("inv.damageType"), DndSpell.damageTypes, existing?.attackDamageType ?: "Fire") { tSpellDamageType(it) }
        form.appendChild(atkDetailsRow)

        // Save details
        val saveDetailsRow = document.createElement("div") as HTMLDivElement
        saveDetailsRow.className = "spell-modal__details-row"
        saveDetailsRow.style.display = if (existing?.needsSavingThrow == true) "grid" else "none"
        saveSel = labeledSelect(saveDetailsRow, t("magic.saveAbility"), DndSpell.savingThrowAbilities, existing?.savingThrowAbility ?: "Dex") { tStat(it) }
        form.appendChild(saveDetailsRow)

        attackCb.addEventListener("change", { atkDetailsRow.style.display = if (attackCb.checked) "grid" else "none" })
        savingThrowCb.addEventListener("change", { saveDetailsRow.style.display = if (savingThrowCb.checked) "grid" else "none" })

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())
    }

    override fun onSave(close: () -> Unit) {
        val spell = DndSpell(
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
            tags = tagsField.getTags()
        )
        Repos.spell.save(spell)
        close()
    }
}

