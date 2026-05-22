package com.dungeonsanddeigo.web.dnd.modals.preparedSpells

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showPreparedSpellsModal(character: Character, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.style.position = "fixed"; overlay.style.top = "0"; overlay.style.left = "0"
    overlay.style.width = "100%"; overlay.style.height = "100%"
    overlay.style.backgroundColor = "rgba(0,0,0,0.5)"; overlay.style.display = "flex"
    overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
    overlay.style.setProperty("z-index", "1000")
    val modal = document.createElement("div") as HTMLDivElement
    modal.style.backgroundColor = "white"; modal.style.borderRadius = "8px"; modal.style.padding = "24px"
    modal.style.maxWidth = "400px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"
    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("magic.changePrepared"); modal.appendChild(title)

    val allSpells = Repos.spell.getByCharacterId(character.id).filter { it.circle != "Cantrip" }
    if (allSpells.isEmpty()) {
        val empty = document.createElement("p") as HTMLParagraphElement
        empty.textContent = t("magic.noSpellsToPrepare"); empty.style.color = "#999"
        modal.appendChild(empty)
    } else {
        allSpells.sortedBy { it.circle }.forEach { spell ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"; row.style.alignItems = "center"; row.style.setProperty("gap", "8px")
            row.style.padding = "4px 0"; row.style.borderBottom = "1px solid #eee"
            val cb = document.createElement("input") as HTMLInputElement
            cb.type = "checkbox"; cb.checked = spell.isPrepared
            cb.addEventListener("change", {
                Repos.spell.save(spell.copy(isPrepared = cb.checked))
            })
            row.appendChild(cb)
            val info = document.createElement("span") as HTMLSpanElement
            info.textContent = "${spell.name} (${spell.circle})"
            info.style.fontSize = "13px"
            row.appendChild(info)
            modal.appendChild(row)
        }
    }

    val closeBtn = document.createElement("button") as HTMLButtonElement
    closeBtn.textContent = t("magic.done"); closeBtn.style.width = "100%"; closeBtn.style.marginTop = "12px"
    closeBtn.addEventListener("click", { document.body?.removeChild(overlay); onDone() })
    modal.appendChild(closeBtn)
    overlay.appendChild(modal); document.body?.appendChild(overlay)
}

