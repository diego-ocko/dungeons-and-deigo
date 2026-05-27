package com.dungeonsanddeigo.web.dnd.modals.tempSlot

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun showTempSlotModal(character: Character, className: String, slots: List<Int>, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"
    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal modal--sm"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("magic.addTempSlot"); modal.appendChild(title)
    slots.forEachIndexed { idx, total ->
        val circleNum = idx + 1
        val slotKey = "dnd_spell_slots_${character.id}_${className}_$circleNum"
        val btn = document.createElement("button") as HTMLButtonElement
        btn.textContent = "${circleNum}\u00BA Circle"
        btn.className = "modal__slot-btn"
        btn.addEventListener("click", {
            // Decrease used by 1 (effectively adding a temp slot)
            val used = localStorage.getItem(slotKey)?.toIntOrNull() ?: 0
            localStorage.setItem(slotKey, (used - 1).toString())
            document.body?.removeChild(overlay); onDone()
        })
        modal.appendChild(btn)
    }
    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel"); cancelBtn.className = "modal__full-btn"
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    modal.appendChild(cancelBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

