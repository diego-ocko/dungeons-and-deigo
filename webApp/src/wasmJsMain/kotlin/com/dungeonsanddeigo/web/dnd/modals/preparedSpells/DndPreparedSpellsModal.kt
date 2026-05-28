package com.dungeonsanddeigo.web.dnd.modals.preparedSpells

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import kotlinx.browser.document
import org.w3c.dom.*

class DndPreparedSpellsModal(
    private val character: Character
) : DndModal(
    title = t("magic.changePrepared"),
    hideSave = true,
    cancelLabel = t("magic.done")
) {
    override fun buildForm(form: HTMLDivElement) {
        val allSpells = Repos.spell.getByCharacterId(character.id).filter { it.circle != "Cantrip" }
        if (allSpells.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = t("magic.noSpellsToPrepare")
            empty.className = "modal__empty"
            form.appendChild(empty)
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
                form.appendChild(row)
            }
        }
    }

    override fun onSave(close: () -> Unit) {}
}
