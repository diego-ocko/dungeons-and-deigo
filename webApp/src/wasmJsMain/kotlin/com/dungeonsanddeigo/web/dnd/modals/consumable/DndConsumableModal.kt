package com.dungeonsanddeigo.web.dnd.modals.consumable

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndConsumable
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import com.dungeonsanddeigo.web.dnd.components.weightAndPrice.WeightAndPriceField
import kotlinx.browser.document
import org.w3c.dom.*

class DndConsumableModal(
    private val character: Character,
    private val existing: DndConsumable?
) : Modal(
    title = if (existing != null) t("inv.editConsumable") else t("inv.addConsumable"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var typeSelect: HTMLSelectElement
    private lateinit var qtyInput: HTMLInputElement
    private lateinit var weightAndPriceField: WeightAndPriceField
    private lateinit var effectInput: HTMLTextAreaElement
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        nameInput = addInput(form, t("label.name"), existing?.name ?: "", classes = arrayOf("modal__full-width")).input

        val typeQtyRow = document.createElement("div") as HTMLDivElement
        typeQtyRow.className = "consumable-modal__type-qty-row"

        val typeCol = document.createElement("div") as HTMLDivElement
        typeCol.className = "consumable-modal__type-col"
        val typeLbl = document.createElement("label") as HTMLLabelElement
        typeLbl.textContent = t("inv.type")
        typeSelect = document.createElement("select") as HTMLSelectElement
        val typeOptions = DndConsumable.types.map { tp ->
            tp to when(tp) { "Healing Potion" -> t("inv.consumable.healingPotion"); "Magic Potion" -> t("inv.consumable.magicPotion"); "Food" -> t("inv.consumable.food"); "Ammunition" -> t("inv.consumable.ammunition"); "Other" -> t("inv.consumable.other"); else -> tp }
        }
        typeOptions.forEach { (value, label) ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = value; opt.textContent = label
            if (value == (existing?.type ?: DndConsumable.types.first())) opt.selected = true
            typeSelect.appendChild(opt)
        }
        typeCol.appendChild(typeLbl); typeCol.appendChild(typeSelect)

        val qtyCol = document.createElement("div") as HTMLDivElement
        qtyCol.className = "consumable-modal__qty-col"
        val qtyLbl = document.createElement("label") as HTMLLabelElement
        qtyLbl.textContent = t("inv.quantity")
        qtyInput = document.createElement("input") as HTMLInputElement
        qtyInput.type = "number"; qtyInput.value = (existing?.quantity ?: 1).toString()
        qtyCol.appendChild(qtyLbl); qtyCol.appendChild(qtyInput)

        typeQtyRow.appendChild(typeCol); typeQtyRow.appendChild(qtyCol)
        form.appendChild(typeQtyRow)
        weightAndPriceField = WeightAndPriceField(form, weightLabel = t("inv.weightPerUnit"), priceLabel = t("inv.pricePerUnit"))
        weightAndPriceField.setValues(existing?.weight ?: 0.0, existing?.price ?: 0, existing?.priceCurrency ?: "pg")

        effectInput = addTextarea(form, t("inv.effect"), existing?.effect ?: "", rows = 3, classes = arrayOf("modal__full-width")).input

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())
    }

    override fun onSave(close: () -> Unit) {
        val wp = weightAndPriceField.getValues()
        val item = DndConsumable(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            type = typeSelect.value,
            quantity = maxOf(0, qtyInput.value.toIntOrNull() ?: 1),
            effect = effectInput.value,
            price = wp.price,
            priceCurrency = wp.priceCurrency,
            weight = wp.weight,
            tags = tagsField.getTags()
        )
        Repos.consumable.save(item)
        close()
    }
}
