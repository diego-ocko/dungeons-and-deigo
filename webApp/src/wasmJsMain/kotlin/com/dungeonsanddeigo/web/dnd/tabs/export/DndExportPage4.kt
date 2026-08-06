package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import org.w3c.dom.*

// Page 4: Notes — may span multiple pages via CSS
fun buildExportPage4(ctx: ExportContext): HTMLDivElement? {
    val notes = ctx.notes
    if (notes.isEmpty()) return null

    val page = ctx.div("export-page")
    page.appendChild(ctx.buildHeader(t("export.page4Title")))

    val content = ctx.div("export-page4")
    notes.sortedWith(compareBy<com.dungeonsanddeigo.model.DndNote> { it.session }.thenBy { it.timestamp })
        .forEach { note ->
            val card = ctx.div("export-note-card")

            val noteHeader = ctx.div("export-note-card__header")
            if (note.title.isNotEmpty())
                noteHeader.appendChild(ctx.span("export-note-card__title", note.title))
            val meta = buildString {
                if (note.session.isNotEmpty()) append(note.session)
                if (note.timestamp.isNotEmpty()) {
                    if (isNotEmpty()) append("  ·  ")
                    append(note.timestamp)
                }
            }
            if (meta.isNotEmpty())
                noteHeader.appendChild(ctx.span("export-note-card__meta", meta))
            card.appendChild(noteHeader)

            if (note.note.isNotEmpty())
                card.appendChild(ctx.p("export-note-card__body", note.note))

            content.appendChild(card)
        }

    page.appendChild(content)
    return page
}
