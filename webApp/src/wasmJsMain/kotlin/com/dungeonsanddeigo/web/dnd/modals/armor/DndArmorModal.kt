package com.dungeonsanddeigo.web.dnd.modals.armor

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tArmorType
import com.dungeonsanddeigo.i18n.tAcModifier
import com.dungeonsanddeigo.i18n.tCurrency
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
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
    private lateinit var weightInput: HTMLInputElement
    private lateinit var priceInput: HTMLInputElement
    private lateinit var currSelect: HTMLSelectElement
    private lateinit var addFeatInput: HTMLTextAreaElement
    private lateinit var eqCb: HTMLInputElement
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        form.classList.add("armor-modal")
    fun lbl(text: String) {
        val l = document.createElement("label") as HTMLLabelElement
        l.textContent = text
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
    acLbl.textContent = t("inv.baseAC")
    form.appendChild(acLbl)
    val acInput = document.createElement("input") as HTMLInputElement
    acInput.type = "number"; acInput.value = (existing?.baseAC ?: 10).toString()
    form.appendChild(acInput)

    // AC Modifier
    val acModLbl = document.createElement("label") as HTMLLabelElement
    acModLbl.textContent = t("inv.acModifier")
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
    minStrLbl.textContent = t("inv.minStr")
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
    weightRow.className = "armor-modal__weight-row"
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
    priceRow.className = "armor-modal__price-row"
    val priceInput = document.createElement("input") as HTMLInputElement
    priceInput.type = "number"; priceInput.min = "0"
    priceInput.value = (existing?.price ?: 0).toString()
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
    addFeatLbl.textContent = t("inv.additionalFeatures")
    addFeatLbl.className = "modal__full-width"
    form.appendChild(addFeatLbl)
    val addFeatInput = document.createElement("textarea") as HTMLTextAreaElement
    addFeatInput.value = existing?.additionalFeatures ?: ""
    addFeatInput.className = "modal__full-width"
    form.appendChild(addFeatInput)

    // Tags
    tagsField = TagsField(form, existing?.tags ?: emptyList())

    // Equipped (last field)
    val eqContainer = document.createElement("div") as HTMLDivElement
    eqContainer.className = "modal__full-width"
    val eqLbl = document.createElement("label") as HTMLLabelElement
    val eqCb = document.createElement("input") as HTMLInputElement
    eqCb.type = "checkbox"; eqCb.checked = existing?.isEquipped ?: false
    eqLbl.appendChild(eqCb); eqLbl.append(t("features.equipped"))
    eqContainer.appendChild(eqLbl)
    form.appendChild(eqContainer)
    }

    override fun onSave(close: () -> Unit) {
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
            tags = tagsField.getTags(),
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
        close()
    }
}


