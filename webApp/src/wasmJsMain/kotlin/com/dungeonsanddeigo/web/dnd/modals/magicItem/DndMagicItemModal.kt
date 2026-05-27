package com.dungeonsanddeigo.web.dnd.modals.magicItem

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndMagicItem
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showMagicItemModal(
    character: Character,
    existing: DndMagicItem?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal modal--lg"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) t("inv.editMagicItem") else t("inv.addMagicItem")
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.className = "modal__form"

    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        form.appendChild(l)
    }

    // Name
    lbl(t("label.name"))
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    form.appendChild(nameInput)

    // Need Synch + Is Synched
    val synchDiv = document.createElement("div") as HTMLDivElement
    synchDiv.className = "modal__full-width"
    synchDiv.className = "modal__checkbox-row"

    val needSynchLbl = document.createElement("label") as HTMLLabelElement
    val needSynchCb = document.createElement("input") as HTMLInputElement
    needSynchCb.type = "checkbox"; needSynchCb.checked = existing?.needSynch ?: false
    needSynchLbl.appendChild(needSynchCb); needSynchLbl.append(t("inv.needSynch"))
    synchDiv.appendChild(needSynchLbl)

    val isSynchedLbl = document.createElement("label") as HTMLLabelElement
    val isSynchedCb = document.createElement("input") as HTMLInputElement
    isSynchedCb.type = "checkbox"; isSynchedCb.checked = existing?.isSynched ?: false
    isSynchedLbl.appendChild(isSynchedCb); isSynchedLbl.append(t("inv.isSynched"))
    synchDiv.appendChild(isSynchedLbl)

    fun updateSynchedVisibility() {
        isSynchedLbl.style.display = if (needSynchCb.checked) "" else "none"
    }
    updateSynchedVisibility()
    needSynchCb.addEventListener("change", { updateSynchedVisibility() })
    form.appendChild(synchDiv)

    // Effect
    val effectLbl = document.createElement("label") as HTMLLabelElement
    effectLbl.className = "modal__full-width"
    form.appendChild(effectLbl)
    val effectInput = document.createElement("textarea") as HTMLTextAreaElement
    effectInput.value = existing?.effect ?: ""
    effectInput.className = "modal__full-width"
    form.appendChild(effectInput)

    // Weight
    lbl(t("inv.weight"))
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.className = "modal__checkbox-row"
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightRow.appendChild(weightInput)
    val kgLabel = document.createElement("span") as HTMLSpanElement
    weightRow.appendChild(kgLabel)
    form.appendChild(weightRow)

    // Price
    lbl(t("inv.price"))
    val priceRow = document.createElement("div") as HTMLDivElement
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndMagicItem.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = tCurrency(c); currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.className = "modal__full-width"
    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsContainer.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.className = "modal__tags"

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.className = "modal__tag"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.className = "modal__tag-add-row"
    val tagInput = document.createElement("input") as HTMLInputElement
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
    btnRow.className = "modal__buttons"
    

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = t("btn.save")
    saveBtn.addEventListener("click", {
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
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

