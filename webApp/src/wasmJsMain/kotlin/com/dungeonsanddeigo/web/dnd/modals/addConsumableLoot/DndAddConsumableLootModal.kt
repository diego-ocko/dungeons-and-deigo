package com.dungeonsanddeigo.web.dnd.modals.addConsumableLoot

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.consumable.showConsumableModal
import kotlinx.browser.document
import org.w3c.dom.*

fun showAddConsumableLootModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = t("inv.addConsumableLoot")
    modal.appendChild(titleEl)

    val desc = document.createElement("p") as HTMLParagraphElement
    desc.textContent = t("inv.selectExisting")
    modal.appendChild(desc)

    val existingItems = Repos.consumable.getByCharacterId(character.id)

    if (existingItems.isNotEmpty()) {
        existingItems.forEach { item ->
            val row = document.createElement("div") as HTMLDivElement
            row.className = "modal__row"

            val info = document.createElement("span") as HTMLSpanElement
            info.textContent = "${item.name} (x${item.quantity})"
            info.className = "modal__row-info"
            row.appendChild(info)

            val addOneBtn = document.createElement("button") as HTMLButtonElement
            addOneBtn.textContent = "+1"
            addOneBtn.addEventListener("click", {
                Repos.consumable.save(item.copy(quantity = item.quantity + 1))
                document.body?.removeChild(overlay)
                onDone()
            })
            row.appendChild(addOneBtn)
            modal.appendChild(row)
        }
    }

    // New item button
    val newBtn = document.createElement("button") as HTMLButtonElement
    newBtn.textContent = "\u2795 " + t("inv.newConsumable")
    newBtn.addEventListener("click", {
        document.body?.removeChild(overlay)
        showConsumableModal(character, null) { onDone() }
    })
    modal.appendChild(newBtn)

    // Cancel
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)

    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

