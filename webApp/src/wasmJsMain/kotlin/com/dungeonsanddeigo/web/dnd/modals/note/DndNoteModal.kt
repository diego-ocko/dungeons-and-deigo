package com.dungeonsanddeigo.web.dnd.modals.note

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.currentTimestamp
import kotlinx.browser.document
import org.w3c.dom.*

fun showNoteModal(character: Character, existing: com.dungeonsanddeigo.model.DndNote?, onDone: () -> Unit) {
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal"

    val titleEl = document.createElement("h3") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) t("notes.editNote") else t("notes.addNote")
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.className = "modal__form"

    // Title
    val titleLbl = document.createElement("label") as HTMLLabelElement
    form.appendChild(titleLbl)
    val titleInput = document.createElement("input") as HTMLInputElement
    titleInput.value = existing?.title ?: ""
    form.appendChild(titleInput)

    // Session
    val sessionLbl = document.createElement("label") as HTMLLabelElement
    form.appendChild(sessionLbl)
    val sessionInput = document.createElement("input") as HTMLInputElement
    sessionInput.value = existing?.session ?: ""
    sessionInput.placeholder = "e.g. Session 5"
    form.appendChild(sessionInput)

    // Note
    val noteLbl = document.createElement("label") as HTMLLabelElement
    form.appendChild(noteLbl)
    val noteInput = document.createElement("textarea") as HTMLTextAreaElement
    noteInput.value = existing?.note ?: ""
    form.appendChild(noteInput)

    // Tags
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsLbl = document.createElement("label") as HTMLLabelElement
    form.appendChild(tagsLbl)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.className = "modal__tags"

    fun refreshNoteTags() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.className = "modal__tag"
            badge.addEventListener("click", { selectedTags.remove(tag); refreshNoteTags() })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshNoteTags()
    form.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.className = "modal__tag-add-row"
    val tagInput = document.createElement("input") as HTMLInputElement
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
    btnRow.className = "modal__buttons"
    

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

