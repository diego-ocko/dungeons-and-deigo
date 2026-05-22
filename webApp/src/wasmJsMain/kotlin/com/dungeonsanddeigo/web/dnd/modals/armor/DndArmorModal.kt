package com.dungeonsanddeigo.web.dnd.modals.armor

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tArmorType
import com.dungeonsanddeigo.i18n.tAcModifier
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showArmorModal(
    character: Character,
    existing: DndArmor?,
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
    titleEl.textContent = if (existing != null) t("inv.editArmor") else t("inv.addArmor")
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
    DndArmor.types.forEach { tp ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = tp; o.textContent = tArmorType(tp); typeSelect.appendChild(o)
    }
    typeSelect.value = existing?.type ?: DndArmor.types.first()
    form.appendChild(typeSelect)

    // Base AC
    val acLbl = document.createElement("label") as HTMLLabelElement
    acLbl.textContent = t("inv.baseAC"); acLbl.style.fontWeight = "bold"
    form.appendChild(acLbl)
    val acInput = document.createElement("input") as HTMLInputElement
    acInput.type = "number"; acInput.value = (existing?.baseAC ?: 10).toString()
    form.appendChild(acInput)

    // AC Modifier
    // AC Modifier
    val acModLbl = document.createElement("label") as HTMLLabelElement
    acModLbl.textContent = t("inv.acModifier"); acModLbl.style.fontWeight = "bold"
    form.appendChild(acModLbl)
    val acModSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.acModifiers.forEach { m ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = m; o.textContent = tAcModifier(m); acModSelect.appendChild(o)
    }
    acModSelect.value = existing?.acModifier ?: "none"
    form.appendChild(acModSelect)

    // Minimum Strength
    val minStrLbl = document.createElement("label") as HTMLLabelElement
    minStrLbl.textContent = t("inv.minStrength"); minStrLbl.style.fontWeight = "bold"
    form.appendChild(minStrLbl)
    val minStrInput = document.createElement("input") as HTMLInputElement
    minStrInput.type = "number"; minStrInput.min = "0"; minStrInput.max = "20"
    minStrInput.value = (existing?.minimumStrength ?: 0).toString()
    form.appendChild(minStrInput)

    // Sneak Disadvantage
    val sneakEmptyLbl = document.createElement("label") as HTMLLabelElement
    sneakEmptyLbl.textContent = ""
    form.appendChild(sneakEmptyLbl)
    val sneakLbl = document.createElement("label") as HTMLLabelElement
    val sneakCb = document.createElement("input") as HTMLInputElement
    sneakCb.type = "checkbox"; sneakCb.checked = existing?.hasSneakDisadvantage ?: false
    sneakCb.style.marginRight = "6px"
    sneakLbl.appendChild(sneakCb); sneakLbl.append(t("inv.sneakDisadv"))
    form.appendChild(sneakLbl)

    // Visibility logic based on type
    fun updateArmorFieldVisibility() {
        val isShield = typeSelect.value == "Shield"
        val isClothes = typeSelect.value == "Clothes"
        val isHeavy = typeSelect.value == "Heavy Armor"
        val hideAC = isShield || isClothes
        acLbl.style.display = if (hideAC) "none" else ""
        acInput.style.display = if (hideAC) "none" else ""
        acModLbl.style.display = if (hideAC) "none" else ""
        acModSelect.style.display = if (hideAC) "none" else ""
        minStrLbl.style.display = if (!isHeavy) "none" else ""
        minStrInput.style.display = if (!isHeavy) "none" else ""
        sneakEmptyLbl.style.display = if (isShield || isClothes) "none" else ""
        sneakLbl.style.display = if (isShield || isClothes) "none" else ""
    }
    updateArmorFieldVisibility()
    typeSelect.addEventListener("change", { updateArmorFieldVisibility() })

    // Weight
    lbl(t("inv.weight"))
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

    // Price
    lbl(t("inv.price"))
    val priceRow = document.createElement("div") as HTMLDivElement
    priceRow.style.display = "flex"; priceRow.style.setProperty("gap", "4px")
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceInput.style.width = "70%"
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.currencies.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = tCurrency(c); currSelect.appendChild(o)
    }
    currSelect.value = existing?.priceCurrency ?: "pg"
    priceRow.appendChild(currSelect)
    form.appendChild(priceRow)

    // Additional Features
    val addFeatLbl = document.createElement("label") as HTMLLabelElement
    addFeatLbl.textContent = t("features.additionalFeatures"); addFeatLbl.style.fontWeight = "bold"
    addFeatLbl.style.setProperty("grid-column", "1 / -1")
    form.appendChild(addFeatLbl)
    val addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
    addFeatInput.value = existing?.additionalFeatures ?: ""
    addFeatInput.rows = 3; addFeatInput.style.width = "100%"
    addFeatInput.style.setProperty("grid-column", "1 / -1")
    form.appendChild(addFeatInput)

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

    fun refreshArmorTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshArmorTagBadges() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshArmorTagBadges()
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
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshArmorTagBadges() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)
    form.appendChild(tagsContainer)

    // Equipped (last field)
    val eqContainer = document.createElement("div") as HTMLDivElement
    eqContainer.style.setProperty("grid-column", "1 / -1")
    val eqLbl = document.createElement("label") as HTMLLabelElement
    val eqCb = document.createElement("input") as HTMLInputElement
    eqCb.type = "checkbox"; eqCb.checked = existing?.isEquipped ?: false
    eqCb.style.marginRight = "6px"
    eqLbl.appendChild(eqCb); eqLbl.append(t("features.equipped"))
    eqLbl.style.fontWeight = "bold"
    eqContainer.appendChild(eqLbl)
    form.appendChild(eqContainer)

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
        val armor = DndArmor(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            type = typeSelect.value,
            baseAC = acInput.value.toIntOrNull() ?: 10,
            acModifier = acModSelect.value,
            minimumStrength = minStrInput.value.toIntOrNull() ?: 0,
            hasSneakDisadvantage = sneakCb.checked,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            isEquipped = eqCb.checked,
            tags = selectedTags.toList(),
            additionalFeatures = addFeatInput.value
        )
        // Unequip other items in same slot if equipping this one
        // Slots: Armor (Light/Medium/Heavy), Shield, Clothes
        if (armor.isEquipped) {
            val allArmors = Repos.armor.getByCharacterId(character.id)
            val slot = when (armor.type) {
                "Shield" -> "Shield"
                "Clothes" -> "Clothes"
                else -> "Armor"
            }
            allArmors.forEach { other ->
                if (other.id != armor.id && other.isEquipped) {
                    val otherSlot = when (other.type) {
                        "Shield" -> "Shield"
                        "Clothes" -> "Clothes"
                        else -> "Armor"
                    }
                    if (slot == otherSlot) {
                        Repos.armor.save(other.copy(isEquipped = false))
                    }
                }
            }
        }
        Repos.armor.save(armor)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

