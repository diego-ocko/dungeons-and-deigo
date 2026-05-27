package com.dungeonsanddeigo.web.dnd.modals.preparedSpells

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showPreparedSpellsModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"
    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("magic.changePrepared"); modal.appendChild(title)

    val allSpells = Repos.spell.getByCharacterId(character.id).filter { it.circle != "Cantrip" }
    if (allSpells.isEmpty()) {
        val empty = document.createElement("p") as HTMLParagraphElement
        empty.textContent = t("magic.noSpellsToPrepare"); empty.className = "modal__empty"
        modal.appendChild(empty)
    } else {
        allSpells.sortedBy { it.circle }.forEach { spell ->
            val row = document.createElement("div") as HTMLDivElement
            row.className = "modal__row"
            val cb = document.createElement("input") as HTMLInputElement
            cb.type = "checkbox"; cb.checked = spell.isPrepared
            cb.addEventListener("change", {
                Repos.spell.save(spell.copy(isPrepared = cb.checked))
            })
            row.appendChild(cb)
            val info = document.createElement("span") as HTMLSpanElement
            info.textContent = "${spell.name} (${spell.circle})"
            info.className = "modal__row-info"
            row.appendChild(info)
            modal.appendChild(row)
        }
    }

    val closeBtn = document.createElement("button") as HTMLButtonElement
    closeBtn.textContent = t("magic.done"); closeBtn.className = "modal__full-btn"
    closeBtn.addEventListener("click", { document.body?.removeChild(overlay); onDone() })
    modal.appendChild(closeBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

