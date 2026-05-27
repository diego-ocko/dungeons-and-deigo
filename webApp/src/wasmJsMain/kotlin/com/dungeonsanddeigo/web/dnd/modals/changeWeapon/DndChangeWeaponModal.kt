package com.dungeonsanddeigo.web.dnd.modals.changeWeapon

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showChangeWeaponModal(
    character: Character,
    onSave: () -> Unit
) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = t("playing.changeWeapon")
    modal.appendChild(titleEl)

    val weapons = Repos.weapon.getByCharacterId(character.id)
    val currentEquipped = weapons.firstOrNull { it.isEquipped }

    val select = document.createElement("select") as HTMLSelectElement
    select.className = "modal__select"

    val noneOpt = document.createElement("option") as HTMLOptionElement
    noneOpt.value = ""; noneOpt.textContent = t("inv.none")
    select.appendChild(noneOpt)

    weapons.forEach { w ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = w.id.toString()
        opt.textContent = "${w.name} (${w.damageDice} ${w.damageType})"
        select.appendChild(opt)
    }
    select.value = currentEquipped?.id?.toString() ?: ""
    modal.appendChild(select)

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.className = "modal__buttons"
    

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = t("btn.save")
    saveBtn.addEventListener("click", {
        val selectedId = select.value.toLongOrNull()
        // Unequip all
        weapons.filter { it.isEquipped }.forEach { Repos.weapon.save(it.copy(isEquipped = false)) }
        // Equip selected
        if (selectedId != null) {
            val toEquip = weapons.firstOrNull { it.id == selectedId }
            if (toEquip != null) Repos.weapon.save(toEquip.copy(isEquipped = true))
        }
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

