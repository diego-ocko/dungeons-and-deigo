package com.dungeonsanddeigo.web.dnd.modals.note

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndNote
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.currentTimestamp
import com.dungeonsanddeigo.web.dnd.components.modal.DndModal
import com.dungeonsanddeigo.web.dnd.components.tagsField.TagsField
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
    private lateinit var tagsField: TagsField

    override fun buildForm(form: HTMLDivElement) {
        titleInput = addInput(form, t("notes.title"), existing?.title ?: "").input
        sessionInput = addInput(form, t("notes.session"), existing?.session ?: "").input
        sessionInput.placeholder = "e.g. Session 5"
        noteInput = addTextarea(form, t("notes.note"), existing?.note ?: "", rows = 6).input

        // Tags
        tagsField = TagsField(form, existing?.tags ?: emptyList())
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
            tags = tagsField.getTags()
        )
        Repos.note.save(note)
        close()
    }
}
