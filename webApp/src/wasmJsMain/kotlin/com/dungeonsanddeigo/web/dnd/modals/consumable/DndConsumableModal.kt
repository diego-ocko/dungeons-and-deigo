package com.dungeonsanddeigo.web.dnd.modals.consumable

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndConsumable
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showConsumableModal(
    character: Character,
    existing: DndConsumable?,
    onSave: () -> Unit
) {
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
    modal.style.maxWidth = "450px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) t("inv.editConsumable") else t("inv.addConsumable")
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("grid-template-columns", "1fr 1fr")
    form.style.setProperty("gap", "10px")

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text; l.style.fontWeight = "bold"
        form.appendChild(l)
    }

    // Name
    lbl(t("label.name"))
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    form.appendChild(nameInput)

    // Type
    lbl(t("inv.type"))
    val typeSelect = document.createElement("select") as HTMLSelectElement
    DndConsumable.types.forEach { tp ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = tp; o.textContent = when(tp) { "Healing Potion" -> t("inv.consumable.healingPotion"); "Magic Potion" -> t("inv.consumable.magicPotion"); "Food" -> t("inv.consumable.food"); "Ammunition" -> t("inv.consumable.ammunition"); "Other" -> t("inv.consumable.other"); else -> tp }; typeSelect.appendChild(o)
    }
    typeSelect.value = existing?.type ?: DndConsumable.types.first()
    form.appendChild(typeSelect)

    // Quantity
    lbl(t("inv.quantity"))
    val qtyInput = document.createElement("input") as HTMLInputElement
    qtyInput.type = "number"; qtyInput.min = "0"
    qtyInput.value = (existing?.quantity ?: 1).toString()
    form.appendChild(qtyInput)

    // Weight per unit
    lbl(t("inv.weightPerUnit"))
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.style.display = "flex"; weightRow.style.alignItems = "center"
    weightRow.style.setProperty("gap", "4px")
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightInput.style.width = "100%"
    weightRow.appendChild(weightInput)
    val kgLabel = document.createElement("span") as HTMLSpanElement
    kgLabel.textContent = "Kg"; kgLabel.style.fontSize = "12px"; kgLabel.style.color = "#666"
    weightRow.appendChild(kgLabel)
    form.appendChild(weightRow)

    // Price per unit
    lbl(t("inv.pricePerUnit"))
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndConsumable.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = tCurrency(c); currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Effect
    val effectLbl = document.createElement("label") as HTMLLabelElement
    effectLbl.textContent = t("inv.effect"); effectLbl.style.fontWeight = "bold"
    effectLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(effectLbl)
    val effectInput = document.createElement("textarea") as HTMLTextAreaElement
    effectInput.value = existing?.effect ?: ""
    effectInput.rows = 3; effectInput.style.width = "100%"
    effectInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(effectInput)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.style.setProperty("grid-column", "1 / -1")
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = t("label.tags"); tagsLbl.style.fontWeight = "bold"
    tagsLbl.style.display = "block"; tagsLbl.style.marginBottom = "6px"
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.marginBottom = "8px"
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    tagAddRow.style.alignItems = "center"
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = t("btn.add")
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

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
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

