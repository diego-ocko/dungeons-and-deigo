package com.dungeonsanddeigo.web.dnd.modals.note

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndNote
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.currentTimestamp
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import kotlinx.browser.document
import org.w3c.dom.*

class DndNoteModal(
    private val character: Character,
    private val existing: DndNote?
) : DndModal(
    title = if (existing != null) t("notes.editNote") else t("notes.addNote")
) {
    private lateinit var titleInput: HTMLInputElement
    private lateinit var sessionInput: HTMLInputElement
    private lateinit var noteInput: HTMLTextAreaElement
    private val selectedTags = mutableListOf<String>()

    init {
        if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)
    }

    override fun buildForm(form: HTMLDivElement) {
        titleInput = addInput(form, t("notes.title"), existing?.title ?: "").input
        sessionInput = addInput(form, t("notes.session"), existing?.session ?: "").input
        sessionInput.placeholder = "e.g. Session 5"
        noteInput = addTextarea(form, t("notes.note"), existing?.note ?: "", rows = 6).input

        // Tags
        val tagsLbl = document.createElement("label") as HTMLLabelElement
        tagsLbl.textContent = t("label.tags")
        form.appendChild(tagsLbl)

        val tagBadgesDiv = document.createElement("div") as HTMLDivElement
        tagBadgesDiv.className = "modal__tags"

        fun refreshTags() {
            tagBadgesDiv.innerHTML = ""
            selectedTags.forEach { tag ->
                val badge = document.createElement("span") as HTMLSpanElement
                badge.textContent = "$tag \u00D7"
                badge.className = "modal__tag"
                badge.addEventListener("click", { selectedTags.remove(tag); refreshTags() })
                tagBadgesDiv.appendChild(badge)
            }
        }
        refreshTags()
        form.appendChild(tagBadgesDiv)

        val tagAddRow = document.createElement("div") as HTMLDivElement
        tagAddRow.className = "modal__tag-add-row"
        val tagInput = document.createElement("input") as HTMLInputElement
        tagInput.placeholder = "Add tag..."
        tagAddRow.appendChild(tagInput)
        val tagAddBtn = document.createElement("button") as HTMLButtonElement
        tagAddBtn.textContent = t("btn.add")
        tagAddBtn.addEventListener("click", {
            val tag = tagInput.value.trim()
            if (tag.isNotEmpty() && tag !in selectedTags) { selectedTags.add(tag); refreshTags() }
            tagInput.value = ""
        })
        tagAddRow.appendChild(tagAddBtn)
        form.appendChild(tagAddRow)
    }

    override fun onSave(close: () -> Unit) {
        val now = currentTimestamp()
        val note = DndNote(
            id = existing?.id ?: 0,
            characterId = character.id,
            title = titleInput.value,
            timestamp = if (existing != null) existing.timestamp else now,
            session = sessionInput.value,
            note = noteInput.value,
            tags = selectedTags.toList()
        )
        Repos.note.save(note)
        close()
    }
}
