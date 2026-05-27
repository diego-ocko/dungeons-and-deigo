package com.dungeonsanddeigo.web.dnd.modals.changeArmor

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tArmorType
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndArmor
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showChangeArmorModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("playing.changeArmor")
    modal.appendChild(title)

    val armors = Repos.armor.getByCharacterId(character.id)
    val armorItems = armors.filter { it.type != "Shield" && it.type != "Clothes" }
    val shields = armors.filter { it.type == "Shield" }
    val clothes = armors.filter { it.type == "Clothes" }

    fun buildSelect(label: String, items: List<DndArmor>): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.className = "modal__section-label"
        modal.appendChild(lbl)
        val sel = document.createElement("select") as HTMLSelectElement
        
        val noneOpt = document.createElement("option") as HTMLOptionElement
        noneOpt.value = "-1"; noneOpt.textContent = "-- None --"
        sel.appendChild(noneOpt)
        items.forEachIndexed { idx, item ->
            val opt = document.createElement("option") as HTMLOptionElement
            opt.value = idx.toString()
            opt.textContent = item.name
            if (item.isEquipped) opt.selected = true
            sel.appendChild(opt)
        }
        modal.appendChild(sel)
        return sel
    }

    val armorSelect = buildSelect("Armor", armorItems)
    val shieldSelect = buildSelect("Shield", shields)
    val clothesSelect = buildSelect("Clothes", clothes)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.className = "modal__buttons"
    

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = "Apply"
    saveBtn.addEventListener("click", {
        // Unequip all, then equip selected
        armors.filter { it.isEquipped }.forEach { Repos.armor.save(it.copy(isEquipped = false)) }
        val armorIdx = armorSelect.value.toIntOrNull() ?: -1
        if (armorIdx >= 0) Repos.armor.save(armorItems[armorIdx].copy(isEquipped = true))
        val shieldIdx = shieldSelect.value.toIntOrNull() ?: -1
        if (shieldIdx >= 0) Repos.armor.save(shields[shieldIdx].copy(isEquipped = true))
        val clothesIdx = clothesSelect.value.toIntOrNull() ?: -1
        if (clothesIdx >= 0) Repos.armor.save(clothes[clothesIdx].copy(isEquipped = true))
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(saveBtn)
    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

