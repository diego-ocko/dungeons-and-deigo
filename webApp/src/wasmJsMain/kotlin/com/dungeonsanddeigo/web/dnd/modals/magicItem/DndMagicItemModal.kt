package com.dungeonsanddeigo.web.dnd.modals.magicItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndMagicItem
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
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
    private val selectedTags = mutableListOf<String>()

    init {
        if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)
    }

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
        val tagsLbl = document.createElement("label") as HTMLLabelElement
        tagsLbl.textContent = t("label.tags")
        tagsLbl.className = "modal__full-width"
        form.appendChild(tagsLbl)
        val tagBadgesDiv = document.createElement("div") as HTMLDivElement
        tagBadgesDiv.className = "modal__tags"
        fun refreshTags() {
            tagBadgesDiv.innerHTML = ""
            selectedTags.forEach { tag ->
                val badge = document.createElement("span") as HTMLSpanElement
                badge.textContent = "$tag \u00D7"
                badge.className = "modal__tag"
                badge.addEventListener("click", { selectedTags.remove(tag); refreshTags() })
                tagBadgesDiv.appendChild(badge)
            }
        }
        refreshTags()
        form.appendChild(tagBadgesDiv)
        val tagAddRow = document.createElement("div") as HTMLDivElement
        tagAddRow.className = "modal__tag-add-row"
        val tagInput = document.createElement("input") as HTMLInputElement
        tagInput.placeholder = "Add tag..."
        tagAddRow.appendChild(tagInput)
        val tagAddBtn = document.createElement("button") as HTMLButtonElement
        tagAddBtn.textContent = t("btn.add")
        tagAddBtn.addEventListener("click", {
            val tag = tagInput.value.trim()
            if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTags() }
            tagInput.value = ""
        })
        tagAddRow.appendChild(tagAddBtn)
        form.appendChild(tagAddRow)
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
            tags = selectedTags.toList()
        )
        Repos.magicItem.save(item)
        close()
    }
}
