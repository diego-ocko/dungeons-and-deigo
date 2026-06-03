package com.dungeonsanddeigo.web.dnd.modals.magicItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndMagicItem
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import kotlinx.browser.document
import org.w3c.dom.*

class DndMagicItemModal(
    private val character: Character,
    private val existing: DndMagicItem?
) : DndModal(
    title = if (existing != null) t("inv.editMagicItem") else t("inv.addMagicItem"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var needSynchCb: HTMLInputElement
    private lateinit var isSynchedCb: HTMLInputElement
    private lateinit var isSynchedLbl: HTMLLabelElement
    private lateinit var effectInput: HTMLTextAreaElement
    private lateinit var weightInput: HTMLInputElement
    private lateinit var priceInput: HTMLInputElement
    private lateinit var currSelect: HTMLSelectElement
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        nameInput = addInput(form, t("label.name"), existing?.name ?: "").input

        // Synch checkboxes
        val synchDiv = document.createElement("div") as HTMLDivElement
        synchDiv.className = "modal__checkbox-row modal__full-width"
        val needSynchLbl = document.createElement("label") as HTMLLabelElement
        needSynchCb = document.createElement("input") as HTMLInputElement
        needSynchCb.type = "checkbox"; needSynchCb.checked = existing?.needSynch ?: false
        needSynchLbl.appendChild(needSynchCb); needSynchLbl.append(t("inv.needSynch"))
        synchDiv.appendChild(needSynchLbl)

        isSynchedLbl = document.createElement("label") as HTMLLabelElement
        isSynchedCb = document.createElement("input") as HTMLInputElement
        isSynchedCb.type = "checkbox"; isSynchedCb.checked = existing?.isSynched ?: false
        isSynchedLbl.appendChild(isSynchedCb); isSynchedLbl.append(t("inv.isSynched"))
        synchDiv.appendChild(isSynchedLbl)
        needSynchCb.addEventListener("change", {
            isSynchedLbl.style.display = if (needSynchCb.checked) "" else "none"
        })
        isSynchedLbl.style.display = if (existing?.needSynch == true) "" else "none"
        form.appendChild(synchDiv)

        effectInput = addTextarea(form, t("inv.effect"), existing?.effect ?: "", rows = 3, classes = arrayOf("modal__full-width")).input
        weightInput = addNumberInput(form, t("inv.weight"), (existing?.weight ?: 0.0).toString()).input
        weightInput.step = "0.1"
        priceInput = addNumberInput(form, t("inv.price"), (existing?.price ?: 0).toString()).input

        val currencies = DndMagicItem.currencies.map { it to tCurrency(it) }
        currSelect = addSelect(form, "", currencies, existing?.priceCurrency ?: "pg").input

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())
    }

    override fun onSave(close: () -> Unit) {
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
            tags = tagsField.getTags()
        )
        Repos.magicItem.save(item)
        close()
    }
}
