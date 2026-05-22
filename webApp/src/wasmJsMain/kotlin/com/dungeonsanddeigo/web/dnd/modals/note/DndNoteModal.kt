package com.dungeonsanddeigo.web.dnd.modals.note

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.currentTimestamp
import kotlinx.browser.document
import org.w3c.dom.*

fun showNoteModal(character: Character, existing: com.dungeonsanddeigo.model.DndNote?, onDone: () -> Unit) {
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
    modal.style.maxWidth = "500px"; modal.style.width = "90%"
    modal.style.maxHeight = "80vh"; modal.style.overflowY = "auto"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) t("notes.editNote") else t("notes.addNote")
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.style.setProperty("display", "grid")
    form.style.setProperty("gap", "10px")

    // Title
    val titleLbl = document.createElement("label") as HTMLLabelElement
    titleLbl.textContent = t("notes.title"); titleLbl.style.fontWeight = "bold"
    form.appendChild(titleLbl)
    val titleInput = document.createElement("input") as HTMLInputElement
    titleInput.value = existing?.title ?: ""
    titleInput.style.padding = "6px"
    form.appendChild(titleInput)

    // Session
    val sessionLbl = document.createElement("label") as HTMLLabelElement
    sessionLbl.textContent = t("notes.session"); sessionLbl.style.fontWeight = "bold"
    form.appendChild(sessionLbl)
    val sessionInput = document.createElement("input") as HTMLInputElement
    sessionInput.value = existing?.session ?: ""
    sessionInput.placeholder = "e.g. Session 5"
    sessionInput.style.padding = "6px"
    form.appendChild(sessionInput)

    // Note
    val noteLbl = document.createElement("label") as HTMLLabelElement
    noteLbl.textContent = t("notes.note"); noteLbl.style.fontWeight = "bold"
    form.appendChild(noteLbl)
    val noteInput = document.createElement("textarea") as HTMLTextAreaElement
    noteInput.value = existing?.note ?: ""
    noteInput.rows = 6; noteInput.style.width = "100%"
    form.appendChild(noteInput)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsLbl = document.createElement("label") as HTMLLabelElement
    tagsLbl.textContent = t("label.tags"); tagsLbl.style.fontWeight = "bold"
    form.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.style.display = "flex"
    tagBadgesDiv.style.setProperty("flex-wrap", "wrap")
    tagBadgesDiv.style.setProperty("gap", "4px")
    tagBadgesDiv.style.marginBottom = "6px"

    fun refreshNoteTags() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.style.backgroundColor = "#e0e0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "13px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshNoteTags() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshNoteTags()
    form.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.style.display = "flex"
    tagAddRow.style.setProperty("gap", "8px")
    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."; tagInput.style.padding = "4px"
    tagAddRow.appendChild(tagInput)
    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = t("btn.add")
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshNoteTags() }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    form.appendChild(tagAddRow)

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
    btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = t("btn.save")
    saveBtn.addEventListener("click", {
        val now = currentTimestamp()
        val note = com.dungeonsanddeigo.model.DndNote(
            id = existing?.id ?: 0,
            characterId = character.id,
            title = titleInput.value,
            timestamp = if (existing != null) existing.timestamp else now,
            session = sessionInput.value,
            note = noteInput.value,
            tags = selectedTags.toList()
        )
        Repos.note.save(note)
        document.body?.removeChild(overlay)
        onDone()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

