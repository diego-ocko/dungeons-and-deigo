package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.DndInventoryItem
import org.w3c.dom.*

// Page 5: Inventory (full) + Magic items (with descriptions) + Spells (with descriptions)
fun buildExportPage5(ctx: ExportContext): HTMLDivElement? {
    val items = ctx.items
    val consumables = ctx.consumables
    val magicItems = ctx.magicItems
    val spells = ctx.spells
    val money = ctx.money

    val hasContent = items.isNotEmpty() || consumables.isNotEmpty() || magicItems.isNotEmpty() || spells.isNotEmpty()
    if (!hasContent) return null

    val page = ctx.div("export-page")
    page.appendChild(ctx.buildHeader())

    val content = ctx.div("export-page5")

    // Money
    val moneyStr = buildString {
        if (money.platinum > 0) append("${money.platinum}pp ")
        if (money.gold > 0) append("${money.gold}po ")
        if (money.electrum > 0) append("${money.electrum}pe ")
        if (money.silver > 0) append("${money.silver}ps ")
        if (money.copper > 0) append("${money.copper}pc")
    }.trim()

    if (moneyStr.isNotEmpty()) {
        content.appendChild(ctx.sectionLabel(t("export.inventory")))
        content.appendChild(ctx.div("export-money").also { it.textContent = moneyStr })
    }

    // General items
    if (items.isNotEmpty()) {
        if (moneyStr.isEmpty()) content.appendChild(ctx.sectionLabel(t("export.inventory")))
        val table = buildItemTable(ctx)
        val thead = document.createElement("thead")
        val hr = document.createElement("tr") as HTMLTableRowElement
        listOf(t("notes.title"), t("inv.quantity"), t("inv.weight"), t("inv.effect")).forEach { h ->
            hr.appendChild((document.createElement("th") as HTMLTableCellElement).also { it.textContent = h })
        }
        thead.appendChild(hr); table.appendChild(thead)
        val tbody = document.createElement("tbody")
        items.sortedBy { it.name }.forEach { item ->
            val tr = document.createElement("tr") as HTMLTableRowElement
            tr.appendChild(td(item.name))
            tr.appendChild(td(item.quantity.toString()))
            tr.appendChild(td(if (item.weight > 0) "${item.weight}kg" else "—"))
            tr.appendChild(td(item.description))
            tbody.appendChild(tr)
        }
        table.appendChild(tbody)
        content.appendChild(table)
    }

    // Consumables
    if (consumables.isNotEmpty()) {
        content.appendChild(ctx.sectionLabel(t("inv.consumable.other")))
        val table = buildItemTable(ctx)
        val thead = document.createElement("thead")
        val hr = document.createElement("tr") as HTMLTableRowElement
        listOf(t("notes.title"), t("inv.type"), t("inv.quantity"), t("inv.effect")).forEach { h ->
            hr.appendChild((document.createElement("th") as HTMLTableCellElement).also { it.textContent = h })
        }
        thead.appendChild(hr); table.appendChild(thead)
        val tbody = document.createElement("tbody")
        consumables.sortedBy { it.name }.forEach { c ->
            val tr = document.createElement("tr") as HTMLTableRowElement
            tr.appendChild(td(c.name))
            tr.appendChild(td(c.type))
            tr.appendChild(td(c.quantity.toString()))
            tr.appendChild(td(c.effect))
            tbody.appendChild(tr)
        }
        table.appendChild(tbody)
        content.appendChild(table)
    }

    // Magic items
    if (magicItems.isNotEmpty()) {
        content.appendChild(ctx.sectionLabel(t("inv.synchedItems")))
        magicItems.sortedBy { it.name }.forEach { item ->
            val card = ctx.div("export-detail-card")
            val nameRow = ctx.div("export-detail-card__name")
            nameRow.textContent = buildString {
                append(item.name)
                if (item.needSynch) append(" [${t("inv.needSynch")}]")
                if (item.isSynched) append(" ✔")
            }
            card.appendChild(nameRow)
            if (item.effect.isNotEmpty())
                card.appendChild(ctx.p("export-detail-card__desc", item.effect))
            content.appendChild(card)
        }
    }

    // Spells with full descriptions
    if (spells.isNotEmpty()) {
        content.appendChild(ctx.sectionLabel(t("export.spells")))
        val circleOrder = listOf("Cantrip", "Circle 1", "Circle 2", "Circle 3", "Circle 4", "Circle 5", "Circle 6", "Circle 7", "Circle 8", "Circle 9")
        val spellsByCircle = spells.groupBy { it.circle }

        circleOrder.forEach { circle ->
            val circleSpells = spellsByCircle[circle] ?: return@forEach
            val circleHeader = ctx.div("export-spell-circle-header")
            circleHeader.textContent = circle
            content.appendChild(circleHeader)

            circleSpells.sortedBy { it.name }.forEach { spell ->
                val card = ctx.div("export-detail-card")

                val nameRow = ctx.div("export-detail-card__name")
                nameRow.textContent = buildString {
                    append(spell.name)
                    val flags = mutableListOf<String>()
                    if (spell.isPrepared) flags.add("P")
                    if (spell.needsConcentration) flags.add("C")
                    if (spell.canBeRitual) flags.add("R")
                    if (flags.isNotEmpty()) append(" [${flags.joinToString("")}]")
                    if (spell.school.isNotEmpty()) append("  ·  ${spell.school}")
                }
                card.appendChild(nameRow)

                val metaRow = ctx.div("export-detail-card__meta")
                metaRow.textContent = buildString {
                    if (spell.castingTime.isNotEmpty()) append("${t("magic.spellcasting")}: ${spell.castingTime}")
                    if (spell.range.isNotEmpty()) append("  ·  ${t("inv.atkTable.range")}: ${spell.range}m")
                    if (spell.duration.isNotEmpty()) append("  ·  ${spell.duration}")
                    val components = mutableListOf<String>()
                    if (spell.hasVerbal) components.add("V")
                    if (spell.hasSomatic) components.add("S")
                    if (spell.hasMaterial) components.add("M")
                    if (components.isNotEmpty()) append("  ·  ${components.joinToString("")}")
                }
                if (metaRow.textContent?.isNotEmpty() == true) card.appendChild(metaRow)

                if (spell.description.isNotEmpty())
                    card.appendChild(ctx.p("export-detail-card__desc", spell.description))

                if (spell.higherCircles.isNotEmpty()) {
                    val higher = ctx.p("export-detail-card__higher", spell.higherCircles)
                    card.appendChild(higher)
                }

                content.appendChild(card)
            }
        }
    }

    page.appendChild(content)
    return page
}

private fun buildItemTable(ctx: ExportContext): HTMLTableElement =
    (document.createElement("table") as HTMLTableElement).also { it.className = "export-table export-table--items" }

private fun td(text: String): HTMLTableCellElement =
    (document.createElement("td") as HTMLTableCellElement).also { it.textContent = text }

private val document get() = kotlinx.browser.document
