package com.dungeonsanddeigo.web.dnd.modals.changeArmor

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.components.modal.Modal
import org.w3c.dom.*

class DndChangeArmorModal(
    private val character: Character
) : Modal(
    title = t("playing.changeArmor"),
    saveLabel = "Apply"
) {
    private val armors = Repos.armor.getByCharacterId(character.id)
    private val armorItems = armors.filter { it.type != "Shield" && it.type != "Clothes" }
    private val shields = armors.filter { it.type == "Shield" }
    private val clothes = armors.filter { it.type == "Clothes" }

    private lateinit var armorSelect: HTMLSelectElement
    private lateinit var shieldSelect: HTMLSelectElement
    private lateinit var clothesSelect: HTMLSelectElement

    override fun buildForm(form: HTMLDivElement) {
        armorSelect = buildArmorSelect(form, "Armor", armorItems)
        shieldSelect = buildArmorSelect(form, "Shield", shields)
        clothesSelect = buildArmorSelect(form, "Clothes", clothes)
    }

    private fun buildArmorSelect(form: HTMLDivElement, label: String, items: List<DndArmor>): HTMLSelectElement {
        val options = mutableListOf("-1" to "-- None --")
        items.forEachIndexed { idx, item -> options.add(idx.toString() to item.name) }
        val currentValue = items.indexOfFirst { it.isEquipped }.let { if (it >= 0) it.toString() else "-1" }
        return addSelect(form, label, options, currentValue).input
    }

    override fun onSave(close: () -> Unit) {
        armors.filter { it.isEquipped }.forEach { Repos.armor.save(it.copy(isEquipped = false)) }
        val armorIdx = armorSelect.value.toIntOrNull() ?: -1
        if (armorIdx >= 0) Repos.armor.save(armorItems[armorIdx].copy(isEquipped = true))
        val shieldIdx = shieldSelect.value.toIntOrNull() ?: -1
        if (shieldIdx >= 0) Repos.armor.save(shields[shieldIdx].copy(isEquipped = true))
        val clothesIdx = clothesSelect.value.toIntOrNull() ?: -1
        if (clothesIdx >= 0) Repos.armor.save(clothes[clothesIdx].copy(isEquipped = true))
        close()
    }
}
