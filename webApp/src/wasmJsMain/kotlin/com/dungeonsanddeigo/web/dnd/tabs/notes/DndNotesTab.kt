package com.dungeonsanddeigo.web.dnd.tabs.notes

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.note.DndNoteModal
import kotlinx.browser.document
import org.w3c.dom.*

fun renderDndNotesTab(character: Character, container: HTMLDivElement) {
    fun refresh() {
        container.innerHTML = ""

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "\uD83D\uDCDD " + t("notes.addNote")
        addBtn.className = "notes-add-btn"
        addBtn.addEventListener("click", {
            DndNoteModal(character, null).show { refresh() }
        })
        container.appendChild(addBtn)

        val notes = Repos.note.getByCharacterId(character.id).sortedByDescending { it.timestamp }

        if (notes.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = t("notes.noNotes")
            empty.className = "notes-empty"
            container.appendChild(empty)
            return
        }

        notes.forEach { note ->
            val card = document.createElement("div") as HTMLDivElement
            card.className = "notes-card"

            val header = document.createElement("div") as HTMLDivElement
            header.className = "notes-header"

            val titleEl = document.createElement("h4") as HTMLHeadingElement
            titleEl.textContent = note.title.ifEmpty { "(Untitled)" }
            header.appendChild(titleEl)

            val metaDiv = document.createElement("div") as HTMLDivElement
            metaDiv.className = "notes-meta"
            if (note.session.isNotEmpty()) {
                val sessionSpan = document.createElement("div") as HTMLDivElement
                sessionSpan.textContent = "${t("notes.sessionLabel")}: ${note.session}"
                metaDiv.appendChild(sessionSpan)
            }
            val timeSpan = document.createElement("div") as HTMLDivElement
            timeSpan.textContent = note.timestamp
            metaDiv.appendChild(timeSpan)
            header.appendChild(metaDiv)

            card.appendChild(header)

            val noteContent = document.createElement("p") as HTMLParagraphElement
            noteContent.textContent = note.note
            noteContent.className = "notes-content"
            card.appendChild(noteContent)

            if (note.tags.isNotEmpty()) {
                val tagsDiv = document.createElement("div") as HTMLDivElement
                tagsDiv.className = "notes-tags"
                note.tags.forEach { tag ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = tag
                    badge.className = "notes-tag"
                    tagsDiv.appendChild(badge)
                }
                card.appendChild(tagsDiv)
            }

            val actions = document.createElement("div") as HTMLDivElement
            actions.className = "notes-actions"

            val editBtn = document.createElement("button") as HTMLButtonElement
            editBtn.textContent = t("btn.edit")
            editBtn.className = "notes-btn"
            editBtn.addEventListener("click", { DndNoteModal(character, note).show { refresh() } })
            actions.appendChild(editBtn)

            val deleteBtn = document.createElement("button") as HTMLButtonElement
            deleteBtn.textContent = t("btn.delete")
            deleteBtn.className = "notes-del-btn"
            deleteBtn.addEventListener("click", {
                Repos.note.delete(character.id, note.id)
                refresh()
            })
            actions.appendChild(deleteBtn)

            card.appendChild(actions)
            container.appendChild(card)
        }
    }

    refresh()
}

