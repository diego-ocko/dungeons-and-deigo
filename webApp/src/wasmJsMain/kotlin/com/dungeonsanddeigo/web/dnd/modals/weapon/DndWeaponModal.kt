package com.dungeonsanddeigo.web.dnd.modals.weapon

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.i18n.tWeapon
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndWeapon
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import kotlinx.browser.document
import org.w3c.dom.*

class DndWeaponModal(
    private val character: Character,
    private val existing: DndWeapon?
) : Modal(
    title = if (existing != null) t("inv.editWeapon") else t("inv.addWeapon"),
    size = "wide"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var catSelect: HTMLSelectElement
    private lateinit var typeSelect: HTMLSelectElement
    private lateinit var customTypeInput: HTMLInputElement
    private lateinit var dmgDiceInput: HTMLInputElement
    private lateinit var dmgTypeSelect: HTMLSelectElement
    private lateinit var ammoCb: HTMLInputElement
    private lateinit var finesseCb: HTMLInputElement
    private lateinit var heavyCb: HTMLInputElement
    private lateinit var lightCb: HTMLInputElement
    private lateinit var loadingCb: HTMLInputElement
    private lateinit var rangeCb: HTMLInputElement
    private lateinit var reachCb: HTMLInputElement
    private lateinit var specialCb: HTMLInputElement
    private lateinit var thrownCb: HTMLInputElement
    private lateinit var twoHandedCb: HTMLInputElement
    private lateinit var versatileCb: HTMLInputElement
    private lateinit var silverCb: HTMLInputElement
    private lateinit var rangeInput: HTMLInputElement
    private lateinit var rangeLongInput: HTMLInputElement
    private lateinit var specialInput: HTMLTextAreaElement
    private lateinit var versatileInput: HTMLInputElement
    private lateinit var addFeatInput: HTMLTextAreaElement
    private lateinit var weightInput: HTMLInputElement
    private lateinit var priceInput: HTMLInputElement
    private lateinit var currSelect: HTMLSelectElement
    private lateinit var eqCb: HTMLInputElement
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        form.classList.add("weapon-modal")
        fun lbl(text: String) {
            val l = document.createElement("label") as HTMLLabelElement
            l.textContent = text
            form.appendChild(l)
        }

        // Name
        lbl(t("label.name"))
        nameInput = document.createElement("input") as HTMLInputElement
        nameInput.value = existing?.name ?: ""
        form.appendChild(nameInput)

        // Category
        lbl(t("inv.category"))
        catSelect = document.createElement("select") as HTMLSelectElement
        DndWeapon.categories.forEach { c ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = c; o.textContent = when(c) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); "Others" -> t("features.weapon.others"); else -> c }; catSelect.appendChild(o)
        }
        catSelect.value = existing?.category ?: DndWeapon.categories.first()
        form.appendChild(catSelect)

        // Weapon Type
        lbl(t("inv.weaponType"))
        typeSelect = document.createElement("select") as HTMLSelectElement
        val emptyTypeOpt = document.createElement("option") as HTMLOptionElement
        emptyTypeOpt.value = ""; emptyTypeOpt.textContent = t("general.select")
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

        // Custom weapon type
        customTypeInput = document.createElement("input") as HTMLInputElement
        customTypeInput.placeholder = "Custom weapon type"
        customTypeInput.className = "modal__full-width modal__custom-input"
        typeSelect.addEventListener("change", {
            customTypeInput.style.display = if (typeSelect.value == "__custom__") "block" else "none"
        })
        form.appendChild(customTypeInput)

        // Damage Dice
        lbl(t("inv.damageDice"))
        dmgDiceInput = document.createElement("input") as HTMLInputElement
        dmgDiceInput.value = existing?.damageDice ?: ""
        dmgDiceInput.placeholder = "e.g. 1d8"
        form.appendChild(dmgDiceInput)

        // Damage Type
        lbl(t("inv.damageType"))
        dmgTypeSelect = document.createElement("select") as HTMLSelectElement
        DndWeapon.damageTypes.forEach { d ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = d; o.textContent = when(d) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> d }; dmgTypeSelect.appendChild(o)
        }
        dmgTypeSelect.value = existing?.damageType ?: DndWeapon.damageTypes.first()
        form.appendChild(dmgTypeSelect)

        // Properties
        val propsGrid = document.createElement("div") as HTMLDivElement
        propsGrid.className = "weapon-modal__props-grid modal__full-width"
        fun addCb(label: String, checked: Boolean): HTMLInputElement {
            val l = document.createElement("label") as HTMLLabelElement
            val cb = document.createElement("input") as HTMLInputElement
            cb.type = "checkbox"; cb.checked = checked
            l.appendChild(cb); l.append(label)
            propsGrid.appendChild(l)
            return cb
        }
        ammoCb = addCb(t("inv.ammunition"), existing?.ammunition ?: false)
        finesseCb = addCb(t("inv.finesse"), existing?.finesse ?: false)
        heavyCb = addCb(t("inv.heavy"), existing?.heavy ?: false)
        lightCb = addCb(t("inv.light"), existing?.light ?: false)
        loadingCb = addCb(t("inv.loading"), existing?.loading ?: false)
        rangeCb = addCb(t("inv.range"), existing?.range ?: false)
        reachCb = addCb(t("inv.reach"), existing?.reach ?: false)
        specialCb = addCb(t("inv.special"), existing?.special ?: false)
        thrownCb = addCb(t("inv.thrown"), existing?.thrown ?: false)
        twoHandedCb = addCb(t("inv.twoHanded"), existing?.twoHanded ?: false)
        versatileCb = addCb(t("inv.versatile"), existing?.versatile ?: false)
        silverCb = addCb(t("inv.silver"), existing?.silver ?: false)
        form.appendChild(propsGrid)

        // Range fields
        val rangeDiv = document.createElement("div") as HTMLDivElement
        rangeDiv.className = "weapon-modal__range-grid modal__full-width"
        val rangeLbl1 = document.createElement("label") as HTMLLabelElement
        rangeLbl1.textContent = t("inv.rangeMeter")
        rangeDiv.appendChild(rangeLbl1)
        val rangeLbl2 = document.createElement("label") as HTMLLabelElement
        rangeLbl2.textContent = t("inv.longRange")
        rangeDiv.appendChild(rangeLbl2)
        rangeInput = document.createElement("input") as HTMLInputElement
        rangeInput.type = "number"; rangeInput.min = "0"
        rangeInput.value = (existing?.rangeDistance ?: 0).toString()
        rangeDiv.appendChild(rangeInput)
        rangeLongInput = document.createElement("input") as HTMLInputElement
        rangeLongInput.type = "number"; rangeLongInput.min = "0"
        rangeLongInput.value = (existing?.rangeLongDistance ?: 0).toString()
        rangeDiv.appendChild(rangeLongInput)
        fun updateRangeVisibility() { rangeDiv.style.display = if (rangeCb.checked || thrownCb.checked) "grid" else "none" }
        updateRangeVisibility()
        rangeCb.addEventListener("change", { updateRangeVisibility() })
        thrownCb.addEventListener("change", { updateRangeVisibility() })
        form.appendChild(rangeDiv)

        // Special description
        val specialDiv = document.createElement("div") as HTMLDivElement
        specialDiv.className = "modal__full-width"
        val specialLbl = document.createElement("label") as HTMLLabelElement
        specialLbl.textContent = t("inv.special")
        specialDiv.appendChild(specialLbl)
        specialInput = document.createElement("textarea") as HTMLTextAreaElement
        specialInput.value = existing?.specialDescription ?: ""
        specialDiv.appendChild(specialInput)
        fun updateSpecialVisibility() { specialDiv.style.display = if (specialCb.checked) "block" else "none" }
        updateSpecialVisibility()
        specialCb.addEventListener("change", { updateSpecialVisibility() })
        form.appendChild(specialDiv)

        // Versatile dice
        val versatileDiv = document.createElement("div") as HTMLDivElement
        versatileDiv.className = "modal__full-width"
        val versatileLbl = document.createElement("label") as HTMLLabelElement
        versatileLbl.textContent = t("inv.twoHandedDice")
        versatileDiv.appendChild(versatileLbl)
        versatileInput = document.createElement("input") as HTMLInputElement
        versatileInput.value = existing?.versatileDice ?: ""
        versatileInput.placeholder = "e.g. 1d10"
        versatileDiv.appendChild(versatileInput)
        fun updateVersatileVisibility() { versatileDiv.style.display = if (versatileCb.checked) "block" else "none" }
        updateVersatileVisibility()
        versatileCb.addEventListener("change", { updateVersatileVisibility() })
        form.appendChild(versatileDiv)

        // Additional Features
        val addFeatLbl = document.createElement("label") as HTMLLabelElement
        addFeatLbl.textContent = t("inv.additionalFeatures")
        addFeatLbl.className = "modal__full-width"
        form.appendChild(addFeatLbl)
        addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
        addFeatInput.value = existing?.additionalFeatures ?: ""
        addFeatInput.className = "modal__full-width"
        form.appendChild(addFeatInput)

        // Weight
        lbl(t("inv.weight"))
        val weightRow = document.createElement("div") as HTMLDivElement
        weightRow.className = "modal__checkbox-row"
        weightInput = document.createElement("input") as HTMLInputElement
        weightInput.type = "number"; weightInput.step = "0.1"; weightInput.min = "0"
        weightInput.value = (existing?.weight ?: 0.0).toString()
        weightRow.appendChild(weightInput)
        val kgSpan = document.createElement("span") as HTMLSpanElement
        kgSpan.textContent = "Kg"
        weightRow.appendChild(kgSpan)
        form.appendChild(weightRow)

        // Price
        lbl(t("inv.price"))
        val priceRow = document.createElement("div") as HTMLDivElement
        priceRow.className = "modal__checkbox-row"
        priceInput = document.createElement("input") as HTMLInputElement
        priceInput.type = "number"; priceInput.min = "0"
        priceInput.value = (existing?.price ?: 0).toString()
        priceRow.appendChild(priceInput)
        currSelect = document.createElement("select") as HTMLSelectElement
        DndWeapon.currencies.forEach { c ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = c; o.textContent = tCurrency(c); currSelect.appendChild(o)
        }
        currSelect.value = existing?.priceCurrency ?: "pg"
        priceRow.appendChild(currSelect)
        form.appendChild(priceRow)

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())

        // Equipped
        val eqContainer = document.createElement("div") as HTMLDivElement
        eqContainer.className = "modal__full-width"
        val eqLbl = document.createElement("label") as HTMLLabelElement
        eqCb = document.createElement("input") as HTMLInputElement
        eqCb.type = "checkbox"; eqCb.checked = existing?.isEquipped ?: false
        eqLbl.appendChild(eqCb); eqLbl.append(t("features.equipped"))
        eqContainer.appendChild(eqLbl)
        form.appendChild(eqContainer)
    }

    override fun onSave(close: () -> Unit) {
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
            tags = tagsField.getTags(),
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
        close()
    }
}

