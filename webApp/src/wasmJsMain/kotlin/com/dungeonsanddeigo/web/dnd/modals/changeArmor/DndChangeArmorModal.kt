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
    modal.style.maxWidth = "400px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("playing.changeArmor")
    modal.appendChild(title)

    val armors = Repos.armor.getByCharacterId(character.id)
    val armorItems = armors.filter { it.type != "Shield" && it.type != "Clothes" }
    val shields = armors.filter { it.type == "Shield" }
    val clothes = armors.filter { it.type == "Clothes" }

    fun buildSelect(label: String, items: List<DndArmor>): HTMLSelectElement {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = label; lbl.style.fontWeight = "bold"; lbl.style.display = "block"
        lbl.style.marginBottom = "4px"; lbl.style.marginTop = "10px"
        modal.appendChild(lbl)
        val sel = document.createElement("select") as HTMLSelectElement
        sel.style.width = "100%"; sel.style.padding = "4px"
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
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

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

