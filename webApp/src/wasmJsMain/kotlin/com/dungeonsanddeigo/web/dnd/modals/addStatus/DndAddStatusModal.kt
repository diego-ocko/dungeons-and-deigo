package com.dungeonsanddeigo.web.dnd.modals.addStatus

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.i18n.tStatus
import com.dungeonsanddeigo.model.Character
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.*

fun showAddStatusModal(character: Character, onDone: () -> Unit) {
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
    modal.style.maxWidth = "350px"; modal.style.width = "90%"

    val title = document.createElement("h3") as HTMLHeadingElement
    title.textContent = t("playing.addStatus")
    modal.appendChild(title)

    val defaultStatuses = listOf("Poisoned", "Confused", "Flying", "Frightened", "Blinded", "Charmed", "Deafened", "Grappled", "Incapacitated", "Invisible", "Paralyzed", "Petrified", "Prone", "Restrained", "Stunned", "Unconscious", "Exhaustion")

    val sel = document.createElement("select") as HTMLSelectElement
    sel.style.width = "100%"; sel.style.padding = "6px"; sel.style.marginBottom = "8px"
    val emptyOpt = document.createElement("option") as HTMLOptionElement
    emptyOpt.value = ""; emptyOpt.textContent = t("playing.selectStatus")
    sel.appendChild(emptyOpt)
    defaultStatuses.forEach { s ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = s; opt.textContent = tStatus(s)
        sel.appendChild(opt)
    }
    val customOpt = document.createElement("option") as HTMLOptionElement
    customOpt.value = "__custom__"; customOpt.textContent = "Custom..."
    sel.appendChild(customOpt)
    modal.appendChild(sel)

    val customInput = document.createElement("input") as HTMLInputElement
    customInput.placeholder = "Custom status"
    customInput.style.width = "100%"; customInput.style.padding = "6px"
    customInput.style.display = "none"; customInput.style.marginBottom = "8px"
    modal.appendChild(customInput)

    sel.addEventListener("change", {
        customInput.style.display = if (sel.value == "__custom__") "block" else "none"
    })

    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val addBtn = document.createElement("button") as HTMLButtonElement
    addBtn.textContent = "Add"
    addBtn.addEventListener("click", {
        val status = if (sel.value == "__custom__") customInput.value.trim() else sel.value
        if (status.isNotEmpty()) {
            val statusKey = "dnd_playing_status_${character.id}"
            val current = localStorage.getItem(statusKey)?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            if (status !in current) current.add(status)
            localStorage.setItem(statusKey, current.joinToString(","))
            document.body?.removeChild(overlay)
            onDone()
        }
    })
    btnRow.appendChild(addBtn)
    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

