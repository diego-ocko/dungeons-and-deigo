package com.dungeonsanddeigo.web.dnd.modals.addConsumableLoot

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import com.dungeonsanddeigo.web.dnd.modals.consumable.DndConsumableModal
import kotlinx.browser.document
import org.w3c.dom.*

class DndAddConsumableLootModal(
    private val character: Character,
    private val onItemAdded: () -> Unit
) : DndModal(
    title = t("inv.addConsumableLoot"),
    hideSave = true
) {
    override fun buildForm(form: HTMLDivElement) {
        val desc = document.createElement("p") as HTMLParagraphElement
        desc.textContent = t("inv.selectExisting")
        form.appendChild(desc)

        val existingItems = Repos.consumable.getByCharacterId(character.id)

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
                close()
                onItemAdded()
            })
            row.appendChild(addOneBtn)
            form.appendChild(row)
        }

        val newBtn = document.createElement("button") as HTMLButtonElement
        newBtn.textContent = "\u2795 " + t("inv.newConsumable")
        newBtn.className = "modal__slot-btn"
        newBtn.addEventListener("click", {
            close()
            DndConsumableModal(character, null).show { onItemAdded() }
        })
        form.appendChild(newBtn)
    }

    override fun onSave(close: () -> Unit) {}
}

