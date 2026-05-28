package com.dungeonsanddeigo.web.dnd.modals.consumable

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndConsumable
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import kotlinx.browser.document
import org.w3c.dom.*

class DndConsumableModal(
    private val character: Character,
    private val existing: DndConsumable?
) : DndModal(
    title = if (existing != null) t("inv.editConsumable") else t("inv.addConsumable"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var typeSelect: HTMLSelectElement
    private lateinit var qtyInput: HTMLInputElement
    private lateinit var weightInput: HTMLInputElement
    private lateinit var priceInput: HTMLInputElement
    private lateinit var currSelect: HTMLSelectElement
    private lateinit var effectInput: HTMLTextAreaElement
    private val selectedTags = mutableListOf<String>()

    init {
        if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)
    }

    override fun buildForm(form: HTMLDivElement) {
        nameInput = addInput(form, t("label.name"), existing?.name ?: "").input

        val typeOptions = DndConsumable.types.map { tp ->
            tp to when(tp) { "Healing Potion" -> t("inv.consumable.healingPotion"); "Magic Potion" -> t("inv.consumable.magicPotion"); "Food" -> t("inv.consumable.food"); "Ammunition" -> t("inv.consumable.ammunition"); "Other" -> t("inv.consumable.other"); else -> tp }
        }
        typeSelect = addSelect(form, t("inv.type"), typeOptions, existing?.type ?: DndConsumable.types.first()).input

        qtyInput = addNumberInput(form, t("inv.quantity"), (existing?.quantity ?: 1).toString()).input
        weightInput = addNumberInput(form, t("inv.weightPerUnit"), (existing?.weight ?: 0.0).toString()).input
        weightInput.step = "0.1"
        priceInput = addNumberInput(form, t("inv.pricePerUnit"), (existing?.price ?: 0).toString()).input

        val currencies = DndConsumable.currencies.map { it to tCurrency(it) }
        currSelect = addSelect(form, "", currencies, existing?.priceCurrency ?: "pg").input

        effectInput = addTextarea(form, t("inv.effect"), existing?.effect ?: "", rows = 3, classes = arrayOf("modal__full-width")).input

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
        Repos.consumable.save(item)
        close()
    }
}
