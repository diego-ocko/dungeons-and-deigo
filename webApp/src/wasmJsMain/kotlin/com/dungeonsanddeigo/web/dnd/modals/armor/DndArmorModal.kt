package com.dungeonsanddeigo.web.dnd.modals.armor

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tArmorType
import com.dungeonsanddeigo.i18n.tAcModifier
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
import com.dungeonsanddeigo.web.dnd.components.weightAndPrice.WeightAndPriceField
import kotlinx.browser.document
import org.w3c.dom.*

class DndArmorModal(
    private val character: Character,
    private val existing: DndArmor?
) : Modal(
    title = if (existing != null) t("inv.editArmor") else t("inv.addArmor"),
    size = "lg"
) {
    private lateinit var nameInput: HTMLInputElement
    private lateinit var typeSelect: HTMLSelectElement
    private lateinit var acInput: HTMLInputElement
    private lateinit var acModSelect: HTMLSelectElement
    private lateinit var minStrInput: HTMLInputElement
    private lateinit var sneakCb: HTMLInputElement
    private lateinit var addFeatInput: HTMLTextAreaElement
    private lateinit var weightAndPrice: WeightAndPriceField
    private lateinit var eqCb: HTMLInputElement
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        form.classList.add("armor-modal")

    // Name + Type row (name 2/3, type 1/3)
    val nameTypeRow = document.createElement("div") as HTMLDivElement
    nameTypeRow.className = "armor-modal__name-type-row modal__full-width"

    val nameCol = document.createElement("div") as HTMLDivElement
    nameCol.className = "armor-modal__name-col"
    val nameLbl = document.createElement("label") as HTMLLabelElement
    nameLbl.textContent = t("label.name")
    nameCol.appendChild(nameLbl)
    nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    nameCol.appendChild(nameInput)
    nameTypeRow.appendChild(nameCol)

    val typeCol = document.createElement("div") as HTMLDivElement
    typeCol.className = "armor-modal__type-col"
    val typeColLbl = document.createElement("label") as HTMLLabelElement
    typeColLbl.textContent = t("inv.type")
    typeCol.appendChild(typeColLbl)
    typeSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.types.forEach { tp ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = tp; o.textContent = tArmorType(tp); typeSelect.appendChild(o)
    }
    typeSelect.value = existing?.type ?: DndArmor.types.first()
    typeCol.appendChild(typeSelect)
    nameTypeRow.appendChild(typeCol)

    form.appendChild(nameTypeRow)

    // AC Base + AC Modifier row
    val acRow = document.createElement("div") as HTMLDivElement
    acRow.className = "armor-modal__fields-row modal__full-width"

    val acCol = document.createElement("div") as HTMLDivElement
    acCol.className = "armor-modal__field-col"
    val acLbl = document.createElement("label") as HTMLLabelElement
    acLbl.textContent = t("inv.baseAC")
    acCol.appendChild(acLbl)
    acInput = document.createElement("input") as HTMLInputElement
    acInput.type = "number"; acInput.value = (existing?.baseAC ?: 10).toString()
    acCol.appendChild(acInput)
    acRow.appendChild(acCol)

    val acModCol = document.createElement("div") as HTMLDivElement
    acModCol.className = "armor-modal__field-col"
    val acModLbl = document.createElement("label") as HTMLLabelElement
    acModLbl.textContent = t("inv.acModifier")
    acModCol.appendChild(acModLbl)
    acModSelect = document.createElement("select") as HTMLSelectElement
    DndArmor.acModifiers.forEach { m ->
        val o = document.createElement("option") as HTMLOptionElement
        o.value = m; o.textContent = tAcModifier(m); acModSelect.appendChild(o)
    }
    acModSelect.value = existing?.acModifier ?: "none"
    acModCol.appendChild(acModSelect)
    acRow.appendChild(acModCol)

    form.appendChild(acRow)

    // Minimum Strength + Sneak Disadvantage row
    val minStrSneakRow = document.createElement("div") as HTMLDivElement
    minStrSneakRow.className = "armor-modal__fields-row modal__full-width"

    val minStrCol = document.createElement("div") as HTMLDivElement
    minStrCol.className = "armor-modal__field-col"
    val minStrLbl = document.createElement("label") as HTMLLabelElement
    minStrLbl.textContent = t("inv.minStr")
    minStrCol.appendChild(minStrLbl)
    minStrInput = document.createElement("input") as HTMLInputElement
    minStrInput.type = "number"; minStrInput.min = "0"; minStrInput.max = "20"
    minStrInput.value = (existing?.minimumStrength ?: 0).toString()
    minStrCol.appendChild(minStrInput)
    minStrSneakRow.appendChild(minStrCol)

    val sneakCol = document.createElement("div") as HTMLDivElement
    sneakCol.className = "armor-modal__field-col"
    sneakCb = document.createElement("input") as HTMLInputElement
    sneakCb.type = "checkbox"; sneakCb.checked = existing?.hasSneakDisadvantage ?: false
    val sneakLbl = document.createElement("label") as HTMLLabelElement
    sneakLbl.className = "armor-modal__sneak-label"
    sneakLbl.appendChild(sneakCb)
    sneakLbl.append(t("inv.sneakDisadv"))
    sneakCol.appendChild(sneakLbl)
    minStrSneakRow.appendChild(sneakCol)

    form.appendChild(minStrSneakRow)

    // Visibility logic based on type
    fun updateArmorFieldVisibility() {
        val isShield = typeSelect.value == "Shield"
        val isClothes = typeSelect.value == "Clothes"
        val isHeavy = typeSelect.value == "Heavy Armor"
        acRow.style.display = if (isShield || isClothes) "none" else ""
        minStrCol.style.display = if (!isHeavy) "none" else ""
        sneakCol.style.display = if (isShield || isClothes) "none" else ""
    }
    updateArmorFieldVisibility()
    typeSelect.addEventListener("change", { updateArmorFieldVisibility() })

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
    weightAndPrice = WeightAndPriceField(form)
    weightAndPrice.setValues(
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
        val wp = weightAndPrice.getValues()
        val armor = DndArmor(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = nameInput.value,
            type = typeSelect.value,
            baseAC = acInput.value.toIntOrNull() ?: 10,
            acModifier = acModSelect.value,
            minimumStrength = minStrInput.value.toIntOrNull() ?: 0,
            hasSneakDisadvantage = sneakCb.checked,
            weight = wp.weight,
            price = wp.price,
            priceCurrency = wp.priceCurrency,
            isEquipped = eqCb.checked,
            tags = tagsField.getTags(),
            additionalFeatures = addFeatInput.value
        )
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
        close()
    }
}
