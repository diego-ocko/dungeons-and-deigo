package com.dungeonsanddeigo.web.dnd.modals.inventoryItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndInventoryItem
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import kotlinx.browser.document
import org.w3c.dom.*

class DndInventoryItemModal(
    private val character: Character,
    private val category: String,
    private val existing: DndInventoryItem?
) : DndModal(
    title = if (existing != null) t("inv.editItem") else t("inv.addItem"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var descInput: HTMLTextAreaElement
    private lateinit var weightInput: HTMLInputElement
    private lateinit var priceInput: HTMLInputElement
    private lateinit var currSelect: HTMLSelectElement
    private val selectedTags = mutableListOf<String>()

    init {
        if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)
    }

    override fun buildForm(form: HTMLDivElement) {
        nameInput = addInput(form, t("label.name"), existing?.name ?: "", classes = arrayOf("modal__full-width")).input
        descInput = addTextarea(form, t("label.description"), existing?.description ?: "", rows = 3, classes = arrayOf("modal__full-width")).input
        weightInput = addNumberInput(form, t("inv.weight"), (existing?.weight ?: 0.0).toString()).input
        weightInput.step = "0.1"

        // Price row
        val currencies = listOf("pc", "ps", "pe", "pg", "pp").map { it to tCurrency(it) }
        priceInput = addNumberInput(form, t("inv.price"), (existing?.price ?: 0).toString()).input
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
        val item = DndInventoryItem(
            id = existing?.id ?: 0,
            characterId = character.id,
            category = category,
            name = nameInput.value,
            description = descInput.value,
            quantity = 1,
            equipped = false,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            tags = selectedTags.toList()
        )
        Repos.inventory.save(item)
        close()
    }
}
