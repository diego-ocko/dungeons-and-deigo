package com.dungeonsanddeigo.web.dnd.modals.inventoryItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndInventoryItem
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import com.dungeonsanddeigo.web.dnd.components.weightAndPrice.WeightAndPriceField
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
    private lateinit var weightAndPriceField: WeightAndPriceField
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        nameInput = addInput(form, t("label.name"), existing?.name ?: "", classes = arrayOf("modal__full-width")).input
        descInput = addTextarea(form, t("label.description"), existing?.description ?: "", rows = 3, classes = arrayOf("modal__full-width")).input
        weightAndPriceField = WeightAndPriceField(form)
        weightAndPriceField.setValues(existing?.weight ?: 0.0, existing?.price ?: 0, existing?.priceCurrency ?: "pg")

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())
    }

    override fun onSave(close: () -> Unit) {
        val wp = weightAndPriceField.getValues()
        val item = DndInventoryItem(
            id = existing?.id ?: 0,
            characterId = character.id,
            category = category,
            name = nameInput.value,
            description = descInput.value,
            quantity = 1,
            equipped = false,
            weight = wp.weight,
            price = wp.price,
            priceCurrency = wp.priceCurrency,
            tags = tagsField.getTags()
        )
        Repos.inventory.save(item)
        close()
    }
}
