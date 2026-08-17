package com.dungeonsanddeigo.web.dnd.modals.weapon

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tWeapon
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndWeapon
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.damageTypes.DamageTypes
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import com.dungeonsanddeigo.web.dnd.components.weightAndPrice.WeightAndPriceField
import kotlinx.browser.document
import org.w3c.dom.*

class DndWeaponModal(
    private val character: Character,
    private val existing: DndWeapon?
) : Modal(
    title = if (existing != null) t("inv.editWeapon") else t("inv.addWeapon"),
    size = "xl"
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
    private lateinit var specialInput: HTMLInputElement
    private lateinit var versatileInput: HTMLInputElement
    private lateinit var addFeatInput: HTMLTextAreaElement
    private lateinit var weightAndPriceField: WeightAndPriceField
    private lateinit var eqCb: HTMLInputElement
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        form.classList.add("weapon-modal")

        fun col(vararg children: HTMLElement): HTMLDivElement {
            val div = document.createElement("div") as HTMLDivElement
            div.className = "weapon-modal__field-col"
            children.forEach { div.appendChild(it) }
            return div
        }
        fun lbl(text: String): HTMLLabelElement {
            val l = document.createElement("label") as HTMLLabelElement
            l.textContent = text
            return l
        }

        // Name + Category + Weapon Type row
        val nameTypRow = document.createElement("div") as HTMLDivElement
        nameTypRow.className = "weapon-modal__name-type-row modal__full-width"

        nameInput = document.createElement("input") as HTMLInputElement
        nameInput.value = existing?.name ?: ""
        val nameCol = col(lbl(t("label.name")), nameInput)
        nameCol.className = "weapon-modal__field-col weapon-modal__name-col"
        nameTypRow.appendChild(nameCol)

        catSelect = document.createElement("select") as HTMLSelectElement
        DndWeapon.categories.forEach { c ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = c; o.textContent = when (c) {
                "Simple" -> t("features.weapon.simple")
                "Martial" -> t("features.weapon.martial")
                "Others"  -> t("features.weapon.others")
                else -> c
            }; catSelect.appendChild(o)
        }
        catSelect.value = existing?.category ?: DndWeapon.categories.first()
        nameTypRow.appendChild(col(lbl(t("inv.category")), catSelect))

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
        nameTypRow.appendChild(col(lbl(t("inv.weaponType")), typeSelect))

        // Custom weapon type — inline, next to type select
        customTypeInput = document.createElement("input") as HTMLInputElement
        customTypeInput.placeholder = "Custom weapon type"
        val customTypeCol = col(lbl(""), customTypeInput)
        customTypeCol.style.display = "none"
        nameTypRow.appendChild(customTypeCol)
        typeSelect.addEventListener("change", {
            val isCustom = typeSelect.value == "__custom__"
            customTypeCol.style.display = if (isCustom) "flex" else "none"
        })

        form.appendChild(nameTypRow)

        // Damage Dice + Damage Type row
        val dmgRow = document.createElement("div") as HTMLDivElement
        dmgRow.className = "weapon-modal__dmg-row modal__full-width"

        dmgDiceInput = document.createElement("input") as HTMLInputElement
        dmgDiceInput.value = existing?.damageDice ?: ""
        dmgDiceInput.placeholder = "e.g. 1d8"
        dmgRow.appendChild(col(lbl(t("inv.damageDice")), dmgDiceInput))

        dmgTypeSelect = document.createElement("select") as HTMLSelectElement
        DamageTypes.entries().forEach { d ->
            val o = document.createElement("option") as HTMLOptionElement
            o.value = d.value
            o.textContent = if (d.emoji.isNotEmpty()) "${d.emoji} ${d.label}" else d.label
            dmgTypeSelect.appendChild(o)
        }
        dmgTypeSelect.value = existing?.damageType ?: DamageTypes.values.first()
        dmgRow.appendChild(col(lbl(t("inv.damageType")), dmgTypeSelect))

        form.appendChild(dmgRow)

        // Properties
        fun cbRow(cols: String): HTMLDivElement {
            val row = document.createElement("div") as HTMLDivElement
            row.className = "weapon-modal__cb-row modal__full-width"
            row.style.setProperty("grid-template-columns", cols)
            return row
        }
        fun addCb(container: HTMLDivElement, label: String, checked: Boolean): HTMLInputElement {
            val l = document.createElement("label") as HTMLLabelElement
            val cb = document.createElement("input") as HTMLInputElement
            cb.type = "checkbox"; cb.checked = checked
            l.appendChild(cb); l.append(label)
            container.appendChild(l)
            return cb
        }

        // Row 1: Acuidade, Alcance Estendido, Munição, Recarga
        val row1 = cbRow("1fr 1fr 1fr 1fr")
        finesseCb  = addCb(row1, t("inv.finesse"),    existing?.finesse    ?: false)
        reachCb    = addCb(row1, t("inv.reach"),      existing?.reach      ?: false)
        ammoCb     = addCb(row1, t("inv.ammunition"), existing?.ammunition ?: false)
        loadingCb  = addCb(row1, t("inv.loading"),    existing?.loading    ?: false)
        form.appendChild(row1)

        // Row 2: Leve, Pesada, Duas Mãos, Prateada
        val row2 = cbRow("1fr 1fr 1fr 1fr")
        lightCb      = addCb(row2, t("inv.light"),      existing?.light      ?: false)
        heavyCb      = addCb(row2, t("inv.heavy"),      existing?.heavy      ?: false)
        twoHandedCb  = addCb(row2, t("inv.twoHanded"),  existing?.twoHanded  ?: false)
        silverCb     = addCb(row2, t("inv.silver"),     existing?.silver     ?: false)
        form.appendChild(row2)

        // Row 3: Alcance | Arremesso | Alcance normal | Alcance longo
        val row3 = cbRow("1fr 1fr 1fr 1fr")
        rangeCb  = addCb(row3, t("inv.range"),  existing?.range  ?: false)
        thrownCb = addCb(row3, t("inv.thrown"), existing?.thrown ?: false)
        rangeInput = document.createElement("input") as HTMLInputElement
        rangeInput.type = "number"; rangeInput.min = "0"
        rangeInput.value = (existing?.rangeDistance ?: 0).toString()
        row3.appendChild(col(lbl(t("inv.rangeMeter")), rangeInput))
        rangeLongInput = document.createElement("input") as HTMLInputElement
        rangeLongInput.type = "number"; rangeLongInput.min = "0"
        rangeLongInput.value = (existing?.rangeLongDistance ?: 0).toString()
        row3.appendChild(col(lbl(t("inv.longRange")), rangeLongInput))
        fun updateRangeEnabled() {
            val enabled = rangeCb.checked || thrownCb.checked
            rangeInput.disabled = !enabled
            rangeLongInput.disabled = !enabled
        }
        updateRangeEnabled()
        rangeCb.addEventListener("change", { updateRangeEnabled() })
        thrownCb.addEventListener("change", { updateRangeEnabled() })
        form.appendChild(row3)

        // Row 4: Versátil | Dado de Dano (duas mãos)
        val row4 = cbRow("1fr 2fr 1fr")
        versatileCb = addCb(row4, t("inv.versatile"), existing?.versatile ?: false)
        versatileInput = document.createElement("input") as HTMLInputElement
        versatileInput.value = existing?.versatileDice ?: ""
        versatileInput.placeholder = "e.g. 1d10"
        row4.appendChild(col(lbl(t("inv.twoHandedDice")), versatileInput))
        fun updateVersatileEnabled() {
            versatileInput.disabled = !versatileCb.checked
        }
        updateVersatileEnabled()
        versatileCb.addEventListener("change", { updateVersatileEnabled() })
        form.appendChild(row4)

        // Row 5: Especial | Descrição
        val row5 = cbRow("1fr 3fr")
        specialCb = addCb(row5, t("inv.special"), existing?.special ?: false)
        specialInput = document.createElement("input") as HTMLInputElement
        specialInput.value = existing?.specialDescription ?: ""
        row5.appendChild(col(lbl(t("inv.special")), specialInput))
        fun updateSpecialEnabled() {
            specialInput.disabled = !specialCb.checked
        }
        updateSpecialEnabled()
        specialCb.addEventListener("change", { updateSpecialEnabled() })
        form.appendChild(row5)

        // Additional Features
        val addFeatLbl = document.createElement("label") as HTMLLabelElement
        addFeatLbl.textContent = t("inv.additionalFeatures")
        addFeatLbl.className = "modal__full-width"
        form.appendChild(addFeatLbl)
        addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
        addFeatInput.value = existing?.additionalFeatures ?: ""
        addFeatInput.className = "modal__full-width"
        form.appendChild(addFeatInput)

        // Weight + Price
        weightAndPriceField = WeightAndPriceField(form)
        weightAndPriceField.setValues(
            weight = existing?.weight ?: 0.0,
            price = existing?.price ?: 0,
            priceCurrency = existing?.priceCurrency ?: "pg"
        )

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
        val wp = weightAndPriceField.getValues()
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
            rangeDistance = if (rangeInput.disabled) 0 else rangeInput.value.toIntOrNull() ?: 0,
            rangeLongDistance = if (rangeLongInput.disabled) 0 else rangeLongInput.value.toIntOrNull() ?: 0,
            reach = reachCb.checked,
            special = specialCb.checked,
            specialDescription = if (specialInput.disabled) "" else specialInput.value,
            thrown = thrownCb.checked,
            twoHanded = twoHandedCb.checked,
            versatile = versatileCb.checked,
            versatileDice = if (versatileInput.disabled) "" else versatileInput.value,
            silver = silverCb.checked,
            additionalFeatures = addFeatInput.value,
            weight = wp.weight,
            price = wp.price,
            priceCurrency = wp.priceCurrency,
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
