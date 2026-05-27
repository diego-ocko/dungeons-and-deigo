package com.dungeonsanddeigo.web.dnd.modals.weapon

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.i18n.tWeapon
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndWeapon
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showWeaponModal(
    character: Character,
    existing: DndWeapon?,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal modal--wide"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) t("inv.editWeapon") else t("inv.addWeapon")
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

    // Category
    lbl(t("inv.category"))
    val catSelect = document.createElement("select") as HTMLSelectElement
    DndWeapon.categories.forEach { c ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = c; o.textContent = when(c) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); "Others" -> t("features.weapon.others"); else -> c }; catSelect.appendChild(o)
    }
    catSelect.value = existing?.category ?: DndWeapon.categories.first()
    form.appendChild(catSelect)

    // Weapon Type
    lbl(t("inv.weaponType"))
    val typeSelect = document.createElement("select") as HTMLSelectElement
    val emptyTypeOpt = document.createElement("option") as HTMLOptionElement
    emptyTypeOpt.value = ""; emptyTypeOpt.textContent = "-- Select --"
    typeSelect.appendChild(emptyTypeOpt)
    DndWeapon.weaponTypes.forEach { wp ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = wp; o.textContent = tWeapon(wp); typeSelect.appendChild(o)
    }
    val customTypeOpt = document.createElement("option") as HTMLOptionElement
    customTypeOpt.value = "__custom__"; customTypeOpt.textContent = "Custom..."
    typeSelect.appendChild(customTypeOpt)
    if (existing != null && existing.weaponType.isNotEmpty() && existing.weaponType !in DndWeapon.weaponTypes) {
        val existOpt = document.createElement("option") as HTMLOptionElement
        existOpt.value = existing.weaponType; existOpt.textContent = existing.weaponType
        typeSelect.insertBefore(existOpt, customTypeOpt)
    }
    typeSelect.value = existing?.weaponType ?: ""
    form.appendChild(typeSelect)

    // Custom weapon type input (hidden by default)
    val customTypeInput = document.createElement("input") as HTMLInputElement
    customTypeInput.placeholder = "Custom weapon type"
    customTypeInput.className = "modal__full-width"
    typeSelect.addEventListener("change", {
        customTypeInput.style.display = if (typeSelect.value == "__custom__") "block" else "none"
    })
    form.appendChild(customTypeInput)

    // Damage Dice
    lbl(t("inv.damageDice"))
    val dmgDiceInput = document.createElement("input") as HTMLInputElement
    dmgDiceInput.value = existing?.damageDice ?: ""
    dmgDiceInput.placeholder = "e.g. 1d8"
    form.appendChild(dmgDiceInput)

    // Damage Type
    lbl(t("inv.damageType"))
    val dmgTypeSelect = document.createElement("select") as HTMLSelectElement
    DndWeapon.damageTypes.forEach { d ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = d; o.textContent = when(d) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> d }; dmgTypeSelect.appendChild(o)
    }
    dmgTypeSelect.value = existing?.damageType ?: DndWeapon.damageTypes.first()
    form.appendChild(dmgTypeSelect)

    // Properties checkboxes section
    val propsLbl = document.createElement("label") as HTMLLabelElement
    propsLbl.className = "modal__full-width"
    form.appendChild(propsLbl)

    val propsGrid = document.createElement("div") as HTMLDivElement
    propsGrid.className = "modal__full-width"
    propsGrid.className = "modal__form"

    fun addCheckbox(label: String, checked: Boolean): HTMLInputElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        val cb = document.createElement("input") as HTMLInputElement
        cb.type = "checkbox"; cb.checked = checked
        lbl.appendChild(cb); lbl.append(label)
        propsGrid.appendChild(lbl)
        return cb
    }

    val ammoCb = addCheckbox(t("inv.ammunition"), existing?.ammunition ?: false)
    val finesseCb = addCheckbox(t("inv.finesse"), existing?.finesse ?: false)
    val heavyCb = addCheckbox(t("inv.heavy"), existing?.heavy ?: false)
    val lightCb = addCheckbox(t("inv.light"), existing?.light ?: false)
    val loadingCb = addCheckbox(t("inv.loading"), existing?.loading ?: false)
    val rangeCb = addCheckbox(t("inv.range"), existing?.range ?: false)
    val reachCb = addCheckbox(t("inv.reach"), existing?.reach ?: false)
    val specialCb = addCheckbox(t("inv.special"), existing?.special ?: false)
    val thrownCb = addCheckbox(t("inv.thrown"), existing?.thrown ?: false)
    val twoHandedCb = addCheckbox(t("inv.twoHanded"), existing?.twoHanded ?: false)
    val versatileCb = addCheckbox(t("inv.versatile"), existing?.versatile ?: false)
    val silverCb = addCheckbox(t("inv.silver"), existing?.silver ?: false)

    form.appendChild(propsGrid)

    // Range fields (shown when Range is checked)
    val rangeDiv = document.createElement("div") as HTMLDivElement
    rangeDiv.className = "modal__full-width"
    rangeDiv.className = "modal__form"

    val rangeLbl1 = document.createElement("label") as HTMLLabelElement
    rangeDiv.appendChild(rangeLbl1)
    val rangeLbl2 = document.createElement("label") as HTMLLabelElement
    rangeDiv.appendChild(rangeLbl2)
    val rangeInput = document.createElement("input") as HTMLInputElement
    rangeInput.type = "number"; rangeInput.min = "0"
    rangeInput.value = (existing?.rangeDistance ?: 0).toString()
    rangeDiv.appendChild(rangeInput)
    val rangeLongInput = document.createElement("input") as HTMLInputElement
    rangeLongInput.type = "number"; rangeLongInput.min = "0"
    rangeLongInput.value = (existing?.rangeLongDistance ?: 0).toString()
    rangeDiv.appendChild(rangeLongInput)

    fun updateRangeVisibility() {
        rangeDiv.style.display = if (rangeCb.checked || thrownCb.checked) "grid" else "none"
    }
    updateRangeVisibility()
    rangeCb.addEventListener("change", { updateRangeVisibility() })
    thrownCb.addEventListener("change", { updateRangeVisibility() })
    form.appendChild(rangeDiv)

    // Special description (shown when Special is checked)
    val specialDiv = document.createElement("div") as HTMLDivElement
    specialDiv.className = "modal__full-width"
    val specialLbl = document.createElement("label") as HTMLLabelElement
    specialDiv.appendChild(specialLbl)
    val specialInput = document.createElement("textarea") as HTMLTextAreaElement
    specialInput.value = existing?.specialDescription ?: ""
    specialDiv.appendChild(specialInput)

    fun updateSpecialVisibility() {
        specialDiv.style.display = if (specialCb.checked) "block" else "none"
    }
    updateSpecialVisibility()
    specialCb.addEventListener("change", { updateSpecialVisibility() })
    form.appendChild(specialDiv)

    // Versatile dice (shown when Versatile is checked)
    val versatileDiv = document.createElement("div") as HTMLDivElement
    versatileDiv.className = "modal__full-width"
    val versatileLbl = document.createElement("label") as HTMLLabelElement
    versatileDiv.appendChild(versatileLbl)
    val versatileInput = document.createElement("input") as HTMLInputElement
    versatileInput.value = existing?.versatileDice ?: ""
    versatileInput.placeholder = "e.g. 1d10"
    versatileDiv.appendChild(versatileInput)

    fun updateVersatileVisibility() {
        versatileDiv.style.display = if (versatileCb.checked) "block" else "none"
    }
    updateVersatileVisibility()
    versatileCb.addEventListener("change", { updateVersatileVisibility() })
    form.appendChild(versatileDiv)

    // Additional Features
    val addFeatLbl = document.createElement("label") as HTMLLabelElement
    addFeatLbl.className = "modal__full-width"
    form.appendChild(addFeatLbl)
    val addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
    addFeatInput.value = existing?.additionalFeatures ?: ""
    addFeatInput.className = "modal__full-width"
    form.appendChild(addFeatInput)

    // Weight
    lbl(t("inv.weight"))
    val weightRow = document.createElement("div") as HTMLDivElement
    weightRow.className = "modal__checkbox-row"
    val weightInput = document.createElement("input") as HTMLInputElement
    weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
    weightInput.value = (existing?.weight ?: 0.0).toString()
    weightRow.appendChild(weightInput)
    val kgSpan = document.createElement("span") as HTMLSpanElement
    weightRow.appendChild(kgSpan)
    form.appendChild(weightRow)

    // Price
    lbl(t("inv.price"))
    val priceRow = document.createElement("div") as HTMLDivElement
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
    priceRow.appendChild(priceInput)
    val currSelect = document.createElement("select") as HTMLSelectElement
    DndWeapon.currencies.forEach { c ->
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

    // Equipped
    val eqContainer = document.createElement("div") as HTMLDivElement
    eqContainer.className = "modal__full-width"
    val eqLbl = document.createElement("label") as HTMLLabelElement
    val eqCb = document.createElement("input") as HTMLInputElement
    eqCb.type = "checkbox"; eqCb.checked = existing?.isEquipped ?: false
    eqLbl.appendChild(eqCb); eqLbl.append(t("features.equipped"))
    eqContainer.appendChild(eqLbl)
    form.appendChild(eqContainer)

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
        val weaponType = if (typeSelect.value == "__custom__") customTypeInput.value.trim() else typeSelect.value
        val weapon = DndWeapon(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            category = catSelect.value,
            weaponType = weaponType,
            damageDice = dmgDiceInput.value,
            damageType = dmgTypeSelect.value,
            ammunition = ammoCb.checked,
            finesse = finesseCb.checked,
            heavy = heavyCb.checked,
            light = lightCb.checked,
            loading = loadingCb.checked,
            range = rangeCb.checked,
            rangeDistance = rangeInput.value.toIntOrNull() ?: 0,
            rangeLongDistance = rangeLongInput.value.toIntOrNull() ?: 0,
            reach = reachCb.checked,
            special = specialCb.checked,
            specialDescription = specialInput.value,
            thrown = thrownCb.checked,
            twoHanded = twoHandedCb.checked,
            versatile = versatileCb.checked,
            versatileDice = versatileInput.value,
            silver = silverCb.checked,
            additionalFeatures = addFeatInput.value,
            weight = weightInput.value.toDoubleOrNull() ?: 0.0,
            price = priceInput.value.toIntOrNull() ?: 0,
            priceCurrency = currSelect.value,
            tags = selectedTags.toList(),
            isEquipped = eqCb.checked
        )
        if (weapon.isEquipped) {
            Repos.weapon.getByCharacterId(character.id).forEach { other ->
                if (other.id != weapon.id && other.isEquipped) {
                    Repos.weapon.save(other.copy(isEquipped = false))
                }
            }
        }
        Repos.weapon.save(weapon)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

