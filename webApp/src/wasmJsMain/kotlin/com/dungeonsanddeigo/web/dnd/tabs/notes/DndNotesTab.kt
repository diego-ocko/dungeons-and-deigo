package com.dungeonsanddeigo.web.dnd.tabs.notes

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.note.showNoteModal
import kotlinx.browser.document
import org.w3c.dom.*

fun renderDndNotesTab(character: Character, container: HTMLDivElement) {
    fun refresh() {
        container.innerHTML = ""

        val addBtn = document.createElement("button") as HTMLButtonElement
        addBtn.textContent = "\uD83D\uDCDD " + t("notes.addNote")
        addBtn.style.marginBottom = "16px"
        addBtn.addEventListener("click", {
            showNoteModal(character, null) { refresh() }
        })
        container.appendChild(addBtn)

        val notes = Repos.note.getByCharacterId(character.id).sortedByDescending { it.timestamp }

        if (notes.isEmpty()) {
            val empty = document.createElement("p") as HTMLParagraphElement
            empty.textContent = t("notes.noNotes")
            empty.style.color = "#999"
            container.appendChild(empty)
            return
        }

        notes.forEach { note ->
            val card = document.createElement("div") as HTMLDivElement
            card.style.border = "1px solid #ccc"
            card.style.borderRadius = "8px"
            card.style.padding = "12px"
            card.style.marginBottom = "10px"
            card.style.maxWidth = "700px"

            // Header: title + session + timestamp
            val header = document.createElement("div") as HTMLDivElement
            header.style.display = "flex"
            header.style.justifyContent = "space-between"
            header.style.alignItems = "center"
            header.style.marginBottom = "6px"

            val titleEl = document.createElement("h4") as HTMLHeadingElement
            titleEl.textContent = note.title.ifEmpty { "(Untitled)" }
            titleEl.style.margin = "0"
            header.appendChild(titleEl)

            val metaDiv = document.createElement("div") as HTMLDivElement
            metaDiv.style.fontSize = "11px"; metaDiv.style.color = "#666"
            metaDiv.style.textAlign = "right"
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

            // Note content
            val noteContent = document.createElement("p") as HTMLParagraphElement
            noteContent.textContent = note.note
            noteContent.style.margin = "0 0 8px 0"
            noteContent.style.fontSize = "14px"
            noteContent.style.whiteSpace = "pre-wrap"
            card.appendChild(noteContent)

            // Tags
            if (note.tags.isNotEmpty()) {
                val tagsDiv = document.createElement("div") as HTMLDivElement
                tagsDiv.style.marginBottom = "8px"
                tagsDiv.style.display = "flex"
                tagsDiv.style.setProperty("flex-wrap", "wrap")
                tagsDiv.style.setProperty("gap", "4px")
                note.tags.forEach { tag ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = tag
                    badge.style.backgroundColor = "#eee"
                    badge.style.padding = "2px 6px"
                    badge.style.borderRadius = "4px"
                    badge.style.fontSize = "11px"
                    tagsDiv.appendChild(badge)
                }
                card.appendChild(tagsDiv)
            }

            // Actions
            val actions = document.createElement("div") as HTMLDivElement
            actions.style.display = "flex"
            actions.style.setProperty("gap", "8px")

            val editBtn = document.createElement("button") as HTMLButtonElement
            editBtn.textContent = t("btn.edit")
            editBtn.style.fontSize = "11px"
            editBtn.addEventListener("click", { showNoteModal(character, note) { refresh() } })
            actions.appendChild(editBtn)

            val deleteBtn = document.createElement("button") as HTMLButtonElement
            deleteBtn.textContent = t("btn.delete")
            deleteBtn.style.fontSize = "11px"; deleteBtn.style.color = "red"
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

