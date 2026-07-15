package com.dungeonsanddeigo.web.dnd.modals.spell

import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndSpell
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import kotlinx.browser.document
import org.w3c.dom.*

class DndSpellModal(
    private val character: Character,
    private val existing: DndSpell?
) : Modal(
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
        originLevelSel = labeledSelect(row1, t("magic.originLevel"), DndSpell.originLevels, existing?.originLevel ?: "1") {
            if (it == "Learned by Scroll") "📜 ${tOriginLevel(it)}" else tOriginLevel(it)
        }
        form.appendChild(row1)

        // Row 2: Circle, School, Prepared
        val row2 = document.createElement("div") as HTMLDivElement
        row2.className = "spell-modal__row3"
        circleSel = labeledSelect(row2, t("magic.circle"), DndSpell.circles, existing?.circle ?: "Cantrip") { tCircle(it) }
        fun schoolIcon(s: String) = when (s) {
            "Abjuration"   -> "🛡️"
            "Conjuration"  -> "✨"
            "Divination"   -> "🔮"
            "Enchantment"  -> "💫"
            "Evocation"    -> "🔥"
            "Illusion"     -> "🌀"
            "Necromancy"   -> "☠️"
            "Transmutation"-> "⚙️"
            else -> ""
        }
        schoolSel = labeledSelect(row2, t("magic.school"), DndSpell.schools, existing?.school ?: DndSpell.schools.first()) { "${schoolIcon(it)} ${tSchool(it)}" }

        val prepCol = document.createElement("div") as HTMLDivElement
        prepCol.className = "spell-modal__cb-col"
        preparedCb = document.createElement("input") as HTMLInputElement
        preparedCb.type = "checkbox"; preparedCb.checked = existing?.isPrepared ?: false
        val prepLbl = document.createElement("label") as HTMLLabelElement
        prepLbl.appendChild(preparedCb); prepLbl.append(t("magic.prepared"))
        prepCol.appendChild(prepLbl)
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

        // Row 3: Casting Time, Ritual, Range, Concentration, Duration
        val row3 = document.createElement("div") as HTMLDivElement
        row3.className = "spell-modal__row5"
        castingTimeInput = labeledInput(row3, t("magic.castingTime"), existing?.castingTime ?: "")

        val ritualCol = document.createElement("div") as HTMLDivElement
        ritualCol.className = "spell-modal__cb-col"
        ritualCb = document.createElement("input") as HTMLInputElement
        ritualCb.type = "checkbox"; ritualCb.checked = existing?.canBeRitual ?: false
        val ritualLbl = document.createElement("label") as HTMLLabelElement
        ritualLbl.appendChild(ritualCb); ritualLbl.append(t("magic.ritual"))
        ritualCol.appendChild(ritualLbl)
        row3.appendChild(ritualCol)

        val rangeCol = document.createElement("div") as HTMLDivElement
        rangeCol.className = "spell-modal__range-col"
        val rangeLbl = document.createElement("label") as HTMLLabelElement
        rangeLbl.textContent = t("magic.range")
        rangeCol.appendChild(rangeLbl)
        val rangeInputs = document.createElement("div") as HTMLDivElement
        rangeInputs.className = "spell-modal__range-inputs"
        rangeInput = document.createElement("input") as HTMLInputElement
        rangeInput.type = "text"
        rangeInput.value = existing?.range ?: ""
        rangeInputs.appendChild(rangeInput)
        val personalBtn = document.createElement("button") as HTMLButtonElement
        personalBtn.textContent = "🧍"
        personalBtn.type = "button"
        personalBtn.title = t("magic.personal")
        personalBtn.addEventListener("click", { rangeInput.value = t("magic.personal") })
        rangeInputs.appendChild(personalBtn)
        val touchBtn = document.createElement("button") as HTMLButtonElement
        touchBtn.textContent = "👆"
        touchBtn.type = "button"
        touchBtn.title = t("magic.touch")
        touchBtn.addEventListener("click", { rangeInput.value = t("magic.touch") })
        rangeInputs.appendChild(touchBtn)
        rangeCol.appendChild(rangeInputs)
        row3.appendChild(rangeCol)

        form.appendChild(row3)

        // Row 4: V, S, M, Material Components, Concentration, Duration
        val row4 = document.createElement("div") as HTMLDivElement
        row4.className = "spell-modal__components"

        verbalCb = inlineCb(row4, "V", existing?.hasVerbal ?: false)
        somaticCb = inlineCb(row4, "S", existing?.hasSomatic ?: false)
        materialCb = inlineCb(row4, "M", existing?.hasMaterial ?: false)

        materialInput = document.createElement("input") as HTMLInputElement
        materialInput.value = existing?.materialComponents ?: ""
        materialInput.placeholder = t("magic.materialComponents")
        materialInput.disabled = existing?.hasMaterial != true
        row4.appendChild(materialInput)
        materialCb.addEventListener("change", { materialInput.disabled = !materialCb.checked; if (!materialCb.checked) materialInput.value = "" })

        val concCol = document.createElement("div") as HTMLDivElement
        concCol.className = "spell-modal__cb-col"
        concentrationCb = document.createElement("input") as HTMLInputElement
        concentrationCb.type = "checkbox"; concentrationCb.checked = existing?.needsConcentration ?: false
        val concLbl = document.createElement("label") as HTMLLabelElement
        concLbl.appendChild(concentrationCb); concLbl.append(t("magic.concentration"))
        concCol.appendChild(concLbl)
        row4.appendChild(concCol)

        val durationCol = document.createElement("div") as HTMLDivElement
        val durationLbl = document.createElement("label") as HTMLLabelElement
        durationLbl.textContent = t("magic.duration")
        durationCol.appendChild(durationLbl)
        durationInput = document.createElement("input") as HTMLInputElement
        durationInput.value = existing?.duration ?: ""
        durationCol.appendChild(durationInput)
        row4.appendChild(durationCol)
        form.appendChild(row4)

        // Description
        descInput = addTextarea(form, t("magic.description"), existing?.description ?: "", rows = 4, classes = arrayOf("modal__full-width")).input

        // Higher Circles
        higherInput = addTextarea(form, t("magic.higherShort"), existing?.higherCircles ?: "", rows = 2, classes = arrayOf("modal__full-width")).input

        // Attack row: checkbox + inline details
        val attackRow = document.createElement("div") as HTMLDivElement
        attackRow.className = "spell-modal__inline-row"
        attackCb = inlineCb(attackRow, t("magic.isAttack"), existing?.isAttack ?: false)
        val atkDetailsRow = document.createElement("div") as HTMLDivElement
        atkDetailsRow.className = "spell-modal__inline-details"
        diceInput = labeledInput(atkDetailsRow, t("inv.damageDice"), existing?.attackDamageDice ?: "")
        dmgTypeSel = labeledSelect(atkDetailsRow, t("inv.damageType"), listOf("") + DndSpell.damageTypes, existing?.attackDamageType ?: "") { if (it.isEmpty()) "-" else tSpellDamageType(it) }
        fun setAtkDisabled(disabled: Boolean) { diceInput.disabled = disabled; dmgTypeSel.disabled = disabled; if (disabled) { diceInput.value = ""; dmgTypeSel.value = "" } }
        setAtkDisabled(existing?.isAttack != true)
        attackRow.appendChild(atkDetailsRow)
        form.appendChild(attackRow)

        // Save row: checkbox + inline details
        val saveRow = document.createElement("div") as HTMLDivElement
        saveRow.className = "spell-modal__inline-row"
        savingThrowCb = inlineCb(saveRow, t("magic.needsSavingThrow"), existing?.needsSavingThrow ?: false)
        val saveDetailsRow = document.createElement("div") as HTMLDivElement
        saveDetailsRow.className = "spell-modal__inline-details"
        saveSel = labeledSelect(saveDetailsRow, t("magic.saveAbility"), listOf("") + DndSpell.savingThrowAbilities, existing?.savingThrowAbility ?: "") { if (it.isEmpty()) "-" else tStat(it) }
        fun setSaveDisabled(disabled: Boolean) { saveSel.disabled = disabled; if (disabled) saveSel.value = "" }
        setSaveDisabled(existing?.needsSavingThrow != true)
        saveRow.appendChild(saveDetailsRow)
        form.appendChild(saveRow)

        attackCb.addEventListener("change", { setAtkDisabled(!attackCb.checked) })
        savingThrowCb.addEventListener("change", { setSaveDisabled(!savingThrowCb.checked) })

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
            materialComponents = if (materialCb.checked) materialInput.value else "",
            description = descInput.value,
            higherCircles = higherInput.value,
            isAttack = attackCb.checked,
            needsSavingThrow = savingThrowCb.checked,
            attackType = if (attackCb.checked) "Roll for Attack" else "",
            savingThrowAbility = if (savingThrowCb.checked) saveSel.value else "",
            attackDamageDice = if (attackCb.checked) diceInput.value else "",
            attackDamageType = if (attackCb.checked) dmgTypeSel.value else "",
            isPrepared = preparedCb.checked,
            tags = tagsField.getTags()
        )
        Repos.spell.save(spell)
        close()
    }
}

