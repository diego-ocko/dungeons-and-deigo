package com.dungeonsanddeigo.web.dnd.modals.addStatus

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tStatus
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.components.modal.Modal
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

class DndAddStatusModal(
    private val character: Character
) : Modal(
    title = t("playing.addStatus"),
    size = "md",
    saveLabel = t("btn.add")
) {
    private lateinit var sel: HTMLSelectElement
    private lateinit var customInput: HTMLInputElement

    private val defaultStatuses = listOf("Poisoned", "Confused", "Flying", "Frightened", "Blinded", "Charmed", "Deafened", "Grappled", "Incapacitated", "Invisible", "Paralyzed", "Petrified", "Prone", "Restrained", "Stunned", "Unconscious", "Exhaustion")

    override fun buildForm(form: HTMLDivElement) {
        val options = mutableListOf("" to t("playing.selectStatus"))
        defaultStatuses.forEach { s -> options.add(s to tStatus(s)) }
        options.add("__custom__" to "Custom...")
        sel = addSelect(form, t("playing.addStatus"), options, "").input

        customInput = document.createElement("input") as HTMLInputElement
        customInput.placeholder = "Custom status"
        customInput.className = "modal__custom-input"
        form.appendChild(customInput)

        sel.addEventListener("change", {
            customInput.style.display = if (sel.value == "__custom__") "block" else "none"
        })
    }

    override fun onSave(close: () -> Unit) {
        val status = if (sel.value == "__custom__") customInput.value.trim() else sel.value
        if (status.isNotEmpty()) {
            val statusKey = "dnd_playing_status_${character.id}"
            val current = localStorage.getItem(statusKey)?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            if (status !in current) current.add(status)
            localStorage.setItem(statusKey, current.joinToString(","))
            close()
        }
    }
}
