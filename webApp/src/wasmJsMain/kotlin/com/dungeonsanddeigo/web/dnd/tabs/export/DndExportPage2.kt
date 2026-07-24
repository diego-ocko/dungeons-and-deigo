package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import org.w3c.dom.*

// Page 2: Appearance + full backstory
fun buildExportPage2(ctx: ExportContext): HTMLDivElement? {
    val appearance = ctx.appearance
    val backstory = ctx.backstory
    if (appearance == null && backstory == null) return null

    val page = ctx.div("export-page")
    page.appendChild(ctx.buildHeader())

    val content = ctx.div("export-page2")

    // Appearance
    if (appearance != null) {
        content.appendChild(ctx.sectionLabel(t("export.background")))

        val physGrid = ctx.div("export-appearance-grid")
        fun physItem(label: String, value: String) {
            if (value.isEmpty()) return
            val item = ctx.div("export-appearance-item")
            item.appendChild(ctx.div("export-appearance-item__label").also { it.textContent = label })
            item.appendChild(ctx.div("export-appearance-item__val").also { it.textContent = value })
            physGrid.appendChild(item)
        }
        physItem(t("bg.age"), appearance.age)
        physItem(t("bg.height"), appearance.height)
        physItem(t("bg.weight"), appearance.weight)
        physItem(t("bg.eyeColor"), appearance.eyeColor)
        physItem(t("bg.skinColor"), appearance.skinColor)
        physItem(t("bg.hairColor"), appearance.hairColor)
        content.appendChild(physGrid)

        if (appearance.description.isNotEmpty()) {
            content.appendChild(ctx.sectionLabel(t("bg.appearanceDesc")))
            content.appendChild(ctx.p("export-bg-full-text", appearance.description))
        }
    }

    // Full backstory
    if (backstory != null) {
        fun bgField(label: String, text: String) {
            if (text.isEmpty()) return
            content.appendChild(ctx.sectionLabel(label))
            content.appendChild(ctx.p("export-bg-full-text", text))
        }
        bgField(t("bg.personalityTraits"), backstory.personalityTraits)
        bgField(t("bg.ideals"), backstory.ideals)
        bgField(t("bg.bonds"), backstory.bonds)
        bgField(t("bg.defects"), backstory.defects)
        bgField(t("bg.charBackstory"), backstory.characterBackstory)
        if (backstory.hasFaction && backstory.factionName.isNotEmpty()) {
            bgField(backstory.factionName, backstory.factionBackstory)
        }
    }

    page.appendChild(content)
    return page
}
