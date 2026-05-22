package com.dungeonsanddeigo.web.dnd.modals.tempSlot

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun showTempSlotModal(character: Character, className: String, slots: List<Int>, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"; overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"; overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")
    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"; modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "300px"; modal.style.width = "90%"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("magic.addTempSlot"); modal.appendChild(title)
    slots.forEachIndexed { idx, total ->
        val circleNum = idx + 1
        val slotKey = "dnd_spell_slots_${character.id}_${className}_$circleNum"
        val btn = document.createElement("button") as HTMLButtonElement
        btn.textContent = "${circleNum}\u00BA Circle"
        btn.style.display = "block"; btn.style.width = "100%"; btn.style.marginBottom = "6px"
        btn.addEventListener("click", {
            // Decrease used by 1 (effectively adding a temp slot)
            val used = localStorage.getItem(slotKey)?.toIntOrNull() ?: 0
            localStorage.setItem(slotKey, (used - 1).toString())
            document.body?.removeChild(overlay); onDone()
        })
        modal.appendChild(btn)
    }
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel"); cancelBtn.style.width = "100%"; cancelBtn.style.marginTop = "8px"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

