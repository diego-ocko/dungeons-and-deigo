package com.dungeonsanddeigo.web.dnd.modals.addConsumableLoot

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.consumable.showConsumableModal
import kotlinx.browser.document
import org.w3c.dom.*

fun showAddConsumableLootModal(character: Character, onDone: () -> Unit) {
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

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = t("inv.addConsumableLoot")
    modal.appendChild(titleEl)

    val desc = document.createElement("p") as HTMLParagraphElement
    desc.textContent = t("inv.selectExisting")
    desc.style.fontSize = "13px"; desc.style.color = "#666"
    modal.appendChild(desc)

    val existingItems = Repos.consumable.getByCharacterId(character.id)

    if (existingItems.isNotEmpty()) {
        existingItems.forEach { item ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"

            val info = document.createElement("span") as HTMLSpanElement
            info.textContent = "${item.name} (x${item.quantity})"
            info.style.fontSize = "13px"
            row.appendChild(info)

            val addOneBtn = document.createElement("button") as HTMLButtonElement
            addOneBtn.textContent = "+1"
            addOneBtn.style.fontSize = "11px"
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
    newBtn.style.marginTop = "12px"
    newBtn.style.width = "100%"
    newBtn.addEventListener("click", {
        document.body?.removeChild(overlay)
        showConsumableModal(character, null) { onDone() }
    })
    modal.appendChild(newBtn)

    // Cancel
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.style.marginTop = "8px"
    cancelBtn.style.width = "100%"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)

    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

