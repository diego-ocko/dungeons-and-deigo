package com.dungeonsanddeigo.web.dnd.tabs.export

import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.t
import com.dungeonsanddeigo.model.DungeonsAndDragons
import kotlinx.browser.localStorage
import org.w3c.dom.*

// Page 3: Spells — simplified reference sheet (no descriptions)
fun buildExportPage3(ctx: ExportContext): HTMLDivElement? {
    val spells = ctx.spells
    if (spells.isEmpty()) return null

    val page = ctx.div("export-page")
    page.appendChild(ctx.buildHeader())

    val content = ctx.div("export-page3")

    // Spellcasting info bar
    val mainInfo = ctx.mainInfo
    val profBonus = ctx.profBonus
    val spellAbility = DungeonsAndDragons.spellcastingAbilityFor(mainInfo.mainClass, mainInfo.mainSubClass)
        ?: localStorage.getItem("dnd_custom_spell_ability_${ctx.character.id}_${mainInfo.mainClass}")
            ?.takeIf { it != "__none__" }

    if (spellAbility != null) {
        val spellMod = when (spellAbility) {
            "Str" -> ctx.strMod; "Dex" -> ctx.dexMod; "Con" -> ctx.conMod
            "Int" -> ctx.intMod; "Wis" -> ctx.wisMod; "Cha" -> ctx.chaMod; else -> 0
        }
        val spellDC = 8 + profBonus + spellMod
        val spellAtk = profBonus + spellMod

        val infoBar = ctx.div("export-spell-info-bar")
        fun infoBox(label: String, value: String) {
            val box = ctx.div("export-stat-box")
            box.appendChild(ctx.div("export-stat-box__val").also { it.textContent = value })
            box.appendChild(ctx.div("export-stat-box__label").also { it.textContent = label })
            infoBar.appendChild(box)
        }
        infoBox(t("magic.ability"), spellAbility)
        infoBox(t("magic.spellMod"), if (spellMod >= 0) "+$spellMod" else "$spellMod")
        infoBox(t("magic.spellDC"), spellDC.toString())
        infoBox(t("export.attacks"), if (spellAtk >= 0) "+$spellAtk" else "$spellAtk")
        content.appendChild(infoBar)
    }

    // Spell slots
    val totalLevel = ctx.totalLevel
    val slots = DungeonsAndDragons.spellSlotsFor(mainInfo.mainClass, mainInfo.mainSubClass, totalLevel)
    if (slots.isNotEmpty()) {
        content.appendChild(ctx.sectionLabel(t("export.spellSlots")))
        val slotsRow = ctx.div("export-spell-slots-row")
        slots.forEachIndexed { idx, count ->
            val box = ctx.div("export-spell-slot-box")
            box.appendChild(ctx.div("export-spell-slot-box__circle").also { it.textContent = "${idx + 1}º" })
            box.appendChild(ctx.div("export-spell-slot-box__count").also { it.textContent = "□".repeat(count) })
            slotsRow.appendChild(box)
        }
        content.appendChild(slotsRow)
    }

    // Spells by circle — compact table
    val circleOrder = listOf("Cantrip", "Circle 1", "Circle 2", "Circle 3", "Circle 4", "Circle 5", "Circle 6", "Circle 7", "Circle 8", "Circle 9")
    val spellsByCircle = spells.groupBy { it.circle }

    circleOrder.forEach { circle ->
        val circleSpells = spellsByCircle[circle] ?: return@forEach

        val circleSection = ctx.div("export-spell-circle-section")
        circleSection.appendChild(ctx.sectionLabel(circle))

        val table = document.createElement("table") as HTMLTableElement
        table.className = "export-table export-table--spells"
        val thead = document.createElement("thead")
        val hr = document.createElement("tr") as HTMLTableRowElement
        listOf(t("notes.title"), t("magic.ability"), t("inv.atkTable.range"), t("magic.prepared")).forEach { h ->
            val th = document.createElement("th") as HTMLTableCellElement
            th.textContent = h
            hr.appendChild(th)
        }
        thead.appendChild(hr); table.appendChild(thead)
        val tbody = document.createElement("tbody")

        circleSpells.sortedWith(compareByDescending<com.dungeonsanddeigo.model.DndSpell> { it.isPrepared }.thenBy { it.name })
            .forEach { spell ->
                val tr = document.createElement("tr") as HTMLTableRowElement

                val tdName = document.createElement("td") as HTMLTableCellElement
                val nameFlags = buildString {
                    append(spell.name)
                    val flags = mutableListOf<String>()
                    if (spell.needsConcentration) flags.add("C")
                    if (spell.canBeRitual) flags.add("R")
                    if (flags.isNotEmpty()) append(" [${flags.joinToString("")}]")
                }
                tdName.textContent = nameFlags
                tr.appendChild(tdName)

                val tdCast = document.createElement("td") as HTMLTableCellElement
                tdCast.textContent = buildString {
                    if (spell.castingTime.isNotEmpty()) append(spell.castingTime)
                    if (spell.duration.isNotEmpty()) append(" / ${spell.duration}")
                }
                tr.appendChild(tdCast)

                val tdRange = document.createElement("td") as HTMLTableCellElement
                tdRange.textContent = if (spell.range.isNotEmpty()) "${spell.range}m" else "—"
                tr.appendChild(tdRange)

                val tdPrep = document.createElement("td") as HTMLTableCellElement
                tdPrep.textContent = if (circle == "Cantrip") "—" else if (spell.isPrepared) "✔" else "□"
                tr.appendChild(tdPrep)

                tbody.appendChild(tr)
            }
        table.appendChild(tbody)
        circleSection.appendChild(table)
        content.appendChild(circleSection)
    }

    page.appendChild(content)
    return page
}

private val document get() = kotlinx.browser.document
