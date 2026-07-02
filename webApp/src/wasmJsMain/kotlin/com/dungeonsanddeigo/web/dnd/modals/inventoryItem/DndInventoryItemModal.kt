package com.dungeonsanddeigo.web.dnd.modals.inventoryItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndInventoryItem
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import kotlinx.browser.document
import org.w3c.dom.*

class DndInventoryItemModal(
    private val character: Character,
    private val category: String,
    private val existing: DndInventoryItem?
) : Modal(
    title = if (existing != null) t("inv.editItem") else t("inv.addItem"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var descInput: HTMLTextAreaElement
    private lateinit var weightInput: HTMLInputElement
    private lateinit var priceInput: HTMLInputElement
    private lateinit var currSelect: HTMLSelectElement
    private lateinit var tagsField: TagsField

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
        tagsField = TagsField(form, existing?.tags ?: emptyList())
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
            tags = tagsField.getTags()
        )
        Repos.inventory.save(item)
        close()
    }
}
